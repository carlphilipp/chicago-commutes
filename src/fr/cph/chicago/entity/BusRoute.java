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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

import lombok.Data;

/**
 * Bus route entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public final class BusRoute implements Parcelable, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 0L;
    /**
     * The id
     **/
    private String id;
    /**
     * The name
     **/
    private String name;

    /**
     *
     */
    public BusRoute() {
    }

    private BusRoute(@NonNull final Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }

    private void readFromParcel(@NonNull final Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Parcelable.Creator<BusRoute> CREATOR = new Parcelable.Creator<BusRoute>() {
        public BusRoute createFromParcel(final Parcel in) {
            return new BusRoute(in);
        }

        public BusRoute[] newArray(final int size) {
            return new BusRoute[size];
        }
    };
}
