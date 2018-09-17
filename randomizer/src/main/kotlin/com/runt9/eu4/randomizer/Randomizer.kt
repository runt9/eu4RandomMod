package com.runt9.eu4.randomizer

// TODO: Don't assign every province, leave room for colonizing

import com.runt9.eu4.randomizer.model.*
import java.io.File
import kotlin.math.E
import kotlin.math.floor
import kotlin.math.max

val provinceFileRegex = Regex("^([0-9]+)\\s*-?\\s*(.*)\\.txt$")
val countryFileRegex = Regex("^([A-Z]{3})\\s*-?\\s*(.*)\\.txt$")

fun main(args: Array<String>) {
    val provinceAdjacencies = mutableMapOf<Int, List<Int>>()
    File("adjacencies.txt").forEachLine {
        val (id, adjacent) = it.split("=")
        provinceAdjacencies[id.toInt()] = if (adjacent.isBlank()) listOf() else adjacent.split(",").map(String::toInt)
    }

    val provinces = mutableListOf<Province>()
    File("../baseGameStuff/history/provinces/").listFiles().forEach { file ->
        val matches = provinceFileRegex.matchEntire(file.name)
                ?: throw Exception("Failed to match filename ${file.name}")
        val id = matches.groups[1]!!.value.toInt()
        if (!provinceAdjacencies.containsKey(id)) {
            return@forEach
        }

        val name = matches.groups[2]!!.value
        val lines = file.readLines(Charsets.ISO_8859_1)
        // Skip "unknown" culture for now
        val culture = lines.findConfigValue("culture") ?: return@forEach
        val (tax, production, manpower) = generateDevelopment()

        provinces += Province(
                fileName = file.name,
                id = id,
                name = name,
                culture = culture,
                baseTax = tax,
                baseProduction = production,
                baseManpower = manpower
        )
    }

    provinces.forEach { province ->
        province.adjacent.addAll(provinceAdjacencies[province.id]!!.map { adjId -> provinces.find { it.id == adjId }!! })
    }

    val ideasFile = File("./common/ideas/00_country_ideas.txt")

    File("../baseGameStuff/history/countries/").listFiles().forEach { file ->
        if (provinces.none { it.owner == null }) return@forEach

        val matches = countryFileRegex.matchEntire(file.name)
                ?: throw Exception("Failed to match filename ${file.name}")
        val key = matches.groups[1]!!.value
        val name = matches.groups[2]!!.value
        val lines = file.readLines(Charsets.ISO_8859_1)
        // TODO: Find better solution so we can have non-starting tags show up (maybe?)
        val monarchName = lines.findConfigValue("name", true) ?: return@forEach

        val monarch = Monarch(monarchName, (0..6).random(), (0..6).random(), (0..6).random())
        val reform = randomEnumValue<GovernmentReform>()
        val countryProvinces = getCountryProvinces(provinces.filter { it.owner == null })
        val rank = when (countryProvinces.totalDevelopment()) {
            in 0..20 -> 1
            in 20..250 -> 2
            else -> 3
        }

        val adjacentProvinces = countryProvinces.flatMap { it.adjacent }
        // TODO: Religion groups and different weighting by religion/group
        val adjacentReligions = adjacentProvinces.asSequence().filter { it.religion != Religion.UNKNOWN }.map { it.religion }.toList()
        val adjacentTechGroups = adjacentProvinces.mapNotNull { it.owner?.techGroup }
        val cultures = countryProvinces.map { it.culture }
        val primaryCulture = cultures.groupingBy { it }.eachCount().maxBy { it.value }?.key ?: return@forEach

        val country = Country(
                tag = key,
                name = name,
                government = reform.government,
                governmentReform = reform,
                rank = rank,
                monarch = monarch,
                religion = getRandomReligion(adjacentReligions),
                techGroup = randomEnumValue(adjacentTechGroups),
                primaryCulture = primaryCulture,
                acceptedCultures = cultures.asSequence().filter { it != primaryCulture }.map { it }.toSet(),
                capital = countryProvinces.maxBy { it.totalDevelopment() }!!
        )

        country.ideas = enumValues<Idea>().filter { it.canBeUsed(country) }.toList().shuffled().subList(0, 10)

        // TODO: Probably should wait to write out provinces until they're all done for discovered_by
        countryProvinces.forEach { province ->
            province.religion = country.religion
            province.discoveredBy.add(country.techGroup)
            province.owner = country

            countryProvinces.flatMap { it.adjacent }.asSequence().mapNotNull { it.owner?.techGroup }.toSet().forEach { province.discoveredBy.add(it) }
        }

        println("Saving ${country.name}")
        val outFile = File("./history/countries/${file.name}")
        outFile.appendText("government = ${country.government.name.toLowerCase()}\n", Charsets.ISO_8859_1)
        outFile.appendText("add_government_reform = ${country.governmentReform.name.toLowerCase()}\n", Charsets.ISO_8859_1)
        outFile.appendText("government_rank = ${country.rank}\n", Charsets.ISO_8859_1)
        outFile.appendText("technology_group = ${country.techGroup.name.toLowerCase()}\n", Charsets.ISO_8859_1)
        outFile.appendText("religion = ${country.religion.name.toLowerCase()}\n", Charsets.ISO_8859_1)
        outFile.appendText("primary_culture = ${country.primaryCulture}\n", Charsets.ISO_8859_1)
        country.acceptedCultures.forEach {
            outFile.appendText("add_accepted_culture = $it\n", Charsets.ISO_8859_1)
        }
        outFile.appendText("capital = ${country.capital.id}\n", Charsets.ISO_8859_1)
        outFile.appendText("1444.1.1 = {\n", Charsets.ISO_8859_1)
        outFile.appendText("    monarch = {\n", Charsets.ISO_8859_1)
        with(country.monarch) {
            outFile.appendText("        name = \"${this.name}\"\n", Charsets.ISO_8859_1)
            outFile.appendText("        adm = \"${this.admSkill}\"\n", Charsets.ISO_8859_1)
            outFile.appendText("        dip = \"${this.dipSkill}\"\n", Charsets.ISO_8859_1)
            outFile.appendText("        mil = \"${this.milSkill}\"\n", Charsets.ISO_8859_1)
        }
        outFile.appendText("    }\n", Charsets.ISO_8859_1)
        outFile.appendText("}\n", Charsets.ISO_8859_1)

        country.acceptedCultures.forEach {
            outFile.appendText("add_accepted_culture = $it\n", Charsets.ISO_8859_1)
        }

        countryProvinces.forEach(::writeProvince)

        ideasFile.appendText("${country.tag}_ideas = {\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    start = {\n", Charsets.ISO_8859_1)
        ideasFile.appendText("        ${country.ideas[0].name.toLowerCase()} = ${country.ideas[0].value}\n", Charsets.ISO_8859_1)
        ideasFile.appendText("        ${country.ideas[1].name.toLowerCase()} = ${country.ideas[1].value}\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    }\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    bonus = {\n", Charsets.ISO_8859_1)
        ideasFile.appendText("        ${country.ideas[2].name.toLowerCase()} = ${country.ideas[2].value}\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    }\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    trigger = {\n", Charsets.ISO_8859_1)
        ideasFile.appendText("        tag = ${country.tag}\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    }\n", Charsets.ISO_8859_1)
        ideasFile.appendText("    free = yes\n", Charsets.ISO_8859_1)
        country.ideas.subList(3, 10).forEach {
            ideasFile.appendText("    ${it.getFileText()} = {\n", Charsets.ISO_8859_1)
            ideasFile.appendText("        ${it.name.toLowerCase()} = ${it.value}\n", Charsets.ISO_8859_1)
            ideasFile.appendText("    }\n", Charsets.ISO_8859_1)
        }
        ideasFile.appendText("}\n", Charsets.ISO_8859_1)
    }

    provinces.filter { it.owner == null }.forEach {
        writeProvince(it)
    }
}

