package fr.cph.chicago.util

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

object RealmUtil {

    fun setUpRealm(context: Context) {
        val realmConfig = getRealmConfiguration(context)
        Realm.setDefaultConfiguration(realmConfig)
    }


    fun cleanRealm(context: Context) {
        val realmConfig = getRealmConfiguration(context)
        Realm.deleteRealm(realmConfig)
    }

    private fun getRealmConfiguration(context: Context): RealmConfiguration {
        Realm.init(context)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
        return RealmConfiguration.Builder()
            .schemaVersion(packageInfo.versionCode.toLong())
            .deleteRealmIfMigrationNeeded()
            .build()
    }
}
