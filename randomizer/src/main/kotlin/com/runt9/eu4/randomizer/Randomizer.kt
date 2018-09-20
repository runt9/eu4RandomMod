package com.runt9.eu4.randomizer

// TODO: Refactor this
// TODO: Play with trade nodes a bit?
// TODO: Decide if we want to have idea values be randomized a bit, too (so provide a range of values and pick one)
// TODO: Look into other things that can be randomized or just should be modded
//       Estates, religious deities, government reforms, advisors, (ages!!!), buildings, institutions,
//       naval doctrines, opinion modifiers (remove modifier for hard, it's dumb),
//       parliament stuff, policies, power projection, professionalism, religions,
//       state edicts (make them actually useful?), subject types (looks like fun!), technologies, trade company and colonial stuff,
//       trade good bonuses?, wargoals, unit types, tech groups
// TODO: Randomize countries' historical ideas
// TODO: Spit out sea provinces and set their discovered_by, too

import com.runt9.eu4.lib.model.Area
import com.runt9.eu4.lib.model.Continent
import com.runt9.eu4.lib.model.Country
import com.runt9.eu4.lib.model.GovernmentReform
import com.runt9.eu4.lib.model.Idea
import com.runt9.eu4.lib.model.Monarch
import com.runt9.eu4.lib.model.Province
import com.runt9.eu4.lib.model.Region
import com.runt9.eu4.lib.model.Religion
import com.runt9.eu4.lib.model.SuperRegion
import com.runt9.eu4.lib.model.TechGroup
import com.runt9.eu4.lib.model.TradeGood
import com.runt9.eu4.lib.model.colonizableContinents
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
    val continents = getContinents()
    val areas = getAreas()
    val regions = getRegions(areas.keys.toList())
    val superRegions = getSuperRegions(regions)

    val provinceAdjacencies = mutableMapOf<Int, List<Int>>()
    val coastalProvinces = mutableListOf<Int>()
    File("adjacencies.txt").forEachLine {
        val (id, coastal, adjacent) = it.split("=")
        provinceAdjacencies[id.toInt()] = if (adjacent.isBlank()) listOf() else adjacent.split(",").map(String::toInt)
        if (coastal.toBoolean()) {
            coastalProvinces.add(id.toInt())
        }
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
        val area = areas.toList().find { it.second.contains(id) }!!.first
        val continent = continents.toList().find { it.second.contains(id) }!!.first
        val province = Province(
                fileName = file.name,
                id = id,
                name = name,
                culture = culture,
                baseTax = tax,
                baseProduction = production,
                baseManpower = manpower,
                coastal = id in coastalProvinces,
                area = area,
                continent = continent,
                tradeGood = randomEnumValue(TradeGood.UNKNOWN),
                canBeAssigned = if (continent.name in colonizableContinents) (1..3).random() == 1 else true,
                centerOfTrade = when ((1..500).random()) {
                    1 -> 3
                    in (2..10) -> 2
                    in (11..50) -> 1
                    else -> 0
                },
                fort = (1..20).random() == 1
        )

        provinces += province
        area.provinces += province
        continent.provinces += province
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

        val monarch = Monarch(monarchName, (0..10).random(), (0..10).random(), (0..10).random())
        val reform = randomEnumValue<GovernmentReform>()
        val countryProvinces = getCountryProvinces(provinces.filter { it.owner == null && it.canBeAssigned })
        val rank = when (countryProvinces.totalDevelopment()) {
            in 0..20 -> 1
            in 20..250 -> 2
            else -> 3
        }

        val adjacentProvinces = countryProvinces.flatMap { it.adjacent }
        val adjacentReligions = adjacentProvinces.asSequence().filter { it.religion != Religion.UNKNOWN }.map { it.religion }.toList()
        val cultures = countryProvinces.map { it.culture }
        val primaryCulture = cultures.groupingBy { it }.eachCount().maxBy { it.value }?.key ?: return@forEach
        val capital = countryProvinces.maxBy { it.totalDevelopment() }!!

        val country = Country(
                tag = key,
                name = name,
                government = reform.government,
                governmentReform = reform,
                rank = rank,
                monarch = monarch,
                religion = getRandomReligion(adjacentReligions),
                techGroup = capital.area.region.superRegion.techGroup,
                primaryCulture = primaryCulture,
                coastal = countryProvinces.any(Province::coastal),
                acceptedCultures = cultures.asSequence().filter { it != primaryCulture }.map { it }.toSet(),
                capital = capital
        )

        country.ideas = enumValues<Idea>().filter { it.canBeUsed(country) }.toList().shuffled().subList(0, 10)

        countryProvinces.forEach { province ->
            province.religion = country.religion
            province.owner = country
        }

        println("Saving $country")
        CountryWriter(file.name).writeObj(country)
        ideasWriter.writeObj(country)
    }

    provinces.forEach { province ->
        // TODO: This is not perfect, like eastern europe shouldn't be able to see all the way to the pacific, so should probably be
        //       region-based rather than superregion, but oh well, it works fine for now
        val superRegionsDiscovered = when (province.area.region.superRegion.name) {
            "india_superregion" -> superRegions.filter { it.name in listOf("india_superregion", "east_indies_superregion", "china_superregion", "tartary_superregion", "persia_superregion") }
            "east_indies_superregion" -> superRegions.filter { it.name in listOf("india_superregion", "east_indies_superregion", "china_superregion", "tartary_superregion", "oceania_superregion", "far_east_superregion") }
            "oceania_superregion" -> superRegions.filter { it.name in listOf("east_indies_superregion", "oceania_superregion") }
            "china_superregion" -> superRegions.filter { it.name in listOf("india_superregion", "east_indies_superregion", "china_superregion", "tartary_superregion", "far_east_superregion") }
            "europe_superregion" -> superRegions.filter { it.name in listOf("europe_superregion", "eastern_europe_superregion") }
            "eastern_europe_superregion", "north_european_sea_superregion" -> superRegions.filter { it.name in listOf("europe_superregion", "eastern_europe_superregion", "near_east_superregion", "tartary_superregion", "persia_superregion") }
            "tartary_superregion" -> superRegions.filter { it.name in listOf("india_superregion", "east_indies_superregion", "china_superregion", "tartary_superregion", "persia_superregion", "far_east_superregion", "eastern_europe_superregion", "near_east_superregion") }
            "far_east_superregion" -> superRegions.filter { it.name in listOf("east_indies_superregion", "china_superregion", "tartary_superregion", "far_east_superregion") }
            "africa_superregion" -> superRegions.filter { it.name in listOf("africa_superregion", "near_east_superregion") }
            "south_america_superregion" -> superRegions.filter { it.name in listOf("south_america_superregion", "central_america_superregion") }
            "north_america_superregion" -> superRegions.filter { it.name in listOf("north_america_superregion", "central_america_superregion") }
            "central_america_superregion" -> superRegions.filter { it.name in listOf("north_america_superregion", "central_america_superregion", "south_america_superregion") }
            "near_east_superregion" -> superRegions.filter { it.name in listOf("africa_superregion", "near_east_superregion", "persia_superregion", "eastern_europe_superregion", "tartary_superregion") }
            "persia_superregion" -> superRegions.filter { it.name in listOf("near_east_superregion", "persia_superregion", "tartary_superregion", "india_superregion") }
            else -> throw Exception("Failed to get superregion for ${province.area.region.superRegion.name}")
        }
        province.discoveredBy.addAll(superRegionsDiscovered.map(SuperRegion::techGroup))

        println("Saving $province")
        ProvinceWriter(province.fileName).writeObj(province)
    }

    PricesWriter().writeObj(TradeGood.values().map { it.randomize() })
}

