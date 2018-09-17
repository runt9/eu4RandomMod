package com.runt9.eu4.lib.model

enum class ReligionGroup {
    CHRISTIAN,
    MUSLIM,
    EASTERN,
    DHARMIC,
    PAGAN,
    JEWISH_GROUP,
    ZOROASTRIAN_GROUP,

    NONE;

    fun religions() = Religion.values().filter { it.group == this }
}