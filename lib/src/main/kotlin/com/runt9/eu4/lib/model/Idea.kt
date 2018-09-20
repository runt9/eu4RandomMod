package com.runt9.eu4.lib.model

// TODO: Determine coastal and skip naval stuff?
// TODO: Localization prefix
enum class Idea(val value: String, private val prefix: String = "custom_idea", val canBeUsed: (Country) -> Boolean = { true }) {
    ADMINISTRATIVE_EFFICIENCY("0.1", "custom"),
    ADM_TECH_COST_MODIFIER("-0.1", "custom"),
    ADVISOR_COST("-0.15"),
    ADVISOR_POOL("2"),
    AE_IMPACT("-0.1"),
    ARMY_TRADITION("1.0"),
    ARMY_TRADITION_DECAY("-0.01"),
    ARMY_TRADITION_FROM_BATTLE("1", "custom"),
    ARTILLERY_BONUS_VS_FORT("3", "custom"),
    ARTILLERY_COST("-0.2"),
    ARTILLERY_POWER("0.2"),
    AUTO_EXPLORE_ADJACENT_TO_COLONY("yes", "custom", canBeUsed = { it.coastal || it.capital.continent.name in colonizableContinents }),
    AUTONOMY_CHANGE_TIME("-0.25", "custom"),
    BACKROW_ARTILLERY_DAMAGE("0.2", "custom"),
    BLOCKADE_EFFICIENCY("0.2", canBeUsed = { it.coastal }),
    BUILD_COST("-0.2"),
    BUILD_TIME("-0.33", "custom"),
    CAPTURE_SHIP_CHANCE("0.25", "custom", canBeUsed = { it.coastal }),
    CARAVAN_POWER("0.25", "custom"),
    CAVALRY_COST("-0.2"),
    CAVALRY_FLANKING("0.5", "custom"),
    CAVALRY_POWER("0.2"),
    CAV_TO_INF_RATIO("0.5", "custom"),
    COLONISTS("1", canBeUsed = { it.coastal || it.capital.continent.name in colonizableContinents }),
    CORE_CREATION("-0.2"),
    CULTURE_CONVERSION_COST("-0.2"),
    DEFENSIVENESS("0.25"),
    DEVELOPMENT_COST("-0.25"),
    DEVOTION("1", canBeUsed = { it.government == Government.THEOCRACY }),
    DIPLOMATIC_ANNEXATION_COST("-0.2"),
    DIPLOMATIC_REPUTATION("3"),
    DIPLOMATIC_UPKEEP("2"),
    DIPLOMATS("2"),
    DIP_TECH_COST_MODIFIER("-0.1", "custom"),
    DISCIPLINE("0.1"),
    EMBARGO_EFFICIENCY("0.25", canBeUsed = { it.coastal }),
    EMBRACEMENT_COST("-0.25", "custom"),
    ENEMY_CORE_CREATION("0.33"),
    FIRE_DAMAGE("0.2", "custom"),
    FIRE_DAMAGE_RECEIVED("-0.2", "custom"),
    FORT_MAINTENANCE_MODIFIER("-0.33"),
    FREE_LEADER_POOL("1", "custom"),
    GALLEY_COST("-0.2", canBeUsed = { it.coastal }),
    GALLEY_POWER("0.2", canBeUsed = { it.coastal }),
    GARRISON_SIZE("0.25", "custom"),
    GLOBAL_AUTONOMY("-0.1"),
    GLOBAL_COLONIAL_GROWTH("20", canBeUsed = { it.coastal || it.capital.continent.name in colonizableContinents }),
    GLOBAL_FOREIGN_TRADE_POWER("0.2", "custom"),
    GLOBAL_INSTITUTION_SPREAD("0.33", "custom"),
    GLOBAL_MANPOWER_MODIFIER("0.25"),
    GLOBAL_MISSIONARY_STRENGTH("0.2"),
    GLOBAL_NAVAL_ENGAGEMENT_MODIFIER("0.2", "custom", canBeUsed = { it.coastal }),
    GLOBAL_OWN_TRADE_POWER("0.25", "custom"),
    GLOBAL_PROV_TRADE_POWER_MODIFIER("0.25"),
    GLOBAL_REGIMENT_COST("-0.2", "custom"),
    GLOBAL_REGIMENT_RECRUIT_SPEED("-0.25"),
    GLOBAL_SAILORS_MODIFIER("0.33", canBeUsed = { it.coastal }),
    GLOBAL_SHIP_RECRUIT_SPEED("-0.2", canBeUsed = { it.coastal }),
    GLOBAL_SHIP_TRADE_POWER("0.33", "custom", canBeUsed = { it.coastal }),
    GLOBAL_TARIFFS("0.2", canBeUsed = { it.coastal && it.capital.continent.name == "europe" }),
    GLOBAL_TAX_MODIFIER("0.25"),
    GLOBAL_TRADE_GOODS_SIZE_MODIFIER("0.2", "custom"),
    GLOBAL_TRADE_POWER("0.2"),
    GLOBAL_UNREST("-3"),
    HARMONIZATION_SPEED("0.25", "custom", { it.religion == Religion.CONFUCIANISM }),
    HARSH_TREATMENT_COST("-025", "custom"),
    HEAVY_SHIP_COST("-0.2", canBeUsed = { it.coastal }),
    HEAVY_SHIP_POWER("0.2", canBeUsed = { it.coastal }),
    HEIR_CHANCE("1"),
    HORDE_UNITY("1", canBeUsed = { it.governmentReform == GovernmentReform.STEPPE_HORDE }),
    HOSTILE_ATTRITION("2.5"),
    IDEA_COST("-0.1"),
    IMPROVE_RELATION_MODIFIER("0.25", "custom"),
    INFANTRY_COST("-0.2"),
    INFANTRY_POWER("0.2"),
    INFLATION_ACTION_COST("-0.2"),
    INFLATION_REDUCTION("0.2"),
    INSTITUTION_SPREAD_FROM_TRUE_FAITH("0.33", "custom"),
    INTEREST("-2"),
    LAND_ATTRITION("-0.33", "custom"),
    LAND_FORCELIMIT_MODIFIER("0.25"),
    LAND_MAINTENANCE_MODIFIER("-0.25"),
    LAND_MORALE("0.2"),
    LEADER_LAND_FIRE("1"),
    LEADER_LAND_MANUEVER("1"),
    LEADER_LAND_SHOCK("1"),
    LEADER_NAVAL_FIRE("1", canBeUsed = { it.coastal }),
    LEADER_NAVAL_MANUEVER("1", canBeUsed = { it.coastal }),
    LEADER_NAVAL_SHOCK("1", canBeUsed = { it.coastal }),
    LEADER_SIEGE("1"),
    LEGITIMACY("0.5", canBeUsed = { it.government == Government.MONARCHY }),
    LIBERTY_DESIRE_FROM_SUBJECT_DEVELOPMENT("-0.25", "custom"),
    LIGHT_SHIP_COST("-0.2", canBeUsed = { it.coastal }),
    LIGHT_SHIP_POWER("0.2", canBeUsed = { it.coastal }),
    LOOT_AMOUNT("0.33", "custom"),
    MANPOWER_RECOVERY_SPEED("0.2"),
    MAX_ABSOLUTISM("25", "custom"),
    MAX_STATES("10", "custom"),
    MAY_PERFORM_SLAVE_RAID("yes", canBeUsed = { it.coastal }),
    MERCENARY_COST("-0.25", "custom"),
    MERCENARY_DISCIPLINE("0.1", "custom"),
    MERCHANTS("1"),
    MERC_MAINTENANCE_MODIFIER("-0.25"),
    MIL_TECH_COST_MODIFIER("-0.1", "custom"),
    MISSIONARIES("1"),
    MONARCH_ADMIN_POWER("2", "custom", { it.government != Government.REPUBLIC }),
    MONARCH_DIPLOMATIC_POWER("2", "custom", { it.government != Government.REPUBLIC }),
    MONARCH_MILITARY_POWER("2", "custom", { it.government != Government.REPUBLIC }),
    MONTHLY_MILITARIZED_SOCIETY("0.2", "custom", { it.governmentReform == GovernmentReform.PRUSSIAN_MONARCHY }),
    MOVEMENT_SPEED("0.2", "custom"),
    NATIVE_ASSIMILATION("0.5", "custom", canBeUsed = { it.coastal || it.capital.continent.name in colonizableContinents }),
    NATIVE_UPRISING_CHANCE("-1", "custom", canBeUsed = { it.coastal || it.capital.continent.name in colonizableContinents }),
    NAVAL_ATTRITION("-0.33", "custom", canBeUsed = { it.coastal }),
    NAVAL_FORCELIMIT_MODIFIER("0.25", canBeUsed = { it.coastal }),
    NAVAL_MAINTENANCE_MODIFIER("-0.25", canBeUsed = { it.coastal }),
    NAVAL_MORALE("0.2", canBeUsed = { it.coastal }),
    NAVAL_TRADITION_FROM_BATTLE("1", "custom", canBeUsed = { it.coastal }),
    NAVY_TRADITION("1", canBeUsed = { it.coastal }),
    NAVY_TRADITION_DECAY("-0.01", canBeUsed = { it.coastal }),
    NUM_ACCEPTED_CULTURES("3"),
    PAPAL_INFLUENCE("2", "custom", { it.religion == Religion.CATHOLIC }),
    PLACED_MERCHANT_POWER("10", "custom"),
    POSSIBLE_CONDOTTIERI("0.5", "custom"),
    POSSIBLE_MERCENARIES("0.5"),
    PRESTIGE("1"),
    PRESTIGE_DECAY("-0.01"),
    PRESTIGE_FROM_LAND("1"),
    PRESTIGE_FROM_NAVAL("1", canBeUsed = { it.coastal }),
    PRIVATEER_EFFICIENCY("0.33", canBeUsed = { it.coastal }),
    PRODUCTION_EFFICIENCY("0.25"),
    PROVINCE_WARSCORE_COST("-0.25", "custom"),
    RANGE("0.25"),
    REBEL_SUPPORT_EFFICIENCY("0.5", "custom"),
    RECOVER_ARMY_MORALE_SPEED("0.25", "custom"),
    RECOVER_NAVY_MORALE_SPEED("0.25", "custom", canBeUsed = { it.coastal }),
    REDUCED_LIBERTY_DESIRE("20", "custom"),
    REDUCED_LIBERTY_DESIRE_ON_SAME_CONTINENT("20", "custom"),
    REINFORCE_COST_MODIFIER("-0.25", "custom"),
    REINFORCE_SPEED("0.33"),
    RELIGIOUS_UNITY("0.25"),
    REPUBLICAN_TRADITION("1", canBeUsed = { it.government == Government.REPUBLIC }),
    RIVAL_BORDER_FORT_MAINTENANCE("-0.25", "custom"),
    SAILORS_RECOVERY_SPEED("0.25", canBeUsed = { it.coastal }),
    SHIP_DURABILITY("0.25", canBeUsed = { it.coastal }),
    SHOCK_DAMAGE("0.2", "custom"),
    SHOCK_DAMAGE_RECEIVED("-0.2", "custom"),
    SIEGE_ABILITY("0.25"),
    SIEGE_BLOCKADE_PROGRESS("1", "custom"),
    SPY_OFFENCE("0.25"),
    STABILITY_COST_MODIFIER("-0.25"),
    STATE_MAINTENANCE_MODIFIER("-0.25", "custom"),
    SUNK_SHIP_MORALE_HIT_RECIEVED("-0.2", "custom", canBeUsed = { it.coastal }),
    TECHNOLOGY_COST("-0.1"),
    TOLERANCE_HEATHEN("2"),
    TOLERANCE_HERETIC("2"),
    TOLERANCE_OWN("2"),
    TRADE_EFFICIENCY("0.25"),
    TRADE_RANGE_MODIFIER("1", "custom"),
    TRADE_STEERING("0.25"),
    TRANSPORT_COST("-0.2", canBeUsed = { it.coastal }),
    UNJUSTIFIED_DEMANDS("-0.33", "custom"),
    VASSAL_FORCELIMIT_BONUS("2"),
    VASSAL_INCOME("0.25"),
    WAR_EXHAUSTION("-0.2"),
    WAR_EXHAUSTION_COST("-0.25"),
    WARSCORE_COST_VS_OTHER_RELIGION("-0.33", "custom"),
    WAR_TAXES_COST_MODIFIER("-0.5", "custom"),
    YEARLY_ABSOLUTISM("1", "custom"),
    YEARLY_CORRUPTION("-0.25", "custom"),
    YEARLY_HARMONY("1", "custom", { it.religion == Religion.CONFUCIANISM }),
    YEARS_OF_NATIONALISM("-10");

    fun getFileText() = "${this.prefix}_${this.name.toLowerCase()}"
}