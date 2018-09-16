package com.runt9.eu4.randomizer.model

enum class GovernmentReform(val government: Government) {
    AUTOCRACY_REFORM(Government.MONARCHY),
    ELECTIVE_MONARCHY(Government.MONARCHY),
    ENGLISH_MONARCHY(Government.MONARCHY),
    FEUDALISM_REFORM(Government.MONARCHY),
    FEUDAL_THEOCRACY(Government.MONARCHY),
    GOND_KINGDOM(Government.TRIBAL),
    GRAND_DUCHY_REFORM(Government.MONARCHY),
    INDIAN_SULTANATE_REFORM(Government.MONARCHY),
    IQTA(Government.MONARCHY),
    LEADING_CLERGY_REFORM(Government.THEOCRACY),
    MAMLUK_GOVERNMENT(Government.MONARCHY),
    MANDALA_REFORM(Government.MONARCHY),
    MERCHANTS_REFORM(Government.REPUBLIC),
    MONASTIC_ORDER_REFORM(Government.THEOCRACY),
    NAYANKARA_REFORM(Government.MONARCHY),
    NOBLE_ELITE_REFORM(Government.MONARCHY),
    OLIGARCHY_REFORM(Government.REPUBLIC),
    OTTOMAN_GOVERNMENT(Government.MONARCHY),
    PEASANTS_REPUBLIC(Government.REPUBLIC),
    PLUTOCRATIC_REFORM(Government.MONARCHY),
    PRESIDENTIAL_DESPOT_REFORM(Government.REPUBLIC),
    PRINCIPALITY(Government.MONARCHY),
    PRUSSIAN_MONARCHY(Government.MONARCHY),
    RAJPUT_KINGDOM(Government.MONARCHY),
    SIBERIAN_TRIBE(Government.TRIBAL),
    STEPPE_HORDE(Government.TRIBAL),
    TRIBAL_DESPOTISM(Government.TRIBAL),
    TRIBAL_FEDERATION(Government.TRIBAL),
    TRIBAL_KINGDOM(Government.TRIBAL),
    TSARDOM(Government.MONARCHY),
    VECHE_REPUBLIC(Government.REPUBLIC)
}