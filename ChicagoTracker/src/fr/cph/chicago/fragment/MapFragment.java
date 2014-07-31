/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.MainActivity;
import fr.cph.chicago.util.Util;

/**
 * Mao Fragment
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class MapFragment extends Fragment implements OnTouchListener {
	/** The fragment argument representing the section number for this fragment. **/
	private static final String ARG_SECTION_NUMBER = "section_number";
	/** The main activity **/
	private MainActivity mActivity;
	/** The matrix **/
	private Matrix matrix = new Matrix();
	/** The saved matrix **/
	private Matrix savedMatrix = new Matrix();
	/** Mode none **/
	private static final int NONE = 0;
	/** Mode drag **/
	private static final int DRAG = 1;
	/** Mode zoom **/
	private static final int ZOOM = 2;
	/** Default mode **/
	private int mode = NONE;
	/** Start point **/
	private PointF start = new PointF();
	/** Mid point **/
	private PointF mid = new PointF();
	/** Old distance **/
	private float oldDist = 1f;
	/** Image view **/
	private ImageView view;

	private Long currentClickTime = (long) 0;
	private Long startTime = (long) 0;
	private Long endTime = (long) 0;
	private Long consecutiveTwoClickTime = (long) 0;
	private Long timeBetweenTwoClick = (long) 0;
	private Long previousClickTime = (long) 0;
	private Long doubleClickTimeDiffrence = (long) 500;
	private Long upCounter = (long) 0;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 * 
	 * @param sectionNumber
	 *            the section number
	 * @return the fragment
	 */
	public static final MapFragment newInstance(final int sectionNumber) {
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.trackScreen(mActivity, R.string.analytics_L_fragment);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);
		view = (ImageView) rootView.findViewById(R.id.imageView);
		view.setOnTouchListener(this);
		view.setImageResource(R.drawable.ctamap);
		return rootView;
	}

	@Override
	public final void onAttach(final Activity activity) {
		super.onAttach(activity);
		mActivity = ((MainActivity) activity);
		mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		view.setScaleType(ScaleType.MATRIX);

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:

			startTime = System.currentTimeMillis();
			if (upCounter != 0) {
				timeBetweenTwoClick = startTime - endTime;
			}

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

			endTime = System.currentTimeMillis();
			currentClickTime = endTime - startTime;
			consecutiveTwoClickTime = currentClickTime + previousClickTime;

			if (upCounter == 0) {
				++upCounter;
				previousClickTime = currentClickTime;
			} else {
				previousClickTime = currentClickTime;
				if ((consecutiveTwoClickTime + timeBetweenTwoClick) <= doubleClickTimeDiffrence) {
					savedMatrix.set(matrix);
					matrix.set(savedMatrix);
					matrix.postScale((float) 2, (float) 2, event.getX(0), event.getY(0));

				}
			}
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
					float scale = newDist / oldDist; // setting the scaling of the matrix...if scale
														// > 1 means zoom in...if scale < 1 means
														// zoom out
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/**
	 * Is centered already
	 * 
	 * @return a boolean
	 */
	public final boolean isCenteredAlready() {
		boolean res = false;
		if (view.getScaleType().equals(ScaleType.FIT_CENTER)) {
			res = true;
		}
		return res;
	}

	/**
	 * Reset image
	 */
	public final void resetImage() {
		view.setScaleType(ScaleType.FIT_CENTER);
	}

	/**
	 * Determine the space between the first two fingers
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) java.lang.Math.sqrt(x * x + y * y);
	}

	/**
	 * Calculate the mid point of the first two fingers
	 * 
	 * @param point
	 *            the point
	 * @param event
	 *            the event
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}
