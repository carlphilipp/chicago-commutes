/**
 * Copyright 2020 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
