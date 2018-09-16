package com.runt9.eu4.randomizer

// 1. Config parser
// 3. Load provinces and adjacencies
// 4. Load countries
// 5. Shuffle country list
// 6. Loop through countries, randomizing their stuff and randomly assigning provinces
//    a. Randomize province development on assign. Random 3-40 then randomly assign to 3 types
//    b. Rank 1 has 3-30 dev, rank 2 has 20-500 dev, rank 3 has 250-1000 dev. Weight heavily towards rank 2
//    c. Provinces assigned must be adjacent to a province the country already has assigned
//    d. Religion weighted fairly heavily towards neighboring religion group, slightly weighted towards neighboring religion
//    e. Similarly weight tech groups to adjacent
//    f. Need to properly figure out how to randomize monarchs. Same name/dynasty, just change skills
//    g. Capital is highest dev province
//    h. Primary culture is the culture with the most number of provinces, ties randomized
//    i. Add accepted culture for all other cultures in country
// 7. Write provinces/countries out to mod folder

import com.runt9.eu4.randomizer.model.*
import java.io.File
import kotlin.math.floor
import kotlin.math.max

fun main(args: Array<String>) {
    val provinceFileRegex = Regex("^([0-9]+)\\s*-?\\s*(.*)\\.txt$")
    val countryFileRegex = Regex("^([A-Z]{3})\\s*-?\\s*(.*)\\.txt$")

    val provinceAdjacencies = mutableMapOf<Int, List<Int>>()
    File("adjacencies.txt").forEachLine {
        val (id, adjacent) = it.split("=")
        provinceAdjacencies[id.toInt()] = if (adjacent.isBlank()) listOf() else adjacent.split(",").map(String::toInt)
    }

    val provinces = mutableListOf<Province>()
    File("./history/provinces/").listFiles().forEach { file ->
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

    File("./history/countries/").listFiles().forEach { file ->
        println("Country $file")
        val matches = countryFileRegex.matchEntire(file.name)
                ?: throw Exception("Failed to match filename ${file.name}")
        val key = matches.groups[1]!!.value
        val name = matches.groups[2]!!.value
        val lines = file.readLines(Charsets.ISO_8859_1)
        // TODO: Find better solution so we can have non-starting tags show up (maybe?)
        val monarchName = lines.findConfigValue("name", true)
        if (monarchName == null) {
            println(" --- $name has no monarch")
            return@forEach
        }

        val monarch = Monarch(monarchName, (0..6).random(), (0..6).random(), (0..6).random())
        val reform = randomEnumValue<GovernmentReform>()
        val rank = generateWeightedRange(1..3).random()
        val countryProvinces = getCountryProvinces(rank, provinces.filter { it.owner == null })

        val adjacentProvinces = countryProvinces.flatMap { it.adjacent }
        // TODO: Religion groups and different weighting by religion/group
        val adjacentReligions = adjacentProvinces.asSequence().filter { it.religion != Religion.UNKNOWN }.map { it.religion }.toList()
        val adjacentTechGroups = adjacentProvinces.mapNotNull { it.owner?.techGroup }
        val cultures = countryProvinces.map { it.culture }
        val primaryCulture = cultures.groupingBy { it }.eachCount().maxBy { it.value }?.key
        if (primaryCulture == null) {
            println("--- $name failed to get primary culture: ${cultures.joinToString()} | ${provinces.count { it.owner == null }} | ${countryProvinces.size}")
            return@forEach
        }

        val country = Country(
                tag = key,
                name = name,
                government = reform.government,
                governmentReform = reform,
                rank = rank,
                monarch = monarch,
                religion = randomEnumValue(adjacentReligions, Religion.UNKNOWN),
                techGroup = randomEnumValue(adjacentTechGroups),
                primaryCulture = primaryCulture,
                acceptedCultures = cultures.asSequence().filter { it != primaryCulture }.map { it }.toSet(),
                capital = countryProvinces.maxBy { it.totalDevelopment() }!!
        )

        // TODO: Probably should wait to write out provinces until they're all done for discovered_by
        countryProvinces.forEach { province ->
            province.religion = country.religion
            province.discoveredBy.add(country.techGroup)
            province.owner = country

            countryProvinces.flatMap { it.adjacent }.asSequence().mapNotNull { it.owner?.techGroup }.toSet().forEach { province.discoveredBy.add(it) }
        }

        val outFile = File("./mod/history/countries/${file.name}")
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
    }

    provinces.filter { it.owner == null }.forEach {
        println("Writing unowned province ${it.name}")
        writeProvince(it)
    }
}

fun writeProvince(province: Province) {
    val provOutFile = File("./mod/history/provinces/${province.fileName}")
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

fun getCountryProvinces(rank: Int, provinces: List<Province>): List<Province> {
    provinces.shuffled()
    val outProvinces = mutableListOf(provinces[0])
    val targetDev = when (rank) {
        1 -> generateWeightedRange(3..30).random()
        2 -> generateWeightedRange(20..500).random()
        3 -> generateWeightedRange(250..1000).random()
        else -> throw Exception("Invalid rank $rank")
    }

    var i = 1
    while (outProvinces.totalDevelopment() < targetDev) {
        if (i == provinces.size) break

        val province = provinces[i]
        if (outProvinces.totalDevelopment() + province.totalDevelopment() <= targetDev && province.isAdjacent(*outProvinces.toTypedArray())) {
            outProvinces += province
        }

        i++
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

val totalDev = generateWeightedRange((3..35))
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
fun generateWeightedRange(range: IntRange): List<Int> {
    val retVal = mutableListOf<Int>()
    range.forEach { i ->
        val limit = ((((range.endInclusive.toDouble() + 1) / i) * 100) - 100).toInt()
        (1..limit).forEach {
            retVal += i
        }
    }

    return retVal.toList()
}
