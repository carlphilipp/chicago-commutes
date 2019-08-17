package fr.cph.chicago.core.model.dto

import fr.cph.chicago.repository.PrefType

data class PreferencesDTO(val preferences: Set<PreferenceDTO> = mutableSetOf()) {

    @Suppress("UNCHECKED_CAST")
    fun addPreference(prefType: PrefType, data: Any) {
        val set = if (data is Set<*>) {
            data as Set<String>
        } else {
            (data as Map<String, *>).entries.map { entry -> "${entry.key} -> ${entry.value}" }.toSet()
        }
        (preferences as MutableSet<PreferenceDTO>).add(PreferenceDTO(prefType, set))
    }
}

data class PreferenceDTO(val name: PrefType, val favorites: Set<String>)
