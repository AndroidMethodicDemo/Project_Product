package com.parallax.ui;

import android.support.v4.app.Fragment;
import android.widget.AbsListView;
/**
 * 这个很关键
 */
public abstract class ScrollTabHolderFragment extends Fragment implements ScrollTabHolder {

	// 这个特别关键
	protected ScrollTabHolder mScrollTabHolder;

	public void setScrollTabHolder(ScrollTabHolder scrollTabHolder) {
		mScrollTabHolder = scrollTabHolder;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
		// nothing
	}

}