package com.example.data

import com.example.R
import com.example.ui.theme.PastelChicken
import com.example.ui.theme.PastelEggs
import com.example.ui.theme.PastelFish
import com.example.ui.theme.PastelMarinated
import com.example.ui.theme.PastelMutton
import com.example.ui.theme.PastelOrganMeat
import com.example.ui.theme.PastelReadyToCook
import com.example.ui.theme.PastelSausages
import com.example.ui.theme.PastelValuePacks

object SeedData {

    val initialCategoryBudgetConfigs: List<CategoryBudgetConfigEntity> = listOf(
        CategoryBudgetConfigEntity(category = "Chicken", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Mutton", minBudget = 100, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Fish", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Eggs", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Organ Meat", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Ready to Cook", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Sausages", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Marinated", minBudget = 50, incrementAmount = 10),
        CategoryBudgetConfigEntity(category = "Value Packs", minBudget = 100, incrementAmount = 10)
    )

    val initialRecommendationConfig = RecommendationConfigEntity(
        id = 1,
        isEnabled = true,
        triggerBudgetAmount = 100,
        usePastOrderHistory = true,
        popularRangeMultiplier = 1.0f
    )

    val categories: List<CategoryCardModel> = listOf(
        CategoryCardModel(
            id = "Chicken",
            name = "Chicken",
            itemCountText = "28 Items",
            imageRes = R.drawable.img_cat_chicken,
            backgroundColor = PastelChicken,
            badgeText = "DOUBT-FREE",
            subcategories = listOf("All", "Breast", "Leg", "Wing", "Thigh", "Boneless", "Curry Cut")
        ),
        CategoryCardModel(
            id = "Mutton",
            name = "Mutton",
            itemCountText = "18 Items",
            imageRes = R.drawable.img_cat_mutton,
            backgroundColor = PastelMutton,
            badgeText = "TENDER",
            subcategories = listOf("All", "Curry Cut", "Boneless", "Biryani Cut", "Keema")
        ),
        CategoryCardModel(
            id = "Fish",
            name = "Fish",
            itemCountText = "16 Items",
            imageRes = R.drawable.img_cat_fish,
            backgroundColor = PastelFish,
            badgeText = "FRESH CATCH",
            subcategories = listOf("All", "Freshwater", "Seawater", "Prawns & Crabs", "Boneless Fillets")
        ),
        CategoryCardModel(
            id = "Eggs",
            name = "Eggs",
            itemCountText = "6 Items",
            imageRes = R.drawable.img_cat_eggs,
            backgroundColor = PastelEggs,
            badgeText = "G.O.L.D.",
            subcategories = listOf("All", "Classic", "Cage-Free", "Omega-3")
        ),
        CategoryCardModel(
            id = "Organ Meat",
            name = "Organ Meat",
            itemCountText = "12 Items",
            imageRes = R.drawable.img_cat_organ_meat,
            backgroundColor = PastelOrganMeat,
            subcategories = listOf("All", "Liver", "Gizzard", "Paya & Trotters")
        ),
        CategoryCardModel(
            id = "Ready to Cook",
            name = "Ready to Cook",
            itemCountText = "14 Items",
            imageRes = R.drawable.img_cat_ready_to_cook,
            backgroundColor = PastelReadyToCook,
            badgeText = "READY BITES",
            subcategories = listOf("All", "Skewers", "Kebabs", "Crispy Fry")
        ),
        CategoryCardModel(
            id = "Sausages",
            name = "Sausages",
            itemCountText = "8 Items",
            imageRes = R.drawable.img_cat_sausages,
            backgroundColor = PastelSausages,
            subcategories = listOf("All", "Smoked", "Cold Cuts", "Frankfurters")
        ),
        CategoryCardModel(
            id = "Marinated",
            name = "Marinated",
            itemCountText = "10 Items",
            imageRes = R.drawable.img_cat_marinated,
            backgroundColor = PastelMarinated,
            badgeText = "CHEF CRAFTED",
            subcategories = listOf("All", "Tandoori", "Peri-Peri", "Herb & Garlic")
        ),
        CategoryCardModel(
            id = "Value Packs",
            name = "Value Packs",
            itemCountText = "6 Items",
            imageRes = R.drawable.img_cat_value_packs,
            backgroundColor = PastelValuePacks,
            badgeText = "%",
            subcategories = listOf("All", "Combos", "Protein Packs", "Family Feast")
        )
    )

    val initialProducts: List<ProductEntity> = listOf(
        ProductEntity(
            id = "prod_chicken_breast",
            name = "Chicken Breast Boneless",
            category = "Chicken",
            subcategory = "Breast",
            weightInfo = "500 g",
            piecesInfo = "3-5 Pieces",
            servingsInfo = "Serves 3–4",
            price = 412,
            originalPrice = 453,
            discountPercent = 9,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_breast,
            description = "Skinless, antibiotic-residue-free tender chicken breast fillets trimmed of excess fat. Ideal for grilling, pan-searing, butter chicken, and high-protein meals.",
            isCurrentHit = true,
            isBudgetPick = false,
            rating = 4.9f,
            proteinGrams = 31,
            pricePerKg = 824,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_mutton_curry",
            name = "Mutton Curry Cut",
            category = "Mutton",
            subcategory = "Curry Cut",
            weightInfo = "500 g",
            piecesInfo = "12-16 Pieces",
            servingsInfo = "Serves 4–5",
            price = 520,
            originalPrice = 599,
            discountPercent = 13,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_mutton_curry,
            description = "Pasture-raised young goat meat cut into uniform bone-in and boneless pieces for rich, slow-simmered curries and gravies.",
            isCurrentHit = true,
            isBudgetPick = false,
            rating = 4.8f,
            proteinGrams = 26,
            pricePerKg = 1040,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_chicken_drumstick",
            name = "Chicken Drumstick",
            category = "Chicken",
            subcategory = "Leg",
            weightInfo = "500 g",
            piecesInfo = "5-6 Pieces",
            servingsInfo = "Serves 3–4",
            price = 285,
            originalPrice = 315,
            discountPercent = 10,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_drumstick,
            description = "Juicy skinless bone-in chicken drumsticks with uniform sizing for even roasting, tandoori grilling, or hearty home-style curries.",
            isCurrentHit = true,
            isBudgetPick = true,
            rating = 4.8f,
            proteinGrams = 24,
            pricePerKg = 570,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_rohu_fish",
            name = "Rohu Fish (Fresh)",
            category = "Fish",
            subcategory = "Freshwater",
            weightInfo = "1 kg",
            piecesInfo = "8-10 Pieces",
            servingsInfo = "Serves 4–5",
            price = 220,
            originalPrice = 260,
            discountPercent = 15,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_rohu_fish,
            description = "Freshly caught river Rohu, descaled, gutted, and neatly cut into Bengali curry steaks. Maintained at 0-4°C from harvest to doorstep.",
            isCurrentHit = true,
            isBudgetPick = true,
            rating = 4.7f,
            proteinGrams = 22,
            pricePerKg = 220,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_chicken_thigh",
            name = "Chicken Thigh (Boneless)",
            category = "Chicken",
            subcategory = "Thigh",
            weightInfo = "500 g",
            piecesInfo = "6-8 Pieces",
            servingsInfo = "Serves 3–4",
            price = 299,
            originalPrice = 335,
            discountPercent = 11,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_thigh,
            description = "Succulent dark-meat boneless chicken thighs that stay extra juicy when grilled, baked, or cooked in tikkas and stir-fries.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.9f,
            proteinGrams = 25,
            pricePerKg = 598,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_chicken_wings",
            name = "Chicken Wings",
            category = "Chicken",
            subcategory = "Wing",
            weightInfo = "500 g",
            piecesInfo = "8-10 Pieces",
            servingsInfo = "Serves 3–4",
            price = 249,
            originalPrice = 279,
            discountPercent = 11,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_wings,
            description = "Cleanly trimmed chicken wings with drumettes and flats ready for crispy air-frying, buffalo glazing, or smoky BBQ.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.8f,
            proteinGrams = 23,
            pricePerKg = 498,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_chicken_curry_cut",
            name = "Chicken Curry Cut",
            category = "Chicken",
            subcategory = "Curry Cut",
            weightInfo = "500 g",
            piecesInfo = "12-16 Pieces",
            servingsInfo = "Serves 4–5",
            price = 219,
            originalPrice = 249,
            discountPercent = 12,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_curry_cut,
            description = "Balanced mix of bone-in breast, leg, and wing cuts crafted specifically for flavorful everyday Indian chicken curry.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.8f,
            proteinGrams = 24,
            pricePerKg = 438,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_chicken_boneless_cubes",
            name = "Chicken Boneless Cubes",
            category = "Chicken",
            subcategory = "Boneless",
            weightInfo = "450 g",
            piecesInfo = "12-16 Pieces",
            servingsInfo = "Serves 4",
            price = 356,
            originalPrice = 424,
            discountPercent = 16,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_chicken_breast,
            description = "Bite-sized boneless cubes cut from tender breast and thigh fillets, ideal for kebabs, skewers, butter chicken, and pasta.",
            isCurrentHit = true,
            isBudgetPick = false,
            rating = 4.9f,
            proteinGrams = 29,
            pricePerKg = 791,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_farm_eggs",
            name = "Farm Eggs",
            category = "Eggs",
            subcategory = "Classic",
            weightInfo = "6 Pieces",
            piecesInfo = "6 Pieces",
            servingsInfo = "Serves 3",
            price = 132,
            originalPrice = 150,
            discountPercent = 12,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_cat_eggs,
            description = "Naturally laid, UV-sanitized farm-fresh white and brown eggs with rich golden yolks and zero shell cracks.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.9f,
            proteinGrams = 36,
            pricePerKg = 440,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_tiger_prawns",
            name = "Fresh Tiger Prawns",
            category = "Fish",
            subcategory = "Prawns & Crabs",
            weightInfo = "250 g",
            piecesInfo = "15-20 Pieces",
            servingsInfo = "Serves 2–3",
            price = 349,
            originalPrice = 410,
            discountPercent = 15,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_prod_prawns_seafood_1794299676701,
            description = "Peeled, deveined, and tail-on jumbo tiger prawns with sweet, firm flesh ready for butter-garlic pan searing or coastal prawn curry.",
            isCurrentHit = true,
            isBudgetPick = false,
            rating = 4.9f,
            proteinGrams = 24,
            pricePerKg = 1396,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_organ_liver",
            name = "Fresh Chicken Liver",
            category = "Organ Meat",
            subcategory = "Liver",
            weightInfo = "350 g",
            piecesInfo = "8-10 Pieces",
            servingsInfo = "Serves 2–3",
            price = 129,
            originalPrice = 155,
            discountPercent = 17,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_cat_organ_meat,
            description = "Hygienically cleaned, iron-rich fresh chicken liver with smooth texture for pepper fry, masala roast, or pâté.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.7f,
            proteinGrams = 26,
            pricePerKg = 368,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_ready_seekh",
            name = "Spiced Chicken Seekh Skewers",
            category = "Ready to Cook",
            subcategory = "Skewers",
            weightInfo = "350 g",
            piecesInfo = "6 Skewers",
            servingsInfo = "Serves 3",
            price = 269,
            originalPrice = 315,
            discountPercent = 15,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_cat_ready_to_cook,
            description = "Chef-marinated minced chicken and herb skewers ready to pan-sear or air-fry in just 8 minutes. Zero preservatives.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.8f,
            proteinGrams = 22,
            pricePerKg = 768,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_smoked_sausages",
            name = "Smoked Chicken Sausages",
            category = "Sausages",
            subcategory = "Smoked",
            weightInfo = "250 g",
            piecesInfo = "6 Links",
            servingsInfo = "Serves 2–3",
            price = 199,
            originalPrice = 235,
            discountPercent = 15,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_cat_sausages,
            description = "Beechwood-smoked chicken sausages made with 100% breast & thigh meat, cracked black pepper, and natural herbs.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.8f,
            proteinGrams = 20,
            pricePerKg = 796,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_tandoori_tikka",
            name = "Tandoori Chicken Tikka",
            category = "Marinated",
            subcategory = "Tandoori",
            weightInfo = "450 g",
            piecesInfo = "10-12 Pieces",
            servingsInfo = "Serves 3–4",
            price = 295,
            originalPrice = 345,
            discountPercent = 14,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_cat_marinated,
            description = "Boneless thigh chunks marinated in hung curd, Kashmiri chilli, and roasted garam masala. Oven or pan-grill ready.",
            isCurrentHit = false,
            isBudgetPick = true,
            rating = 4.9f,
            proteinGrams = 27,
            pricePerKg = 656,
            budgetSelectorEnabled = true
        ),
        ProductEntity(
            id = "prod_combo_feast",
            name = "Protein Combo Feast Pack",
            category = "Value Packs",
            subcategory = "Combos",
            weightInfo = "1 kg",
            piecesInfo = "Chicken + Mutton",
            servingsInfo = "Serves 6–8",
            price = 799,
            originalPrice = 950,
            discountPercent = 16,
            deliveryTimeMins = 30,
            imageRes = R.drawable.img_combo_packs_meat,
            description = "500 g Chicken Breast Boneless + 500 g Mutton Curry Cut bundled together for maximum family savings.",
            isCurrentHit = false,
            isBudgetPick = false,
            rating = 4.9f,
            proteinGrams = 57,
            pricePerKg = 799,
            budgetSelectorEnabled = true
        )
    )

    val initialOrders: List<OrderEntity> = listOf(
        OrderEntity(
            orderId = "#FC12847",
            status = "Out for Delivery",
            placedAtText = "Today, 6:10 PM",
            etaText = "Arriving in ~ 18 min",
            itemTotal = 1064,
            deliveryFee = 0,
            packagingCharge = 0,
            totalAmount = 1064,
            rating = 5.0f,
            itemsEncoded = listOf(
                "prod_chicken_breast::Chicken Breast Boneless::500 g::412::1::${R.drawable.img_prod_chicken_breast}",
                "prod_mutton_curry::Mutton Curry Cut::500 g::520::1::${R.drawable.img_prod_mutton_curry}",
                "prod_farm_eggs::Farm Eggs::6 Pieces::132::1::${R.drawable.img_cat_eggs}"
            ).joinToString("||"),
            timelineEncoded = "6:10 PM,6:14 PM,6:20 PM,6:32 PM,",
            createdAtEpoch = 1762780000000L
        ),
        OrderEntity(
            orderId = "#FC12846",
            status = "Processing",
            placedAtText = "Today, 5:42 PM",
            etaText = "Freshly cutting & vacuum sealing",
            itemTotal = 412,
            deliveryFee = 0,
            packagingCharge = 0,
            totalAmount = 412,
            rating = 5.0f,
            itemsEncoded = listOf(
                "prod_chicken_drumstick::Chicken Breast Boneless::500 g::412::1::${R.drawable.img_prod_chicken_drumstick}"
            ).joinToString("||"),
            timelineEncoded = "5:42 PM,5:46 PM,,,",
            createdAtEpoch = 1762770000000L
        ),
        OrderEntity(
            orderId = "#FC12821",
            status = "Delivered",
            placedAtText = "Yesterday, 7:10 PM",
            etaText = "Delivered on time",
            itemTotal = 740,
            deliveryFee = 0,
            packagingCharge = 0,
            totalAmount = 740,
            rating = 5.0f,
            itemsEncoded = listOf(
                "prod_mutton_curry::Mutton Curry Cut::500 g::520::1::${R.drawable.img_prod_mutton_curry}",
                "prod_rohu_fish::Rohu Fish (Fresh)::1 kg::220::1::${R.drawable.img_prod_rohu_fish}"
            ).joinToString("||"),
            timelineEncoded = "7:10 PM,7:14 PM,7:20 PM,7:26 PM,7:38 PM",
            createdAtEpoch = 1762690000000L
        )
    )

    val initialAddresses: List<AddressEntity> = listOf(
        AddressEntity(
            id = 1,
            label = "Home",
            areaName = "Udhepali, Bargarh",
            fullAddress = "Plot 42, Ward 14, Near Main Road, Udhepali, Bargarh, Odisha 768028",
            deliveryMins = 30,
            isSelected = true
        ),
        AddressEntity(
            id = 2,
            label = "Work",
            areaName = "Civil Township, Bargarh",
            fullAddress = "3rd Floor, Apex Plaza, NH-53 Service Road, Bargarh 768028",
            deliveryMins = 25,
            isSelected = false
        )
    )

    val promotionalBanners: List<PromotionalBannerModel> = listOf(
        PromotionalBannerModel(
            id = "banner_farm_to_home",
            title = "Fresh Meat & Fish\nFrom Farm to Home",
            highlights = listOf("Hygienic", "Fresh", "Trusted"),
            ctaText = "Order Now",
            targetCategory = "Chicken",
            imageRes = R.drawable.img_hero_farm_to_home_1794299674996
        ),
        PromotionalBannerModel(
            id = "banner_seafood_catch",
            title = "Coastal Prawns & Fish\n0–4°C Cold Chain",
            highlights = listOf("Descaled", "Cleaned", "30 Min"),
            ctaText = "Shop Fish",
            targetCategory = "Fish",
            imageRes = R.drawable.img_prod_prawns_seafood_1794299676701
        ),
        PromotionalBannerModel(
            id = "banner_combo_feast",
            title = "Weekend Combo Packs\nSave Up to 16% Extra",
            highlights = listOf("Zero Waste", "Vacuum Packed", "Fresh"),
            ctaText = "Explore Packs",
            targetCategory = "Value Packs",
            imageRes = R.drawable.img_combo_packs_meat
        )
    )

    val recipes: List<RecipeCardModel> = listOf(
        RecipeCardModel(
            id = "recipe_butter_chicken",
            title = "Classic Delhi Butter Chicken",
            subtitle = "Made with Chicken Breast Boneless",
            prepTime = "25 mins",
            difficulty = "Easy",
            calories = "380 kcal",
            imageRes = R.drawable.img_recipe_butter_chicken_1794299674369,
            primaryProductId = "prod_chicken_breast",
            cutName = "Chicken Breast Boneless (500 g)",
            cutPrice = 412,
            steps = listOf(
                "Marinate 500 g FRESCUT Chicken Breast Boneless in hung curd, ginger-garlic paste, Kashmiri chilli, and lemon juice for 15 mins.",
                "Pan-sear the chicken pieces in butter on high heat for 4–5 mins until golden brown and juicy.",
                "Simmer in velvety tomato-cashew makhani gravy with kasuri methi and finish with fresh cream."
            )
        ),
        RecipeCardModel(
            id = "recipe_rogan_josh",
            title = "Kashmiri Mutton Rogan Josh",
            subtitle = "Made with Mutton Curry Cut",
            prepTime = "45 mins",
            difficulty = "Medium",
            calories = "440 kcal",
            imageRes = R.drawable.img_recipe_rogan_josh_1794299676591,
            primaryProductId = "prod_mutton_curry",
            cutName = "Mutton Curry Cut (500 g)",
            cutPrice = 520,
            steps = listOf(
                "Sear 500 g FRESCUT Mutton Curry Cut in mustard oil with black cardamom, cloves, and cinnamon until browned.",
                "Whisk in yogurt, fennel powder, dry ginger (saunth), and vibrant Kashmiri लाल mirch.",
                "Slow-cook until the bone-in mutton cuts are fork-tender and the rogan floats to the top."
            )
        ),
        RecipeCardModel(
            id = "recipe_garlic_prawns",
            title = "Lemon Butter Garlic Prawns",
            subtitle = "Made with Fresh Tiger Prawns",
            prepTime = "12 mins",
            difficulty = "Easy",
            calories = "260 kcal",
            imageRes = R.drawable.img_prod_prawns_seafood_1794299676701,
            primaryProductId = "prod_tiger_prawns",
            cutName = "Fresh Tiger Prawns (250 g)",
            cutPrice = 349,
            steps = listOf(
                "Pat dry 250 g FRESCUT deveined Tiger Prawns and season lightly with sea salt and cracked pepper.",
                "Sauté minced garlic and chilli flakes in melted butter for 30 seconds.",
                "Toss prawns for 2 mins per side until pink and finish with fresh lemon juice and parsley."
            )
        )
    )

    val customerReviews: List<CustomerReviewModel> = listOf(
        CustomerReviewModel(
            id = "rev_1",
            customerName = "Ananya Mohanty",
            location = "Udhepali, Bargarh",
            rating = 5.0f,
            orderedCut = "Chicken Breast Boneless",
            reviewText = "Delivered in 22 minutes! Vacuum-sealed temperature was super chilled and the chicken breast fillets were extraordinarily clean and tender.",
            verifiedDate = "Verified Order • Today"
        ),
        CustomerReviewModel(
            id = "rev_2",
            customerName = "Rajeshwar Patel",
            location = "Civil Township, Bargarh",
            rating = 5.0f,
            orderedCut = "Mutton Curry Cut",
            reviewText = "Best mutton quality in town. Uniformly cut pieces with the exact right bone-to-meat ratio. Cooked in 3 whistles and melted in the mouth.",
            verifiedDate = "Verified Order • Yesterday"
        ),
        CustomerReviewModel(
            id = "rev_3",
            customerName = "Subhashree Panda",
            location = "Bargarh Main Road",
            rating = 4.9f,
            orderedCut = "Rohu Fish (Fresh)",
            reviewText = "Zero fishy smell, neatly descaled and cut into proper Bengali steaks. Saving so much morning kitchen prep time with FRESCUT!",
            verifiedDate = "Verified Order • 2 days ago"
        )
    )

    val coupons: List<CouponOfferModel> = listOf(
        CouponOfferModel(
            code = "FRESH20",
            title = "Welcome Rewards • Extra 20% OFF",
            subtitle = "Valid on your first 3 orders above ₹299",
            discountPercent = 20,
            maxDiscount = 250,
            minOrderValue = 299
        ),
        CouponOfferModel(
            code = "FEAST15",
            title = "Flat 15% OFF on Mutton & Seafood",
            subtitle = "Save up to ₹200 on orders above ₹499",
            discountPercent = 15,
            maxDiscount = 200,
            minOrderValue = 499
        ),
        CouponOfferModel(
            code = "PROTEIN10",
            title = "Extra 10% OFF on Chicken & Eggs",
            subtitle = "No minimum order value required",
            discountPercent = 10,
            maxDiscount = 150,
            minOrderValue = 199
        )
    )
}
