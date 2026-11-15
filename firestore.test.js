const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const EMU_GUEST_UID = "emu_guest_987654";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function makeValidProduct(ownerId = ALICE_UID) {
  const now = new Date();
  return {
    name: "Chicken Breast Boneless",
    category: "Chicken",
    subcategory: "Breast",
    pricePerKg: 824,
    price: 412,
    originalPrice: 453,
    discountPercent: 9,
    deliveryTimeMins: 30,
    image: "img_prod_chicken_breast",
    available: true,
    weightInfo: "500 g",
    piecesInfo: "3-5 Pieces",
    servingsInfo: "Serves 3-4",
    description: "Tender skinless chicken breast fillets.",
    isCurrentHit: true,
    isBudgetPick: false,
    isFlashDelivery: true,
    rating: 4.9,
    proteinGrams: 31,
    budgetSelectorEnabled: true,
    ownerId,
    createdAt: now,
    updatedAt: now,
  };
}

function makeValidOrder(userId = ALICE_UID) {
  const now = new Date();
  return {
    orderNumber: "#FC12848",
    userId,
    items: [
      {
        productId: "prod_chicken_breast",
        name: "Chicken Breast Boneless",
        weight: "500 g",
        price: 412,
        quantity: 1,
        image: "img_prod_chicken_breast",
      },
    ],
    total: 412,
    itemTotal: 412,
    deliveryFee: 0,
    packagingCharge: 0,
    address: "Plot 42, Udhepali, Bargarh",
    deliveryInstructions: "Ring bell once",
    paymentMethod: "Cash on Delivery",
    status: "Order Placed",
    time: "Today, 6:10 PM",
    timelineTimes: ["6:10 PM", "", "", "", ""],
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unfiltered orders query is rejected", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").get());
});

test("Products: visible to everyone, but only owner can update or delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const guestDb = testEnv.unauthenticatedContext().firestore();

  const prod = makeValidProduct(ALICE_UID);
  await assertSucceeds(aliceDb.collection("products").doc("prod_chicken_breast").set(prod));

  // Bob and Guest can read and list available products
  await assertSucceeds(bobDb.collection("products").doc("prod_chicken_breast").get());
  await assertSucceeds(guestDb.collection("products").where("available", "==", true).get());

  // Bob and Guest CANNOT update or delete Alice's product
  await assertFails(
    bobDb.collection("products").doc("prod_chicken_breast").update({
      pricePerKg: 900,
      updatedAt: new Date(),
    })
  );
  await assertFails(
    guestDb.collection("products").doc("prod_chicken_breast").update({
      pricePerKg: 900,
      updatedAt: new Date(),
    })
  );
  await assertFails(bobDb.collection("products").doc("prod_chicken_breast").delete());

  // Alice (owner) CAN update her product
  await assertSucceeds(
    aliceDb.collection("products").doc("prod_chicken_breast").update({
      pricePerKg: 850,
      updatedAt: new Date(),
    })
  );
});

test("Orders: must start at Order Placed without fake rating, and can be rated later", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const guestDb = testEnv.unauthenticatedContext().firestore();

  const aliceOrder = makeValidOrder(ALICE_UID);
  await assertSucceeds(aliceDb.collection("orders").doc("FC12848").set(aliceOrder));

  // Creating an order with pre-filled rating or non-'Order Placed' status is rejected
  await assertFails(
    aliceDb.collection("orders").doc("FC12849").set({
      ...makeValidOrder(ALICE_UID),
      status: "Out for Delivery",
    })
  );
  await assertFails(
    aliceDb.collection("orders").doc("FC12850").set({
      ...makeValidOrder(ALICE_UID),
      rating: 5.0,
    })
  );

  // Alice can read and query her own orders, and rate her order later
  await assertSucceeds(aliceDb.collection("orders").doc("FC12848").get());
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertSucceeds(
    aliceDb.collection("orders").doc("FC12848").update({
      rating: 5.0,
      updatedAt: new Date(),
    })
  );

  // Bob cannot read or query Alice's orders
  await assertFails(bobDb.collection("orders").doc("FC12848").get());
  await assertFails(bobDb.collection("orders").where("userId", "==", ALICE_UID).get());

  // Emulator guest can create and query their own emu_guest_* orders in Firestore
  const guestOrder = makeValidOrder(EMU_GUEST_UID);
  await assertSucceeds(guestDb.collection("orders").doc("FC12899").set(guestOrder));
  await assertSucceeds(guestDb.collection("orders").where("userId", "==", EMU_GUEST_UID).get());
});

