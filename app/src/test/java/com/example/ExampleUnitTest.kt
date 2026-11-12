package com.example

import com.example.data.BudgetCalculator
import com.example.data.RecommendationConfigEntity
import com.example.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify category minimum budgets and increments match specification`() {
        val configs = SeedData.initialCategoryBudgetConfigs.associateBy { it.category }

        val chicken = configs["Chicken"]
        assertNotNull(chicken)
        assertEquals(50, chicken!!.minBudget)
        assertEquals(10, chicken.incrementAmount)

        val mutton = configs["Mutton"]
        assertNotNull(mutton)
        assertEquals(100, mutton!!.minBudget)
        assertEquals(10, mutton.incrementAmount)

        val fish = configs["Fish"]
        assertNotNull(fish)
        assertEquals(50, fish!!.minBudget)
        assertEquals(10, fish.incrementAmount)

        val eggs = configs["Eggs"]
        assertNotNull(eggs)
        assertEquals(50, eggs!!.minBudget)
        assertEquals(10, eggs.incrementAmount)
    }

    @Test
    fun `verify automatic quantity formula and 192g prompt example`() {
        // Prompt example: Product price ₹260/kg, Selected budget ₹50 -> ~192 grams
        val grams = BudgetCalculator.calculateEstimatedGrams(selectedBudget = 50, pricePerKg = 260)
        assertEquals(192, grams)

        // Increasing budget in ₹10 increments updates estimated quantity automatically
        val gramsAt60 = BudgetCalculator.calculateEstimatedGrams(selectedBudget = 60, pricePerKg = 260)
        assertEquals(231, gramsAt60)

        val gramsAt100 = BudgetCalculator.calculateEstimatedGrams(selectedBudget = 100, pricePerKg = 260)
        assertEquals(385, gramsAt100)
    }

    @Test
    fun `verify plus and minus clamping never goes below category minimum`() {
        val chickenConfig = SeedData.initialCategoryBudgetConfigs.first { it.category == "Chicken" }
        var selectedBudget = chickenConfig.minBudget // Starts at ₹50
        assertEquals(50, selectedBudget)

        // Attempting minus at minimum stays at ₹50
        selectedBudget = (selectedBudget - chickenConfig.incrementAmount).coerceAtLeast(chickenConfig.minBudget)
        assertEquals(50, selectedBudget)

        // Plus increases in ₹10 increments: 60, 70, 80, 90, 100
        repeat(5) {
            selectedBudget += chickenConfig.incrementAmount
        }
        assertEquals(100, selectedBudget)
    }

    @Test
    fun `verify three smart recommendations generated and update estimated quantity`() {
        val recConfig = SeedData.initialRecommendationConfig
        assertEquals(100, recConfig.triggerBudgetAmount)

        val chickenProduct = SeedData.initialProducts.first { it.category == "Chicken" }
        val chickenConfig = SeedData.initialCategoryBudgetConfigs.first { it.category == "Chicken" }

        val recommendations = BudgetCalculator.generateRecommendations(
            product = chickenProduct,
            categoryConfig = chickenConfig,
            recommendationConfig = recConfig,
            pastOrders = emptyList()
        )
        assertEquals(3, recommendations.size)
        // Verify each recommendation has positive budget at or above trigger and matching estimated grams
        recommendations.forEach { rec ->
            assertTrue(rec.budgetAmount >= recConfig.triggerBudgetAmount)
            assertEquals(
                BudgetCalculator.calculateEstimatedGrams(rec.budgetAmount, chickenProduct.effectivePricePerKg),
                rec.estimatedGrams
            )
        }
    }

    @Test
    fun `verify admin dashboard custom recommendation configuration`() {
        val customConfig = RecommendationConfigEntity(
            id = 1,
            isEnabled = true,
            triggerBudgetAmount = 120,
            usePastOrderHistory = true,
            popularRangeMultiplier = 1.25f
        )

        val muttonProduct = SeedData.initialProducts.first { it.category == "Mutton" }
        val muttonConfig = SeedData.initialCategoryBudgetConfigs.first { it.category == "Mutton" }

        val recs = BudgetCalculator.generateRecommendations(
            product = muttonProduct,
            categoryConfig = muttonConfig,
            recommendationConfig = customConfig,
            pastOrders = emptyList()
        )
        assertEquals(3, recs.size)
        assertTrue(recs[0].budgetAmount < recs[1].budgetAmount)
        assertTrue(recs[1].budgetAmount < recs[2].budgetAmount)
    }
}
