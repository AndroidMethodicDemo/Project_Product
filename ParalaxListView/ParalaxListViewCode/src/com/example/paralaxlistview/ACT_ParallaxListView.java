package com.example.paralaxlistview;

import com.nineoldandroids.view.ViewHelper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

public class ACT_ParallaxListView extends ACT_Base implements ScrollTabHolder,
		ViewPager.OnPageChangeListener {
	private static final String TAG = ACT_ParallaxListView.class
			.getSimpleName();

	// private static AccelerateDecelerateInterpolator sSmoothInterpolator = new
	// AccelerateDecelerateInterpolator();

	private ImageView mHeaderPicture;
	private View mHeader;

	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private int mMinHeaderHeight;
	private int mHeaderHeight;
	private int mMinHeaderTranslation;
	private ImageView mHeaderLogo;

	private TypedValue mTypedValue = new TypedValue();
	private SpannableString mSpannableString;

	// private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
	@Override
	protected void initViews() {
		// Header的最小高度 250dp
		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.min_header_height);
		// Header的高度 298dp
		mHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.header_height);
		// Header的最小位移，是个负值
		// mMinHeaderTranslation = -mMinHeaderHeight + getActionBarHeight();
		mMinHeaderTranslation = -mMinHeaderHeight;

		setContentView(R.layout.act_parallax);
		// 每隔一段时间换一张图的控件
		mHeaderPicture = (ImageView) findViewById(R.id.header_picture);
		// Header中间的logo
		mHeaderLogo = (ImageView) findViewById(R.id.header_logo);
		mHeader = findViewById(R.id.header);

		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// 防止页面重置
		mViewPager.setOffscreenPageLimit(4);

		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mPagerAdapter.setTabHolderScrollingContent(this);// 这个设置特别重要！！

		mViewPager.setAdapter(mPagerAdapter);

		mPagerSlidingTabStrip.setViewPager(mViewPager);
		mPagerSlidingTabStrip.setOnPageChangeListener(this);
		// mSpannableString = new
		// SpannableString(getString(R.string.actionbar_title));
		// mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
		// // 设置ActionBar的透明度
		// ViewHelper.setAlpha(getActionBarIconView(), 0f);
	}

	@Override
	protected void bindEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub

	}

	public class PagerAdapter extends FragmentPagerAdapter {

		private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
		private final String[] TITLES = { "Page 1", "Page 2", "Page 3",
				"Page 4" };
		private ScrollTabHolder mListener;

		public PagerAdapter(FragmentManager fm) {
			super(fm);
			// SparseArrayCompat 是个什么东西？
			mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
		}

		public void setTabHolderScrollingContent(ScrollTabHolder listener) {
			mListener = listener;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) SampleListFragment
					.newInstance(position);

			mScrollTabHolders.put(position, fragment);
			if (mListener != null) {
				// 到底是什么玩意？设置Fragment中ListView的onScroll事件
				fragment.setScrollTabHolder(mListener);
			}

			return fragment;
		}

		public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
			return mScrollTabHolders;
		}

	}

	// ---------------------------------------------------------------

	// -------------------------------------------------------------------OnPageChangeListener----------------------------------------------------------------------------
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// nothing
	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
		// TODO
		// 切换ListView，记录ListView所处的位置，调用的是SampleListFragment中的adjustScroll方法
		currentHolder.adjustScroll((int) (mHeader.getHeight() + ViewHelper
				.getTranslationY(mHeader)));
	}

	// -------------------------------------------------------------------OnPageChangeListener----------------------------------------------------------------------------

	// -------------------------------------------------------------------ScrollTabHolder----------------------------------------------------------------------------
	// 入口方法
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		// 这个看不懂
		if (mViewPager.getCurrentItem() == pagePosition) {
			// 加载更多
			((RefreshListView) view).onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
			// 获取ListView滑动的高度，这个是实现平滑滑动的关键所在
			int scrollY = getScrollY(view);
			((RefreshListView) view).setScroll(scrollY);
			Log.i(TAG, "scrollY:" + scrollY);
			// 真正使header滚动的代码，当ListView滚动时，给Header添加一个Y方向的位移动画，通过不断的动画，实现Header随ListView滚动的效果
			ViewHelper.setTranslationY(mHeader,
					Math.max(-scrollY, mMinHeaderTranslation));
			// Log.i(TAG,
			// "Math.max(-scrollY, mMinHeaderTranslation):"+Math.max(-scrollY,
			// mMinHeaderTranslation));
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState,
			int pagePosition) {
		if (mViewPager.getCurrentItem() == pagePosition) {
			((RefreshListView) view).onScrollStateChanged(view, scrollState);
		}

	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// nothing
	}

	// -------------------------------------------------------------------ScrollTabHolder----------------------------------------------------------------------------

	/**
	 * onScroll 中调用了这个方法 从方法名上看是获取ListView滑动的高度 这个方法太复杂了，主要是最后的计算太复杂，看不懂 目的是什么呢？
	 */
	public int getScrollY(AbsListView view) {
		// 获取ListView中的第一个子View，当header可见时为header，当header不可见时为第一个可见view！！
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		// Top position of this view relative to its parent.
		int top = c.getTop();

		int headerHeight = 0;
		// 如果第一个可视控件大于等于1
		if (firstVisiblePosition >= 1) {
			headerHeight = mHeaderHeight;
		}
		Log.i(TAG,
				"top:" + top + ";firstVisiblePosition:" + firstVisiblePosition
						+ ";c.getHeight():" + c.getHeight() + ";headerHeight:"
						+ headerHeight + ";c.toString:" + c.toString());
		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}

	/**
	 * clamp夹紧，return Math.max(Math.min(value, min), max);
	 */
	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

}
