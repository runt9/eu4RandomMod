package com.runt9.eu4.randomizer.writer

import com.runt9.eu4.lib.model.Country

class CountryWriter(countryFile: String) : BaseWriter<Country>("./history/countries/$countryFile") {
    override fun writeObj(obj: Country) {
        writeLn("government = ${obj.government.name.toLowerCase()}")
        writeLn("add_government_reform = ${obj.governmentReform.name.toLowerCase()}")
        writeLn("government_rank = ${obj.rank}")
        writeLn("technology_group = ${obj.techGroup.name.toLowerCase()}")
        writeLn("religion = ${obj.religion.name.toLowerCase()}")
        writeLn("primary_culture = ${obj.primaryCulture}")
        obj.acceptedCultures.forEach {
            writeLn("add_accepted_culture = $it")
        }
        writeLn("capital = ${obj.capital.id}")
        writeLn("1444.1.1 = {")
        writeLn("    monarch = {")
        with(obj.monarch) {
            writeLn("        name = \"${this.name}\"")
            writeLn("        adm = \"${this.admSkill}\"")
            writeLn("        dip = \"${this.dipSkill}\"")
            writeLn("        mil = \"${this.milSkill}\"")
        }
        writeLn("    }")
        writeLn("}")

        obj.acceptedCultures.forEach {
            writeLn("add_accepted_culture = $it")
        }
    }
}