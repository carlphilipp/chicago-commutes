package fr.cph.chicago.core.model

enum class Theme(val key: String, val description: String) {
    AUTO("Auto", "System default"),
    LIGHT("Light", "Light"),
    DARK("Dark", "Dark ");

    companion object {
        fun convert(value: String): Theme {
            return values().find { theme -> theme.key == value } ?: AUTO
        }
    }
}
