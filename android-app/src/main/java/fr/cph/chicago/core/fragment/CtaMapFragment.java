package fr.cph.chicago.core.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.cph.chicago.R;
import fr.cph.chicago.core.activity.MainActivity;
import fr.cph.chicago.util.Util;
import uk.co.senab.photoview.PhotoView;

public class CtaMapFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    @BindView(R.id.cta_map)
    PhotoView ctaMap;
    private MainActivity activity;
    private Bitmap bitmapCache;
    private Unbinder unbinder;

    @NonNull
    public static CtaMapFragment newInstance(final int sectionNumber) {
        final CtaMapFragment fragment = new CtaMapFragment();
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
        Util.trackScreen(getContext(), getString(R.string.analytics_cta_map_fragment));
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_cta_map, container, false);
        if (!activity.isFinishing()) {
            unbinder = ButterKnife.bind(this, rootView);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
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
