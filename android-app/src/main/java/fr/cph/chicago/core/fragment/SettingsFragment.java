package fr.cph.chicago.core.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.BaseActivity;
import fr.cph.chicago.core.activity.MainActivity;
import fr.cph.chicago.data.Preferences;
import fr.cph.chicago.data.PreferencesImpl;
import fr.cph.chicago.util.RealmUtil;
import fr.cph.chicago.util.Util;

public class SettingsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    @BindView(R.id.clear_cache)
    LinearLayout clearCache;

    @BindView(R.id.version_number)
    TextView versionNumber;

    private Preferences preferences;
    private MainActivity activity;
    private Unbinder unbinder;

    @NonNull
    public static SettingsFragment newInstance(final int sectionNumber) {
        final SettingsFragment fragment = new SettingsFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (MainActivity) context : null;
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferencesImpl.INSTANCE;
        Util.trackScreen(getContext(), getString(R.string.analytics_settings_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        if (!activity.isFinishing()) {
            unbinder = ButterKnife.bind(this, rootView);
            final String version = "Version " + Util.getCurrentVersion(getContext());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
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
        preferences.clearPreferences(getContext());
        RealmUtil.cleanRealm(getContext());
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