package com.runt9.eu4.randomizer

// TODO: Refactor this
// TODO: Don't assign every province, leave room for colonizing, setting colonial regions and trade companies appropriately
// TODO: Mark provinces as "coastal" if they border the sea. Needs to be done in "find adjacent provinces" script
// TODO: Utilize the above to not assign any naval ideas to countries with 0 coastal provinces
// TODO: Maybe a good idea to import countries/provinces/etc into a DB.
//       This means we can separate the import and export logic from each other
// TODO: Play with trade nodes a bit?
// TODO: Decide if we want to have idea values be randomized a bit, too (so provide a range of values and pick one)
// TODO: Really need to fix the "discovered by" problem with provinces.
//       1. Could assign tech groups to continents then base country's tech group off of its capital's continent
//       2. Could make discovered_by be by "country tag" and so it's anyone in the same or adjacent continent/superregion/whatever
// TODO: Add in "Runt" country that's for us to play!
// TODO: Look into other things that can be randomized or just should be modded
//       Estates, religious deities, government reforms, various static modifiers, advisors, (ages!!!), buildings, institutions,
//       Being ahead of time in MIL giving a small bonus, naval doctrines, opinion modifiers (remove modifier for hard, it's dumb),
//       parliament stuff, policies, power projection, trade good prices and acts like gold, professionalism, religions,
//       state edicts (make them actually useful?), subject types (looks like fun!), technologies, trade company and colonial stuff,
//       trade good bonuses?, wargoals, defines, unit types, tech groups
// TODO: Randomize countries' historical ideas

import com.runt9.eu4.lib.model.Country
import com.runt9.eu4.lib.model.GovernmentReform
import com.runt9.eu4.lib.model.Idea
import com.runt9.eu4.lib.model.Monarch
import com.runt9.eu4.lib.model.Province
import com.runt9.eu4.lib.model.Religion
import com.runt9.eu4.lib.model.TradeGood
import com.runt9.eu4.randomizer.writer.CountryIdeasWriter
import com.runt9.eu4.randomizer.writer.CountryWriter
import com.runt9.eu4.randomizer.writer.PricesWriter
import com.runt9.eu4.randomizer.writer.ProvinceWriter
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
                baseManpower = manpower,
                tradeGood = randomEnumValue(TradeGood.UNKNOWN),
                centerOfTrade = when ((1..500).random()) {
                    1 -> 3
                    in (2..10) -> 2
                    in (11..50) -> 1
                    else -> 0
                },
                fort = (1..20).random() == 1
        )
    }

    provinces.forEach { province ->
        province.adjacent.addAll(provinceAdjacencies[province.id]!!.map { adjId -> provinces.find { it.id == adjId }!! })
    }

    val ideasWriter = CountryIdeasWriter()

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
        CountryWriter(file.name).writeObj(country)
        countryProvinces.forEach { ProvinceWriter(it.fileName).writeObj(it) }
        ideasWriter.writeObj(country)
    }

    provinces.filter { it.owner == null }.forEach { ProvinceWriter(it.fileName).writeObj(it) }
    PricesWriter().writeObj(TradeGood.values().map { it.randomize() })
}

// Heavily weight towards adjacent religion group and heavier towards adjacent religion
fun getRandomReligion(adjacentReligions: List<Religion>): Religion {
    val finalReligions = enumValues<Religion>().toMutableList()

    (1..20).forEach { _ ->
        adjacentReligions.forEach { religion ->
            finalReligions.addAll(religion.group.religions())
            finalReligions.add(religion)
        }
    }
    return finalReligions.random()
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
            if (outProvinces.totalDevelopment() + province.totalDevelopment() <= targetDev &&
                    ((outProvinces.size == 1 && province.isAdjacent(*outProvinces.toTypedArray())) ||
                            (outProvinces.size > 1 && province.numAdjacent(*outProvinces.toTypedArray()) > 1))) {
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