test("Config, Banners, Coupons, Recipes, Reviews, and Categories: readable by everyone, writable only by owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  // 1. Config / app_config seeded by Alice
  await assertSucceeds(
    aliceDb.collection("config").doc("app_config").set({
      searchChips: ["Chicken", "Mutton", "Fish", "Eggs"],
      referralCode: "",
      storeName: "FRESCUT",
      supportPhone: "",
      minOrderAmount: 0,
      ownerId: ALICE_UID,
    })
  );

  // 2. Banners seeded by Alice
  await assertSucceeds(
    aliceDb.collection("banners").doc("banner_1").set({
      title: "Fresh Meat & Fish",
      subtitle: "100% Fresh, Hygienic & Antibiotic-Free Cuts",
      buttonText: "Order Now",
      imageUrl: "img_hero_farm_to_home_1794299674996",
      active: true,
      order: 1,
      ownerId: ALICE_UID,
    })
  );

  // 3. Coupons seeded by Alice (WELCOME10 with active: false)
  await assertSucceeds(
    aliceDb.collection("coupons").doc("WELCOME10").set({
      code: "WELCOME10",
      discountPercent: 10,
      minOrder: 0,
      active: false,
      ownerId: ALICE_UID,
    })
  );

  // 4. Recipes seeded by Alice
  await assertSucceeds(
    aliceDb.collection("recipes").doc("recipe_1").set({
      title: "Butter Chicken",
      time: "30 mins",
      difficulty: "Easy",
      imageUrl: "img_recipe_butter_chicken_1794299674369",
      active: true,
      order: 1,
      ownerId: ALICE_UID,
    })
  );

  // 5. Reviews created by owner
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").set({
      name: "Rohan",
      text: "Very fresh chicken cuts!",
      rating: 5,
      active: true,
      order: 1,
      ownerId: ALICE_UID,
    })
  );

  // Everyone (Bob and unauthenticated user) can read all of these collections
  await assertSucceeds(bobDb.collection("config").doc("app_config").get());
  await assertSucceeds(unauthDb.collection("config").get());
  await assertSucceeds(bobDb.collection("banners").get());
  await assertSucceeds(unauthDb.collection("banners").get());
  await assertSucceeds(bobDb.collection("coupons").get());
  await assertSucceeds(unauthDb.collection("coupons").get());
  await assertSucceeds(bobDb.collection("recipes").get());
  await assertSucceeds(unauthDb.collection("recipes").get());
  await assertSucceeds(bobDb.collection("reviews").get());
  await assertSucceeds(unauthDb.collection("reviews").get());

  // Bob (non-owner) CANNOT update or delete any of Alice's documents
  await assertFails(
    bobDb.collection("config").doc("app_config").update({ storeName: "HACKED" })
  );
  await assertFails(
    bobDb.collection("banners").doc("banner_1").update({ active: false })
  );
  await assertFails(
    bobDb.collection("coupons").doc("WELCOME10").update({ active: true })
  );
  await assertFails(
    bobDb.collection("recipes").doc("recipe_1").delete()
  );
  await assertFails(
    bobDb.collection("reviews").doc("rev_1").delete()
  );

  // Alice (owner) CAN update her documents
  await assertSucceeds(
    aliceDb.collection("coupons").doc("WELCOME10").update({ active: true })
  );
});