fun getContinents(): MutableMap<Continent, MutableList<Int>> {
    val continents = mutableMapOf<Continent, MutableList<Int>>()
    var continent: Continent? = null
    File("../baseGameStuff/map/continent.txt").forEachLine { line ->
        val trimmedLine = line.trim().replace(Regex("\\s*#.*$"), "")
        if (trimmedLine.startsWith('}') || trimmedLine.isBlank()) return@forEachLine

        if (trimmedLine.contains(Regex("^[a-z_]+"))) {
            val continentName = trimmedLine.split(' ')[0]
            continent = Continent(continentName)
            continents[continent!!] = mutableListOf()
            return@forEachLine
        }

        continents[continent]!!.addAll(trimmedLine.split(Regex("\\s+")).map { it.toInt() })
    }
    
    return continents
}

fun getAreas(): MutableMap<Area, MutableList<Int>> {
    val areas = mutableMapOf<Area, MutableList<Int>>()
    var area: Area? = null
    File("../baseGameStuff/map/area.txt").forEachLine { line ->
        val trimmedLine = line.trim().replace(Regex("\\s*#.*$"), "")
        if (trimmedLine.isBlank() || trimmedLine.startsWith('}') || trimmedLine.startsWith("color ")) return@forEachLine

        if (trimmedLine.contains(Regex("^[a-z_]+"))) {
            val areaName = trimmedLine.split(' ')[0]
            area = Area(areaName)
            areas[area!!] = mutableListOf()
            return@forEachLine
        }

        areas[area]!!.addAll(trimmedLine.split(Regex("\\s+")).map { it.toInt() })
    }
    
    return areas
}

fun getRegions(areas: List<Area>): List<Region> {
    val regions = mutableListOf<Region>()
    var region: Region? = null
    File("../baseGameStuff/map/region.txt").forEachLine { line ->
        val trimmedLine = line.trim().replace(Regex("\\s*#.*$"), "")
        if (trimmedLine.isBlank() ||
                trimmedLine.startsWith('}') ||
                trimmedLine.startsWith("areas ") ||
                trimmedLine.startsWith("color ") ||
                trimmedLine.startsWith("monsoon ") ||
                trimmedLine.startsWith("00.")) return@forEachLine

        if (trimmedLine.contains(Regex("^[a-z_]+\\s*="))) {
            val regionName = trimmedLine.split(' ')[0]
            region = Region(regionName)
            regions.add(region!!)
            return@forEachLine
        }

        val area = areas.find { it.name == trimmedLine }!!
        area.region = region!!
        region!!.areas.add(area)
    }
    
    return regions
}

fun getSuperRegions(regions: List<Region>): List<SuperRegion> {
    val techGroups = TechGroup.values().toMutableList().shuffled().toMutableList()
    val superRegions = mutableListOf<SuperRegion>()
    var superRegion: SuperRegion? = null
    File("../baseGameStuff/map/superregion.txt").forEachLine { line ->
        val trimmedLine = line.trim().replace(Regex("\\s*#.*$"), "")
        if (trimmedLine.startsWith('}') || trimmedLine.isBlank() || trimmedLine.contains("sea_superregion") || trimmedLine.contains("new_world")) return@forEachLine

        if (trimmedLine.contains(Regex("^[a-z_]+\\s*="))) {
            val superRegionName = trimmedLine.split(' ')[0]
            superRegion = SuperRegion(superRegionName, techGroups.removeAt(0))
            superRegions.add(superRegion!!)
            return@forEachLine
        }

        val region = regions.find { it.name == trimmedLine }!!
        region.superRegion = superRegion!!
        superRegion!!.regions.add(region)
    }

    return superRegions
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