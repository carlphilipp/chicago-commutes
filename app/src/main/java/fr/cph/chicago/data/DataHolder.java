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

package fr.cph.chicago.data;

import android.support.annotation.NonNull;

import lombok.Data;

/**
 * Class that hold bus and train data. Singleton
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class DataHolder {

    private static DataHolder DATA_HOLDER;
    private TrainData trainData;
    private BusData busData;

    /**
     * Private constructor
     */
    private DataHolder() {
    }

    /**
     * Get instance of the class. Singleton
     */
    @NonNull
    public static DataHolder getInstance() {
        if (DATA_HOLDER == null) {
            DATA_HOLDER = new DataHolder();
        }
        return DATA_HOLDER;
    }
}
