package com.runt9.eu4.randomizer.model

enum class Religion(val group: ReligionGroup) {
    ANIMISM(ReligionGroup.PAGAN),
    BUDDHISM(ReligionGroup.EASTERN),
    CATHOLIC(ReligionGroup.CHRISTIAN),
    CONFUCIANISM(ReligionGroup.EASTERN),
    COPTIC(ReligionGroup.CHRISTIAN),
    HINDUISM(ReligionGroup.DHARMIC),
    IBADI(ReligionGroup.MUSLIM),
    INTI(ReligionGroup.PAGAN),
    JEWISH(ReligionGroup.JEWISH_GROUP),
    MAHAYANA(ReligionGroup.EASTERN),
    MESOAMERICAN_RELIGION(ReligionGroup.PAGAN),
    NAHUATL(ReligionGroup.PAGAN),
    ORTHODOX(ReligionGroup.CHRISTIAN),
    SHAMANISM(ReligionGroup.PAGAN),
    SHIITE(ReligionGroup.MUSLIM),
    SHINTO(ReligionGroup.EASTERN),
    SUNNI(ReligionGroup.MUSLIM),
    TENGRI_PAGAN_REFORMED(ReligionGroup.PAGAN),
    TOTEMISM(ReligionGroup.PAGAN),
    VAJRAYANA(ReligionGroup.EASTERN),
    ZOROASTRIAN(ReligionGroup.ZOROASTRIAN_GROUP),

    UNKNOWN(ReligionGroup.NONE)
}