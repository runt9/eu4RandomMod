package com.runt9.eu4.lib.model

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
        val tradeGood: TradeGood,
        val centerOfTrade: Int,
        val fort: Boolean,
        var religion: Religion = Religion.UNKNOWN,
        var discoveredBy: MutableSet<TechGroup> = mutableSetOf(),
        val adjacent: MutableSet<Province> = mutableSetOf()
) {
    fun totalDevelopment() = baseTax + baseProduction + baseManpower
    fun isAdjacent(vararg provinces: Province) = provinces.intersect(adjacent).isNotEmpty()
    fun numAdjacent(vararg provinces: Province) = provinces.intersect(adjacent).size

    override fun equals(other: Any?): Boolean {
        if (other !is Province) return false
        return this.id == other.id
    }

    override fun hashCode() = this.id.hashCode()
    override fun toString(): String {
        return "$name($id)"
    }
}