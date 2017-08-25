package fr.cph.chicago.repository

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
        return RealmConfiguration.Builder()
            .schemaVersion(packageInfo.versionCode.toLong())
            .deleteRealmIfMigrationNeeded()
            .build()
    }
}
