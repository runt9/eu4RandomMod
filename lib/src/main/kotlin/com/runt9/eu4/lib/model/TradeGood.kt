package com.runt9.eu4.lib.model

// TODO: Probably want a "weight" here so things like gold don't have the same chance to spawn as grain, for example
enum class TradeGood(var basePrice: Double, var goldType: Boolean = false) {
    CHINAWARE(3.0),
    CLOTH(3.0),
    COCOA(4.0),
    COFFEE(3.0),
    COPPER(3.0),
    COTTON(3.0),
    DYES(4.0),
    FISH(2.5),
    FUR(2.0),
    GEMS(4.0),
    GLASS(3.0),
    GOLD(0.0, true),
    GRAIN(2.5),
    INCENSE(2.5),
    IRON(3.0),
    IVORY(4.0),
    LIVESTOCK(2.0),
    NAVAL_SUPPLIES(2.0),
    PAPER(3.5),
    SALT(3.0),
    SILK(4.0),
    SLAVES(2.0),
    SPICES(3.0),
    SUGAR(3.0),
    TEA(2.0),
    TOBACCO(3.0),
    TROPICAL_WOOD(2.0),
    WINE(2.5),
    WOOL(2.5),

    UNKNOWN(0.0);

    fun randomize(): TradeGood {
        goldType = (1..20).shuffled().last() == 1
        basePrice = if (goldType) 0.0 else (2..10).shuffled().last() / 2.0
        return this
    }
}