// Heavily weight towards adjacent religion group and heavier towards adjacent religion
fun getRandomReligion(adjacentReligions: List<Religion>): Religion {
    val finalReligions = enumValues<Religion>().toMutableList()

    (1..20).forEach { i ->
        adjacentReligions.forEach { religion ->
            finalReligions.addAll(religion.group.religions())
            finalReligions.add(religion)
        }
    }
    return finalReligions.random()
}

fun writeProvince(province: Province) {
    val provOutFile = File("./history/provinces/${province.fileName}")
    println(" - Saving ${province.name}")
    if (province.owner != null) {
        with(province.owner!!) {
            provOutFile.appendText("add_core = ${this.tag}\n", Charsets.ISO_8859_1)
            provOutFile.appendText("owner = ${this.tag}\n", Charsets.ISO_8859_1)
            provOutFile.appendText("controller = ${this.tag}\n", Charsets.ISO_8859_1)
        }
    }

    if (province.religion != Religion.UNKNOWN) {
        provOutFile.appendText("religion = ${province.religion.name.toLowerCase()}\n", Charsets.ISO_8859_1)
    }

    provOutFile.appendText("culture = ${province.culture}\n", Charsets.ISO_8859_1)
    provOutFile.appendText("hre = no\n", Charsets.ISO_8859_1)
    provOutFile.appendText("base_tax = ${province.baseTax}\n", Charsets.ISO_8859_1)
    provOutFile.appendText("base_production = ${province.baseProduction}\n", Charsets.ISO_8859_1)
    provOutFile.appendText("base_manpower = ${province.baseManpower}\n", Charsets.ISO_8859_1)
    provOutFile.appendText("capital = \"${province.name}\"\n", Charsets.ISO_8859_1)
    provOutFile.appendText("trade_goods = ${province.tradeGood.name.toLowerCase()}\n", Charsets.ISO_8859_1)

    if (province.centerOfTrade > 0) {
        provOutFile.appendText("center_of_trade = ${province.centerOfTrade}\n", Charsets.ISO_8859_1)
    }

    if (province.fort) {
        provOutFile.appendText("fort_15th = yes\n", Charsets.ISO_8859_1)
    }

    province.discoveredBy.forEach {
        provOutFile.appendText("discovered_by = ${it.name.toLowerCase()}\n", Charsets.ISO_8859_1)
    }
}

