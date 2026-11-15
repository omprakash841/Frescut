package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FrescutRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `products can be seeded by owner, read by all signed-in users, and blocked from non-owner updates`() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = FrescutRepository(db = firestore, dao = null)

        val uniqueProdId = "prod_test_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        val testProduct = SeedData.initialProducts.first().copy(id = uniqueProdId)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.seedProduct(testProduct)
        }

        // Switch to Bob: Bob can list available products
        signInTestUser(BOB_EMAIL)
        val bobRepo = FrescutRepository(db = firestore, dao = null)
        val products = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getAvailableProducts()
        }
        assertTrue(products.any { it.id == uniqueProdId })

        // Bob CANNOT update Alice's product
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.updateProductPricePerKg(uniqueProdId, 999)
            }
            fail("Expected PERMISSION_DENIED when non-owner updates product")
        } catch (e: Exception) {
            val fsError = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError.code)
        }
    }

    @Test
    fun `orders are saved to Firestore with Order Placed, single timeline time, no fake rating or etaText, and blocked for other users`() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = FrescutRepository(db = firestore, dao = null)

        val cartItem = CartProductItem(
            product = SeedData.initialProducts.first(),
            quantity = 1,
            selectedBudget = 100,
            estimatedGrams = 121
        )

        val orderCode = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.placeOrder(
                cartItems = listOf(cartItem),
                discountAmount = 0,
                deliveryFee = 0,
                addressText = "Plot 42, Udhepali, Bargarh",
                paymentMethod = "Card on Delivery (POS at Doorstep)"
            )
        }
        assertTrue(orderCode.startsWith("#FC"))

        val observedOrders = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeUserOrders().first { list -> list.any { it.orderId == orderCode } }
        }
        val placedOrder = observedOrders.first { it.orderId == orderCode }
        assertEquals("Order Placed", placedOrder.status)
        assertEquals("Card on Delivery (POS at Doorstep)", placedOrder.paymentMethod)
        assertTrue("First timeline entry must have Order Placed time", placedOrder.timelineTimes[0].isNotBlank())
        assertTrue("Remaining timeline entries must be empty", placedOrder.timelineTimes.drop(1).all { it.isEmpty() })
        assertEquals(null, placedOrder.rating)
        assertEquals(null, placedOrder.etaText)
        assertEquals(null, placedOrder.deliveryPartner)
        assertEquals(null, placedOrder.chillerTemp)
        assertEquals(null, placedOrder.route)

        // Customer rates the order -> rating is saved in Firestore
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.rateOrder(orderCode, 4)
        }
        val ratedOrder = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeUserOrders().first { list ->
                list.find { it.orderId == orderCode }?.rating == 4.0f
            }.first { it.orderId == orderCode }
        }
        assertEquals(4.0f, ratedOrder.rating)

        // Switch to Bob: Bob cannot read Alice's order by ID
        signInTestUser(BOB_EMAIL)
        val bobRepo = FrescutRepository(db = firestore, dao = null)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getOrderById(orderCode)
            }
            fail("Expected PERMISSION_DENIED when reading another user's order")
        } catch (e: Exception) {
            val fsError = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError.code)
        }
    }

    @Test
    fun `new guest user starts with zero orders, places order to Firestore, and unauthorized session is rejected`() = runBlocking {
        auth.signOut()
        val guestUid = "emu_guest_${System.currentTimeMillis()}"
        val guestRepo = FrescutRepository(db = firestore, dao = null, sessionUserId = guestUid)

        // 1. New guest user initially has an empty My Orders list AND empty addresses list in Firestore
        val initialOrders = withTimeout(FLOW_TIMEOUT_MS) {
            guestRepo.observeUserOrders().first()
        }
        assertTrue("New guest should start with empty orders list", initialOrders.isEmpty())

        val initialAddresses = withTimeout(FLOW_TIMEOUT_MS) {
            guestRepo.observeUserAddresses().first()
        }
        assertTrue("New guest should start with empty addresses list", initialAddresses.isEmpty())

        // 2. Guest adds a real address, then places an order -> saved to Firestore and emitted in My Orders
        withTimeout(DEFAULT_TIMEOUT_MS) {
            guestRepo.addNewAddress(
                label = "Home",
                areaName = "Sector 4",
                fullAddress = "House 12, Sector 4, Main Road"
            )
        }
        val savedAddresses = withTimeout(FLOW_TIMEOUT_MS) {
            guestRepo.observeUserAddresses().first { it.isNotEmpty() }
        }
        assertEquals(1, savedAddresses.size)
        assertEquals("House 12, Sector 4, Main Road", savedAddresses.first().fullAddress)

        val cartItem = CartProductItem(
            product = SeedData.initialProducts.first(),
            quantity = 1,
            selectedBudget = 120,
            estimatedGrams = 145
        )
        val newOrderCode = withTimeout(DEFAULT_TIMEOUT_MS) {
            guestRepo.placeOrder(
                cartItems = listOf(cartItem),
                discountAmount = 0,
                deliveryFee = 0,
                addressText = savedAddresses.first().fullAddress
            )
        }
        assertTrue(newOrderCode.startsWith("#FC"))

        val updatedOrders = withTimeout(FLOW_TIMEOUT_MS) {
            guestRepo.observeUserOrders().first { list -> list.any { it.orderId == newOrderCode } }
        }
        assertEquals(1, updatedOrders.size)
        assertEquals(newOrderCode, updatedOrders.first().orderId)

        // 3. Invalid non-emulator session ID is rejected by Firestore rules with PERMISSION_DENIED
        val invalidRepo = FrescutRepository(db = firestore, dao = null, sessionUserId = "invalid_session_id")
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                invalidRepo.observeUserOrders().first()
            }
            fail("Expected PERMISSION_DENIED for invalid unauthenticated session ID")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertNotNull(fsError)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    @Test
    fun `ensureSeeded seeds config, banners, coupons, and recipes when empty, while keeping reviews empty`() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = FrescutRepository(db = firestore, dao = null)

        withTimeout(DEFAULT_TIMEOUT_MS * 2) {
            repo.ensureSeeded()
        }

        // 1. config / app_config document
        val appConfig = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeAppConfig().first { it.searchChips.isNotEmpty() }
        }
        assertEquals(listOf("Chicken", "Mutton", "Fish", "Eggs"), appConfig.searchChips)
        assertEquals("", appConfig.referralCode)
        assertEquals("FRESCUT", appConfig.storeName)
        assertEquals("", appConfig.supportPhone)
        assertEquals(0, appConfig.minOrderAmount)

        // 2. banners collection (2 active documents)
        val banners = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBanners().first { it.isNotEmpty() }
        }
        assertEquals(2, banners.size)
        assertEquals(listOf("Fresh Meat & Fish", "Weekend Combo Packs"), banners.map { it.title })

        // 3. coupons collection (WELCOME10 seeded with active = false -> observeActiveCoupons is empty)
        val activeCoupons = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeActiveCoupons().first()
        }
        assertTrue(activeCoupons.isEmpty())

        // 4. recipes collection (3 active documents)
        val recipes = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeRecipes().first { it.isNotEmpty() }
        }
        assertEquals(3, recipes.size)
        assertEquals(listOf("Butter Chicken", "Mutton Rogan Josh", "Garlic Prawns"), recipes.map { it.title })

        // 5. reviews collection (not seeded -> remains empty)
        val reviews = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeReviews().first()
        }
        assertTrue(reviews.isEmpty())
    }

    private companion object {
        const val ALICE_EMAIL = "alice@frescut.test"
        const val BOB_EMAIL = "bob@frescut.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}

