package fr.cph.chicago.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import lombok.SneakyThrows;

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

    @SneakyThrows(PackageManager.NameNotFoundException.class)
    private static RealmConfiguration getRealmConfiguration(final Context context) {
        Realm.init(context);
        final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return new RealmConfiguration
            .Builder()
            .schemaVersion(packageInfo.versionCode)
            .deleteRealmIfMigrationNeeded()
            .build();
    }
}
