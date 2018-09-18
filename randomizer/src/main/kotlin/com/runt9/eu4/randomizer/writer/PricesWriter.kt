package com.runt9.eu4.randomizer.writer

import com.runt9.eu4.lib.model.TradeGood

class PricesWriter : BaseWriter<List<TradeGood>>("./common/prices/00_prices.txt") {
    override fun writeObj(obj: List<TradeGood>) {
        obj.forEach {
            writeLn("${it.name.toLowerCase()} = {")
            writeLn("    base_price = ${it.basePrice}")
            if (it.goldType) {
                writeLn("    goldtype = yes")
            }
            writeLn("}")
        }
    }
}