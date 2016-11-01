/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.entity;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.cph.chicago.entity.enumeration.BusDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Bus directions entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@AllArgsConstructor
@Builder
@Data
public final class BusDirections {
    private String id;
    private List<BusDirection> lBusDirection;

    public final void addBusDirection(@NonNull final BusDirection dir) {
        if (lBusDirection == null) {
            lBusDirection = new ArrayList<>();
        }
        if (!lBusDirection.contains(dir)) {
            lBusDirection.add(dir);
        }
    }
}
