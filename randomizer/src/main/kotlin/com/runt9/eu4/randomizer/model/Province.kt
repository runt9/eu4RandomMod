package com.runt9.eu4.randomizer.model

import com.runt9.eu4.randomizer.random
import com.runt9.eu4.randomizer.randomEnumValue

// TODO: Add extra cores to start the game off with some reconquest options?
data class Province(
        val fileName: String,
        val id: Int,
        val name: String,
        var culture: String,
        val baseTax: Int,
        val baseProduction: Int,
        val baseManpower: Int,
        var owner: Country? = null,
        var religion: Religion = Religion.UNKNOWN,
        val tradeGood: TradeGood = randomEnumValue(TradeGood.UNKNOWN),
        val centerOfTrade: Int = when((1..500).random()) {
            1 -> 3
            in (2..10) -> 2
            in (11..50) -> 1
            else -> 0
        },
        val fort: Boolean = (1..20).random() == 1,

        var discoveredBy: MutableSet<TechGroup> = mutableSetOf(),

        // TODO: The memory overhead here scares me. If we run OOM, change this to id reference.
        val adjacent: MutableSet<Province> = mutableSetOf()
) {
    fun totalDevelopment() = baseTax + baseProduction + baseManpower
    fun isAdjacent(vararg provinces: Province) = provinces.intersect(adjacent).isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (other !is Province) return false
        return this.id == other.id
    }

    override fun hashCode() = this.id.hashCode()
}