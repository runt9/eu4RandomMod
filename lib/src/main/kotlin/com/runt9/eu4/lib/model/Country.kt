package com.runt9.eu4.lib.model

data class Country(
        val tag: String,
        val name: String,
        val government: Government,
        val governmentReform: GovernmentReform,
        val rank: Int,
        val monarch: Monarch,
        val techGroup: TechGroup,
        val religion: Religion,
        val primaryCulture: String,
        val capital: Province,
        var ideas: List<Idea> = listOf(),
        val acceptedCultures: Set<String> = setOf()
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Country) return false
        return this.tag == other.tag
    }

    override fun hashCode() = this.tag.hashCode()
    override fun toString(): String {
        return "$name($tag)"
    }
}