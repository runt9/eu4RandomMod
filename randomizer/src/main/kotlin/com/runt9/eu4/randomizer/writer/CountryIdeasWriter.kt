package com.runt9.eu4.randomizer.writer

import com.runt9.eu4.lib.model.Country

class CountryIdeasWriter : BaseWriter<Country>("./common/ideas/00_country_ideas.txt") {
    override fun writeObj(obj: Country) {
        val ideas = obj.ideas

        writeLn("${obj.tag}_ideas = {")
        writeLn("    start = {")
        writeLn("        ${ideas[0].name.toLowerCase()} = ${ideas[0].value}")
        writeLn("        ${ideas[1].name.toLowerCase()} = ${ideas[1].value}")
        writeLn("    }")
        writeLn("    bonus = {")
        writeLn("        ${ideas[2].name.toLowerCase()} = ${ideas[2].value}")
        writeLn("    }")
        writeLn("    trigger = {")
        writeLn("        tag = ${obj.tag}")
        writeLn("    }")
        writeLn("    free = yes")
        ideas.subList(3, 10).forEach {
            writeLn("    ${it.getFileText()} = {")
            writeLn("        ${it.name.toLowerCase()} = ${it.value}")
            writeLn("    }")
        }
        writeLn("}")
    }
}