package com.runt9.eu4.lib.model

val colonizableContinents = listOf("north_america", "south_america", "africa")
class Continent(val name: String, val provinces: MutableList<Province> = mutableListOf())