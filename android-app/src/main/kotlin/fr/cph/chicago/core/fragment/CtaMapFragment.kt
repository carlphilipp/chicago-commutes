package fr.cph.chicago.core.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.github.chrisbanes.photoview.PhotoView
import fr.cph.chicago.R
import fr.cph.chicago.util.Util

class CtaMapFragment : AbstractFragment() {

    @BindView(R.id.cta_map)
    lateinit var ctaMap: PhotoView
    private var bitmapCache: Bitmap? = null

    private val util: Util = Util

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        util.trackScreen(getString(R.string.analytics_cta_map_fragment))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_cta_map, container, false)
        if (!mainActivity!!.isFinishing) {
            setBinder(rootView)
            loadBitmap(ctaMap)
        }
        return rootView
    }

    fun loadBitmap(imageView: PhotoView?) {
        if (bitmapCache != null) {
            imageView!!.setImageBitmap(bitmapCache)
        } else {
            val task = BitmapWorkerTask(imageView!!)
            task.execute()
        }
    }

    private inner class BitmapWorkerTask internal constructor(private val imageView: PhotoView) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap {
            return BitmapFactory.decodeResource(resources, R.drawable.ctamap)
        }

        override fun onPostExecute(bitmap: Bitmap) {
            this@CtaMapFragment.bitmapCache = bitmap
            imageView.setImageBitmap(bitmap)
        }
    }

    companion object {

        fun newInstance(sectionNumber: Int): CtaMapFragment {
            return AbstractFragment.Companion.fragmentWithBundle(CtaMapFragment(), sectionNumber) as CtaMapFragment
        }
    }
}
