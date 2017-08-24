package fr.cph.chicago.core.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.core.activity.BaseActivity;
import fr.cph.chicago.repository.RealmConfig;
import fr.cph.chicago.service.PreferenceService;
import fr.cph.chicago.util.Util;

@SuppressWarnings("WeakerAccess")
public class SettingsFragment extends AbstractFragment {

    @BindView(R.id.clear_cache)
    LinearLayout clearCache;

    @BindView(R.id.version_number)
    TextView versionNumber;

    private final Util util;
    private final PreferenceService preferenceService;
    private final RealmConfig realmConfig;

    public SettingsFragment() {
        util = Util.INSTANCE;
        preferenceService = PreferenceService.INSTANCE;
        realmConfig = RealmConfig.INSTANCE;
    }

    @NonNull
    public static SettingsFragment newInstance(final int sectionNumber) {
        return (SettingsFragment) fragmentWithBundle(new SettingsFragment(), sectionNumber);
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.trackScreen((App) getActivity().getApplication(), getString(R.string.analytics_settings_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        if (!activity.isFinishing()) {
            setBinder(rootView);
            final String version = "Version " + util.getCurrentVersion(getContext());
            versionNumber.setText(version);
            clearCache.setOnClickListener(view -> {
                final DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            cleanLocalData();
                            restartApp();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                new AlertDialog.Builder(getContext())
                    .setMessage("This is going to:\n\n- Delete all your favorites\n- Clear application cache\n- Restart the application")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();
            });
        }
        return rootView;
    }

    private void restartApp() {
        final Intent intent = new Intent(getContext(), BaseActivity.class);
        final int intentId = new Random().nextInt();
        final PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), intentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
        activity.finish();
    }

    private void cleanLocalData() {
        deleteCache(getContext());
        preferenceService.clearPreferences(getContext());
        realmConfig.cleanRealm(getContext());
    }

    private void deleteCache(final Context context) {
        try {
            final File cacheDirectory = context.getCacheDir();
            deleteRecursiveDirectory(cacheDirectory);
        } catch (Exception ignored) {
        }
    }

    private boolean deleteRecursiveDirectory(final File directory) {
        if (directory != null && directory.isDirectory()) {
            final String[] children = directory.list();
            for (final String child : children) {
                boolean success = deleteRecursiveDirectory(new File(directory, child));
                if (!success) {
                    return false;
                }
            }
            return directory.delete();
        } else
            return directory != null && directory.isFile() && directory.delete();
    }
}
