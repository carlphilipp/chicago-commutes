package fr.cph.chicago.core.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import butterknife.BindView;
import fr.cph.chicago.R;
import fr.cph.chicago.core.App;
import fr.cph.chicago.util.Util;

@SuppressWarnings("WeakerAccess")
public class CtaMapFragment extends AbstractFragment {

    @BindView(R.id.cta_map)
    PhotoView ctaMap;
    private Bitmap bitmapCache;

    private final Util util;

    public CtaMapFragment() {
        util = Util.INSTANCE;
    }

    @NonNull
    public static CtaMapFragment newInstance(final int sectionNumber) {
        return (CtaMapFragment) fragmentWithBundle(new CtaMapFragment(), sectionNumber);
    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.trackScreen(getString(R.string.analytics_cta_map_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_cta_map, container, false);
        if (!activity.isFinishing()) {
            setBinder(rootView);
            loadBitmap(ctaMap);
        }
        return rootView;
    }

    public void loadBitmap(final PhotoView imageView) {
        if (bitmapCache != null) {
            imageView.setImageBitmap(bitmapCache);
        } else {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute();
        }
    }

    private class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final PhotoView imageView;

        BitmapWorkerTask(final PhotoView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(final Void... params) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ctamap);
        }

        @Override
        protected final void onPostExecute(final Bitmap bitmap) {
            CtaMapFragment.this.bitmapCache = bitmap;
            imageView.setImageBitmap(bitmap);
        }
    }
}
