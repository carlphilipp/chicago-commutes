package fr.cph.chicago.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;

public class MapFragment extends Fragment implements OnTouchListener {

	private static final String TAG = "MapFragment";

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private MainActivity activity;

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	float mCurrentScale = 1.0f;

	private ImageView view;

	public static final MapFragment newInstance(final int sectionNumber) {
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);
		view = (ImageView) rootView.findViewById(R.id.imageView);
		view.setOnTouchListener(this);
//		Handler handler = new Handler(); 
//	    handler.postDelayed(new Runnable() { 
//	         public void run() { 
//	        	 view.setImageResource(R.drawable.ctamap);
//	         } 
//	    }, 250); 
	    
	    view.setImageResource(R.drawable.ctamap);
		return rootView;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity) activity;
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

//	@Override
//	public final void onResume(){
//		
//	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		view.setScaleType(ScaleType.MATRIX);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:

			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 5f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist; // setting the scaling of the
					// matrix...if scale > 1 means
					// zoom in...if scale < 1 means
					// zoom out
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	public final boolean isCenteredAlready() {
		boolean res = false;
		if (view.getScaleType().equals(ScaleType.FIT_CENTER)) {
			res = true;
		}
		return res;
	}

	public final void resetImage() {
		view.setScaleType(ScaleType.FIT_CENTER);
	}

	/** Show an event in the LogCat view, for debugging */

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
