/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.fragment.drawer;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.cph.chicago.R;

/**
 * Custom drawer adapter
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {
	/** **/
	private Context mContext;
	/** **/
	private List<DrawerItem> mListItems;
	/** **/
	private int mResource;

	/**
	 * @param context
	 * @param resource
	 * @param listItems
	 */
	public CustomDrawerAdapter(Context context, int resource, List<DrawerItem> listItems) {
		super(context, resource, listItems);
		this.mContext = context;
		this.mResource = resource;
		this.mListItems = listItems;
	}

	@Override
	public final View getView(final int position, final View convertView, final ViewGroup parent) {
		DrawerItemHolder drawerHolder;
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(mResource, parent, false);
			drawerHolder.name = (TextView) view.findViewById(R.id.drawer_itemName);
			drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

			view.setTag(drawerHolder);

		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();

		}

		DrawerItem dItem = (DrawerItem) this.mListItems.get(position);

		drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgId()));
		drawerHolder.name.setText(dItem.getName());

		return view;
	}

	private static class DrawerItemHolder {
		TextView name;
		ImageView icon;
	}

}
