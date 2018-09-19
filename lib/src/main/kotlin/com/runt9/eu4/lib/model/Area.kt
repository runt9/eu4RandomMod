package com.runt9.eu4.lib.model

data class Area(val name: String, val provinces: MutableList<Province> = mutableListOf()) {
    lateinit var region: Region
}