val targetDevRange = generateWeightedRange(3..1200) { _, _, value -> gaussian(300.0, 30.0, 80.0, value.toDouble()).toInt() + 3 }
fun getCountryProvinces(provinces: List<Province>): List<Province> {
    val targetDev = targetDevRange.random()
    val filteredProvinces = provinces.filter { it.totalDevelopment() <= targetDev }.shuffled().toMutableList()
    if (filteredProvinces.isEmpty()) return emptyList()
    val outProvinces = mutableListOf(filteredProvinces.removeAt(0))

    while (outProvinces.totalDevelopment() < targetDev) {
        var found = false

        for (i in (0 until filteredProvinces.size)) {
            val province = filteredProvinces[i]
            if (outProvinces.totalDevelopment() + province.totalDevelopment() <= targetDev && province.isAdjacent(*outProvinces.toTypedArray())) {
                outProvinces += filteredProvinces.removeAt(i)
                found = true
                break
            }
        }

        if (!found) break
    }

    return outProvinces
}

fun List<Province>.totalDevelopment() = this.sumBy { it.baseTax + it.baseProduction + it.baseManpower }
fun List<String>.findConfigValue(key: String) = this.findConfigValue(key, false)
fun List<String>.findConfigValue(key: String, fullLine: Boolean): String? {
    return this.find {
        if (fullLine) {
            it.trim().startsWith(key)
        } else {
            it.startsWith(key)
        }
    }?.split('=')
            ?.get(1)
            ?.replace(Regex("#.*$"), "")
            ?.replace("\"", "")
            ?.trim()
}

val totalDev = generateWeightedRange(3..35) { range, _, value -> ((((range.endInclusive.toDouble() + 1) / value) * 100) - 100).toInt() }
fun generateDevelopment(): List<Int> {
    val totalDev = totalDev.random()

    val dev1LowerLimit = max(floor(totalDev * .2).toInt(), 1)
    val dev1UpperLimit = floor(totalDev * .5).toInt()

    val dev1 = (dev1LowerLimit..dev1UpperLimit).random()

    val dev2LowerLimit = max(floor((totalDev - dev1) * .2).toInt(), 1)
    val dev2UpperLimit = max(floor((totalDev - dev1) * .75).toInt(), 1)

    val dev2 = (dev2LowerLimit..dev2UpperLimit).random()
    val dev3 = totalDev - dev1 - dev2

    return arrayListOf(dev1, dev2, dev3).shuffled()
}

fun IntRange.random() = this.shuffled().last()
fun <T> List<T>.random() = this.shuffled().last()
fun <T> Array<T>.random() = this.toList().random()
inline fun <reified T : Enum<T>> randomEnumValue(): T = enumValues<T>().random()
inline fun <reified T : Enum<T>> randomEnumValue(excluding: T): T {
    var out = randomEnumValue<T>()
    while (out == excluding) {
        out = randomEnumValue()
    }

    return out
}

inline fun <reified T : Enum<T>> randomEnumValue(additionalValues: List<T>, excluding: T? = null): T {
    val vals = enumValues<T>()
    vals.toMutableList().addAll(additionalValues)
    return if (excluding == null) randomEnumValue() else randomEnumValue(excluding)
}

// Generates a weighted range, heavily weighted towards the front part of the range
fun generateWeightedRange(range: IntRange, weightLimiter: (IntRange, Int, Int) -> Int): List<Int> {
    val retVal = mutableListOf<Int>()
    range.forEachIndexed { i, value ->
        val limit = weightLimiter(range, i, value)
        (1..limit).forEach {
            retVal += value
        }
    }

    return retVal.toList()
}

fun gaussian(height: Double, center: Double, stdev: Double, x: Double): Double {
    return (height * (Math.pow(E, -(Math.pow(x - center, 2.0) / (Math.pow(stdev, 2.0) * 2.0)))))
}