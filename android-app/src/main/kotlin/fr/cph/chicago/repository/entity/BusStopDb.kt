/**
 * Copyright 2021 Carl-Philipp Harmant
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

package fr.cph.chicago.repository.entity

import fr.cph.chicago.core.model.BusStop
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class BusStopDb(
    @PrimaryKey
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    // Realm decided that position must be nullable... https://github.com/realm/realm-java/commit/39bb67cef10b62456649fdd7cf5710bd3361c29a
    var position: PositionDb? = PositionDb()) : RealmObject() {

    constructor(busStop: BusStop) : this(
        id = busStop.id.toInt(),
        name = busStop.name,
        description = busStop.description,
        position = PositionDb(busStop.position.latitude, busStop.position.longitude)
    )
}
