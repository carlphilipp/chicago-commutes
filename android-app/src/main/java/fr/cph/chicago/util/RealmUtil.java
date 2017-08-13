package fr.cph.chicago.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public enum RealmUtil {
    ;

    public static void setUpRealm(final Context context) {
        final RealmConfiguration realmConfig = getRealmConfiguration(context);
        Realm.setDefaultConfiguration(realmConfig);
    }


    public static void cleanRealm(final Context context) {
        final RealmConfiguration realmConfig = getRealmConfiguration(context);
        Realm.deleteRealm(realmConfig);
    }

    private static RealmConfiguration getRealmConfiguration(final Context context) {
        // FIXME kotlin
        Realm.init(context);
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new RealmConfiguration
            .Builder()
            .schemaVersion(packageInfo.versionCode)
            .deleteRealmIfMigrationNeeded()
            .build();
    }
}
