package com.example.data

import kotlin.math.roundToInt

data class BudgetRecommendationItem(
    val budgetAmount: Int,
    val estimatedGrams: Int,
    val tagLabel: String
)

object BudgetCalculator {

    /**
     * Formula required by specification:
     * Estimated Quantity (grams) = (Selected Budget / Product Price per KG) * 1000
     */
    fun calculateEstimatedGrams(selectedBudget: Int, pricePerKg: Int): Int {
        if (pricePerKg <= 0 || selectedBudget <= 0) return 0
        return ((selectedBudget.toDouble() / pricePerKg.toDouble()) * 1000.0).roundToInt()
    }

    /**
     * Rounds a raw amount to the nearest valid increment at or above minBudget.
     */
    fun snapToIncrement(amount: Int, minBudget: Int, increment: Int): Int {
        val safeIncrement = increment.coerceAtLeast(1)
        if (amount <= minBudget) return minBudget
        val offset = amount - minBudget
        val steps = (offset.toDouble() / safeIncrement.toDouble()).roundToInt()
        return (minBudget + steps * safeIncrement).coerceAtLeast(minBudget)
    }

    /**
     * Generates exactly 3 dynamic budget recommendations when selectedBudget >= triggerThreshold (default ₹100).
     * Considers:
     * 1. Product category & category minimum/increment
     * 2. Current product price & pricePerKg
     * 3. Customer's previous purchase amounts from real orders in Room DB (when available)
     * 4. Popular purchase budgets & common budget ranges for that product
     */
    fun generateRecommendations(
        product: ProductEntity,
        categoryConfig: CategoryBudgetConfigEntity,
        recommendationConfig: RecommendationConfigEntity,
        pastOrders: List<OrderDomainModel>
    ): List<BudgetRecommendationItem> {
        val minBudget = categoryConfig.minBudget
        val increment = categoryConfig.incrementAmount.coerceAtLeast(10)
        val pricePerKg = product.effectivePricePerKg
        val trigger = recommendationConfig.triggerBudgetAmount

        val candidates = mutableListOf<Pair<Int, String>>()

        // 1. Check customer's previous purchase amounts for this product or category from real orders
        if (recommendationConfig.usePastOrderHistory) {
            val matchingLineAmounts = pastOrders
                .flatMap { it.items }
                .filter {
                    it.productId == product.id ||
                        it.name.contains(product.category, ignoreCase = true)
                }
                .map { it.price * it.quantity }
                .filter { it >= trigger }

            matchingLineAmounts.firstOrNull()?.let { pastAmt ->
                val snapped = snapToIncrement(pastAmt, minBudget, increment).coerceAtLeast(trigger)
                candidates.add(snapped to "Past Order")
            }
        }

        // 2. Category & product price-based popular budgets
        // Baseline popular anchor at trigger (e.g. ₹100)
        val base100 = snapToIncrement(
            maxOf(trigger, minBudget),
            minBudget,
            increment
        )
        candidates.add(base100 to "Popular Pick")

        // Mid-tier budget: ~250g–350g portion or popular step (e.g. ₹150 or ~0.35 kg of product)
        val midTargetRaw = when (product.category.lowercase()) {
            "mutton" -> maxOf(150, (pricePerKg * 0.25).roundToInt().coerceAtMost(250))
            "fish" -> maxOf(150, (pricePerKg * 0.5).roundToInt().coerceAtMost(220))
            "eggs" -> 150
            else -> 150
        }
        val midStep = snapToIncrement(
            (midTargetRaw * recommendationConfig.popularRangeMultiplier).roundToInt(),
            minBudget,
            increment
        ).coerceAtLeast(base100 + increment * 2)
        candidates.add(midStep to "Best Value")

        // Family / Meal pack budget: ~500g portion or ₹200+ range
        val highTargetRaw = when (product.category.lowercase()) {
            "mutton" -> maxOf(200, (pricePerKg * 0.4).roundToInt().coerceAtMost(350))
            "chicken" -> maxOf(200, (product.price / 2).coerceAtLeast(200).coerceAtMost(260))
            else -> 200
        }
        val highStep = snapToIncrement(
            (highTargetRaw * recommendationConfig.popularRangeMultiplier).roundToInt(),
            minBudget,
            increment
        ).coerceAtLeast(midStep + increment * 2)
        candidates.add(highStep to "Meal Pack")

        // Additional fallback steps in case of duplicates
        candidates.add((base100 + 50) to "Regular Cut")
        candidates.add((base100 + 100) to "Family Cut")
        candidates.add((base100 + 150) to "Feast Pack")

        val distinctSorted = candidates
            .distinctBy { it.first }
            .sortedBy { it.first }
            .take(3)

        return distinctSorted.map { (budget, label) ->
            BudgetRecommendationItem(
                budgetAmount = budget,
                estimatedGrams = calculateEstimatedGrams(budget, pricePerKg),
                tagLabel = label
            )
        }
    }
}
