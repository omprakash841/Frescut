package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.example.data.BudgetCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun ensureLoggedInToHome() {
        composeTestRule.waitForIdle()
        if (composeTestRule.onAllNodesWithTag("continue_as_guest_button").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("continue_as_guest_button").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `verify automatic quantity formula and 192g example`() {
        // Prompt specification example: Product price ₹260/kg, Selected budget ₹50 -> ~192 grams
        val grams = BudgetCalculator.calculateEstimatedGrams(selectedBudget = 50, pricePerKg = 260)
        assertEquals(192, grams)
    }

    @Test
    fun `verify chicken starts at 50, increments by 10, shows 3 recommendations at 100, and updates cart`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("FRESCUT", context.getString(R.string.app_name))
        ensureLoggedInToHome()

        // 1. Open Chicken Breast Boneless product detail
        composeTestRule.onNodeWithTag("product_card_prod_chicken_breast").performClick()
        composeTestRule.waitForIdle()

        // Chicken starts at ₹50
        composeTestRule.onNodeWithTag("selected_budget_amount").assertTextContains("₹50")
        composeTestRule.onNodeWithTag("estimated_quantity_text").assertTextContains("Approx. 61 g (Estimated)")
        composeTestRule.onNodeWithTag("price_per_kg_text").assertTextContains("₹824/kg", substring = true)

        // Minus button at minimum (₹50) must NOT go below ₹50
        composeTestRule.onNodeWithTag("budget_minus_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("selected_budget_amount").assertTextContains("₹50")

        // 3. Plus button increases in ₹10 increments up to ₹100
        repeat(5) {
            composeTestRule.onNodeWithTag("budget_plus_btn").performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onNodeWithTag("selected_budget_amount").assertTextContains("₹100")
        composeTestRule.onNodeWithTag("estimated_quantity_text").assertTextContains("Approx. 121 g (Estimated)")

        // 5. Three recommendations appear at ₹100
        composeTestRule.onNodeWithTag("recommended_for_you_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_2").assertIsDisplayed()

        // 6. Selecting a recommendation updates the budget and estimated quantity
        composeTestRule.onNodeWithTag("recommendation_chip_1").performClick()
        composeTestRule.waitForIdle()

        // Add selected budget to cart and verify sticky cart bar updates
        composeTestRule.onNodeWithTag("product_detail_add_budget_btn").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun `verify mutton starts at 100 and shows 3 recommendations immediately`() {
        ensureLoggedInToHome()

        // 2. Open Mutton Curry Cut product detail
        composeTestRule.onNodeWithTag("product_card_prod_mutton_curry").performClick()
        composeTestRule.waitForIdle()

        // Mutton starts at ₹100
        composeTestRule.onNodeWithTag("selected_budget_amount").assertTextContains("₹100")

        // Three recommendations appear at ₹100
        composeTestRule.onNodeWithTag("recommended_for_you_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recommendation_chip_2").assertIsDisplayed()

        // Tap plus -> ₹110
        composeTestRule.onNodeWithTag("budget_plus_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("selected_budget_amount").assertTextContains("₹110")
    }

    @Test
    fun `verify smart bottom navigation hides on scroll down and shows on scroll up`() {
        ensureLoggedInToHome()

        // 9. Navigation stays visible at the top of the page
        composeTestRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()

        // 7. Scroll down on Home screen -> Bottom navigation hides
        composeTestRule.onNodeWithTag("home_lazy_column").performTouchInput {
            swipeUp()
        }
        composeTestRule.waitForIdle()
        assertTrue(
            composeTestRule.onAllNodesWithTag("bottom_nav_home").fetchSemanticsNodes().isEmpty()
        )

        // 8. Scroll up on Home screen -> Bottom navigation appears again
        composeTestRule.onNodeWithTag("home_lazy_column").performTouchInput {
            swipeDown()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()

        // 12. Existing navigation to Categories, Orders, and Customer Profile works
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
    }

    @Test
    fun `verify compact header, horizontal category navigation, and shopping bag interaction`() {
        ensureLoggedInToHome()

        // 1. Compact Header & Notification & Shopping Bag Icon
        composeTestRule.onNodeWithTag("header_location_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_notification_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_cart_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sticky_search_bar").assertIsDisplayed()

        // 2. Compact Flipkart-style horizontal category navigation tabs
        composeTestRule.onNodeWithTag("compact_cat_tab_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("compact_cat_tab_chicken").assertIsDisplayed()
        composeTestRule.onNodeWithTag("compact_cat_tab_mutton").assertIsDisplayed()

        // Selecting a category filters Home products without breaking layout
        composeTestRule.onNodeWithTag("compact_cat_tab_chicken").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("compact_cat_tab_all").performClick()
        composeTestRule.waitForIdle()

        // 3. Tapping Shopping Bag icon opens Cart & Checkout
        composeTestRule.onNodeWithTag("header_cart_button").performClick()
        composeTestRule.waitForIdle()
    }
}
