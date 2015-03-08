package com.example.paralaxlistview;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView {
	
	private int firstVisibleItem;
	private int downY;
	private int headerViewHeight;
	private View headerView;
	private ImageView ivArrow;
	private ProgressBar pbProgress;
	private TextView tvState;
	private TextView tvLastUpdateTime;
	
	private final int DOWN_PULL = 0;
	private final int RELEASE_REFRESH = 1;
	private final int REFRESHING = 2;
	
	private int currentState = DOWN_PULL;
	private RotateAnimation upAnimation;
	private RotateAnimation downAnimation;
	private LoadDataCallback mOnRefreshListener;
	private boolean isScroll2Bottom;
	private View footerView;
	private int footerViewHeight;
	private boolean isLoadingMore = false;
	private int scroll=0;

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initHeader();
		initFooter();
		
		setOnScrollListener(new MyListener());
	}

	private void initFooter() {
		footerView = View.inflate(getContext(), R.layout.listview_footer, null);
		footerView.measure(0, 0);
		
		footerViewHeight = footerView.getMeasuredHeight();
		
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		
		addFooterView(footerView);
	}

	private void initHeader() {
		View placeHolderView = View.inflate(this.getContext(),R.layout.view_header_placeholder, null);
		addHeaderView(placeHolderView);
		// ListView下拉刷新头
		headerView = View.inflate(getContext(), R.layout.listview_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
		pbProgress = (ProgressBar) headerView.findViewById(R.id.pb_progress);
		tvState = (TextView) headerView.findViewById(R.id.tv_refresh_state);
		tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_last_update_time);
		
		headerView.measure(0, 0);
		
		headerViewHeight = headerView.getMeasuredHeight();
		
		headerView.setPadding(0, -headerViewHeight, 0, 0);
		
		addHeaderView(headerView);
		
		initAnimation();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveY = (int) ev.getY();
			
			int diff = (moveY - downY) / 2;
			
//			if(firstVisibleItem == 0 && diff > 0 && currentState != REFRESHING) {
			if(scroll == 0 && diff > 0 && currentState != REFRESHING) {
				int paddingTop = -headerViewHeight + diff;
				
				if(paddingTop > 0 && currentState == DOWN_PULL) {
					currentState = RELEASE_REFRESH;
					refreshHeaderViewState();
				} else if(paddingTop < 0 && currentState == RELEASE_REFRESH) {
					currentState = DOWN_PULL;
					refreshHeaderViewState();
				}
				
				headerView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(currentState == RELEASE_REFRESH) {
				currentState = REFRESHING;
				refreshHeaderViewState();
				headerView.setPadding(0, 0, 0, 0);
				//------------------获取最新数据------------------------
				if(mOnRefreshListener != null) {
//					mOnRefreshListener.onRefresh();
					loadNewData();
				}
			} else if(currentState == DOWN_PULL) {
				headerView.setPadding(0, -headerViewHeight, 0, 0);
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(isScroll2Bottom && 
				(scrollState == 0 || scrollState == 2)) {
//		System.out.println("滚动到底部");
			
			if(!isLoadingMore) {
				isLoadingMore = true;
				footerView.setPadding(0, 0, 0, 0);
				setSelection(getCount() -1);
				
				if(mOnRefreshListener != null) {
//					mOnRefreshListener.loadOldData();
					/*-------------------------------------------------
					 * 加载以前的数据
					 * 需要在线程池中执行，并把在主线程中更新界面
					 */
					loadOldData();
				}
			}
		}
	}
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(getLastVisiblePosition() == (getCount() -1)) {
			isScroll2Bottom = true;
		} else {
			isScroll2Bottom = false;
		}
	}
	class MyListener implements OnScrollListener{
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(isScroll2Bottom && 
					(scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING)) {
//			System.out.println("滚动到底部");
				
				if(!isLoadingMore) {
					isLoadingMore = true;
					footerView.setPadding(0, 0, 0, 0);
					setSelection(getCount() -1);
					
					if(mOnRefreshListener != null) {
//						mOnRefreshListener.loadOldData();
						/*-------------------------------------------------
						 * 加载以前的数据
						 * 需要在线程池中执行，并把在主线程中更新界面
						 */
						loadOldData();
					}
				}
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			RefreshListView.this.firstVisibleItem = firstVisibleItem;
			
			if(getLastVisiblePosition() == (getCount() -1)) {
				isScroll2Bottom = true;
			} else {
				isScroll2Bottom = false;
			}
		}
	}// End of OnRefreshListener

	public void setOnRefreshListener(LoadDataCallback listener) {
		this.mOnRefreshListener = listener;
	}
	
	private void loadNewData(){
		new AsyncTask<Void, Void, BaseAdapter>(){

			@Override
			protected BaseAdapter doInBackground(Void... params) {
				return mOnRefreshListener.loadNewData();
				
			}
			
			@Override
			protected void onPostExecute(BaseAdapter result) {
				onRefreshFinish();
				result.notifyDataSetChanged();
			}
			
		}.execute();
	}
	private void loadOldData(){
		new AsyncTask<Void, Void, BaseAdapter>(){
			@Override
			protected BaseAdapter doInBackground(Void... params) {
				return mOnRefreshListener.loadOldData();
			}
			@Override
			protected void onPostExecute(BaseAdapter result) {
				onRefreshFinish();
				result.notifyDataSetChanged();
			}
		}.execute();
	}
	/**
	 * 更新数据成功之后的界面更新
	 */
	public void onRefreshFinish() {
		if(isLoadingMore) {
			isLoadingMore = false;
			footerView.setPadding(0, -footerViewHeight, 0, 0);
		} else {
			headerView.setPadding(0, -headerViewHeight, 0, 0);
			currentState = DOWN_PULL;
			ivArrow.setVisibility(View.VISIBLE);
			pbProgress.setVisibility(View.GONE);
			tvLastUpdateTime.setText("最后刷新时间: " + getLastUpdateTime());
		}
	}

	/**
	 * 重要的方法
	 */
	private void refreshHeaderViewState() {
		if(currentState == DOWN_PULL) {	// 下拉刷新状态
			ivArrow.startAnimation(downAnimation);
			tvState.setText("下拉刷新");
		} else if(currentState == RELEASE_REFRESH) {	// 释放刷新
			ivArrow.startAnimation(upAnimation);
			tvState.setText("松开刷新");
		} else if(currentState == REFRESHING) {	// 正在刷新
			ivArrow.clearAnimation();
			ivArrow.setVisibility(View.GONE);
			pbProgress.setVisibility(View.VISIBLE);
			tvState.setText("正在刷新");
		}
	}
	
	/**
	 * 创建动画对象
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(
				0f, -180f, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true);
		
		downAnimation = new RotateAnimation(
				-180f, -360f, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true);
	}
	
	/**
	 * 获得最新的时间
	 * @return
	 */
	private String getLastUpdateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	public void setScroll(int scroll){
		this.scroll=scroll;
	}
}
