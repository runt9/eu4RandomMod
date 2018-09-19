package com.runt9.eu4.randomizer.writer

import com.runt9.eu4.lib.model.Province
import com.runt9.eu4.lib.model.Religion

class ProvinceWriter(provinceFile: String) : BaseWriter<Province>("./history/provinces/$provinceFile") {
    override fun writeObj(obj: Province) {
        if (obj.owner != null) {
            with(obj.owner!!) {
                writeLn("add_core = ${this.tag}")
                writeLn("owner = ${this.tag}")
                writeLn("controller = ${this.tag}")
            }
        }

        if (obj.religion != Religion.UNKNOWN) {
            writeLn("religion = ${obj.religion.name.toLowerCase()}")
        }

        writeLn("culture = ${obj.culture}")
        writeLn("hre = no")
        writeLn("base_tax = ${obj.baseTax}")
        writeLn("base_production = ${obj.baseProduction}")
        writeLn("base_manpower = ${obj.baseManpower}")
        writeLn("capital = \"${obj.name}\"")
        writeLn("trade_goods = ${obj.tradeGood.name.toLowerCase()}")

        if (obj.centerOfTrade > 0) {
            writeLn("center_of_trade = ${obj.centerOfTrade}")
        }

        if (obj.fort) {
            writeLn("fort_15th = yes")
        }

        obj.discoveredBy.forEach { writeLn("discovered_by = ${it.tag}") }
    }
}