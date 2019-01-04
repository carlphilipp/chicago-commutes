/**
 * Copyright 2019 Carl-Philipp Harmant
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

package fr.cph.chicago.repository

import android.os.Build
import fr.cph.chicago.core.App
import io.realm.Realm
import io.realm.RealmConfiguration

object RealmConfig {

    fun setUpRealm() {
        Realm.init(App.instance.applicationContext)
        Realm.setDefaultConfiguration(getRealmConfiguration())
    }

    fun cleanRealm() {
        Realm.deleteRealm(getRealmConfiguration())
    }

    private fun getRealmConfiguration(): RealmConfiguration {
        val context = App.instance.applicationContext
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
        return if (Build.VERSION.SDK_INT >= 28) {
            RealmConfiguration.Builder()
                .schemaVersion(packageInfo.longVersionCode)
                .deleteRealmIfMigrationNeeded()
                .build()
        } else {
            RealmConfiguration.Builder()
                .schemaVersion(packageInfo.versionCode.toLong())
                .deleteRealmIfMigrationNeeded()
                .build()
        }
    }
}
