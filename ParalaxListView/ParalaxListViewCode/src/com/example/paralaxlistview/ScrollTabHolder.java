package com.example.paralaxlistview;

import android.widget.AbsListView;
/**
 * 
 */
public interface ScrollTabHolder {

	void adjustScroll(int scrollHeight);

	void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition);
	void onScrollStateChanged(AbsListView view, int scrollState, int pagePosition) ;
}
