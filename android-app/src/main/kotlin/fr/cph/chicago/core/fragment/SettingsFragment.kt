package fr.cph.chicago.core.fragment

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import java.io.File
import java.util.*

class SettingsFragment : AbstractFragment() {

    @BindView(R.id.clear_cache)
    lateinit var clearCache: LinearLayout

    @BindView(R.id.version_number)
    lateinit var versionNumber: TextView

    private val util: Util = Util
    private val preferenceService: PreferenceService = PreferenceService
    private val realmConfig: RealmConfig = RealmConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        util.trackScreen(getString(R.string.analytics_settings_fragment))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        if (!mainActivity!!.isFinishing) {
            setBinder(rootView)
            val version = "Version " + util.getCurrentVersion()
            versionNumber.text = version
            clearCache.setOnClickListener { _ ->
                val dialogClickListener = { _: Any, which: Any ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            cleanLocalData()
                            restartApp()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

                AlertDialog.Builder(context)
                    .setMessage("This is going to:\n\n- Delete all your favorites\n- Clear application cache\n- Restart the application")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
            }
        }
        return rootView
    }

    private fun restartApp() {
        val intent = Intent(context, BaseActivity::class.java)
        val intentId = Random().nextInt()
        val pendingIntent = PendingIntent.getActivity(context, intentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent)
        mainActivity!!.finish()
    }

    private fun cleanLocalData() {
        deleteCache(context)
        preferenceService.clearPreferences()
        realmConfig.cleanRealm()
    }

    private fun deleteCache(context: Context?) {
        try {
            val cacheDirectory = context!!.cacheDir
            deleteRecursiveDirectory(cacheDirectory)
        } catch (ignored: Exception) {
        }

    }

    private fun deleteRecursiveDirectory(directory: File?): Boolean {
        if (directory != null && directory.isDirectory) {
            val children = directory.list()
            for (child in children) {
                val success = deleteRecursiveDirectory(File(directory, child))
                if (!success) {
                    return false
                }
            }
            return directory.delete()
        } else
            return directory != null && directory.isFile && directory.delete()
    }

    companion object {

        fun newInstance(sectionNumber: Int): SettingsFragment {
            return AbstractFragment.Companion.fragmentWithBundle(SettingsFragment(), sectionNumber) as SettingsFragment
        }
    }
}
