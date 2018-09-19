package com.runt9.eu4.lib.model

class Region(val name: String, val areas: MutableList<Area> = mutableListOf()) {
    lateinit var superRegion: SuperRegion
}