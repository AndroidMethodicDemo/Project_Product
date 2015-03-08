package com.parallax.ui;

import android.annotation.TargetApi;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.kmshack.newsstand.R;
import com.nineoldandroids.view.ViewHelper;
import com.parallax.view.AlphaForegroundColorSpan;
import com.parallax.view.KenBurnsSupportView;
import com.parallax.view.PagerSlidingTabStrip;

public class MainActivity extends ActionBarActivity implements ScrollTabHolder, ViewPager.OnPageChangeListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static AccelerateDecelerateInterpolator sSmoothInterpolator = new AccelerateDecelerateInterpolator();

	private KenBurnsSupportView mHeaderPicture;
	private View mHeader;

	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private int mActionBarHeight;
	private int mMinHeaderHeight;
	private int mHeaderHeight;
	private int mMinHeaderTranslation;
	private ImageView mHeaderLogo;

	private RectF mRect1 = new RectF();
	private RectF mRect2 = new RectF();

	private TypedValue mTypedValue = new TypedValue();
	private SpannableString mSpannableString;
	private AlphaForegroundColorSpan mAlphaForegroundColorSpan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Header的最小高度 250dp
		mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height);
		//Header的高度 298dp
		mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
		//Header的最小位移，是个负值
		mMinHeaderTranslation = -mMinHeaderHeight + getActionBarHeight();

		setContentView(R.layout.activity_main);
		// 每隔一段时间换一张图的控件
		mHeaderPicture = (KenBurnsSupportView) findViewById(R.id.header_picture);
		mHeaderPicture.setResourceIds(R.drawable.pic0, R.drawable.pic1);
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
		mSpannableString = new SpannableString(getString(R.string.actionbar_title));
		mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
		// 设置ActionBar的透明度
		ViewHelper.setAlpha(getActionBarIconView(), 0f);
		
		getSupportActionBar().setBackgroundDrawable(null);
	}
//-------------------------------------------------------------------OnPageChangeListener----------------------------------------------------------------------------
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing
	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
		// TODO 切换ListView，记录ListView所处的位置，调用的是SampleListFragment中的adjustScroll方法
		currentHolder.adjustScroll((int) (mHeader.getHeight() + ViewHelper.getTranslationY(mHeader)));
	}
//-------------------------------------------------------------------OnPageChangeListener----------------------------------------------------------------------------

//-------------------------------------------------------------------ScrollTabHolder----------------------------------------------------------------------------
	//入口方法
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
		// 这个看不懂
		if (mViewPager.getCurrentItem() == pagePosition) {
			// 获取ListView滑动的高度，这个是实现平滑滑动的关键所在
			int scrollY = getScrollY(view);
//			Log.i(TAG, "scrollY:"+scrollY);
			// 真正使header滚动的代码，当ListView滚动时，给Header添加一个Y方向的位移动画，通过不断的动画，实现Header随ListView滚动的效果
			ViewHelper.setTranslationY(mHeader, Math.max(-scrollY, mMinHeaderTranslation));
//			Log.i(TAG, "Math.max(-scrollY, mMinHeaderTranslation):"+Math.max(-scrollY, mMinHeaderTranslation));
			
			// 感觉下面这三句话主要是对headerLogo的操作
			float ratio = clamp(ViewHelper.getTranslationY(mHeader) / mMinHeaderTranslation, 0.0f, 1.0f);
			interpolate(mHeaderLogo, getActionBarIconView(), sSmoothInterpolator.getInterpolation(ratio));// 第三个参数是动态变化的关键
			setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
		}
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// nothing
	}
//-------------------------------------------------------------------ScrollTabHolder----------------------------------------------------------------------------

	/**
	 * onScroll 中调用了这个方法
	 * 从方法名上看是获取ListView滑动的高度
	 * 这个方法太复杂了，主要是最后的计算太复杂，看不懂
	 * 目的是什么呢？
	 */
	public int getScrollY(AbsListView view) {
		// 获取ListView中的第一个子View，当header可见时为header，当header不可见时为第一个可见view！！
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		//Top position of this view relative to its parent.
		int top = c.getTop();

		int headerHeight = 0;
		// 如果第一个可视控件大于等于1
		if (firstVisiblePosition >= 1) {
			headerHeight = mHeaderHeight;
		}
		Log.i(TAG, "top:"+top+";firstVisiblePosition:"+firstVisiblePosition+";c.getHeight():"+c.getHeight()+";headerHeight:"+headerHeight+";c.toString:"+c.toString());
		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}

	/**
	 * clamp夹紧，return Math.max(Math.min(value, min), max);
	 */
	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

	/**
	 * interpolate插入
	 * 作用：logo大小和位移动画
	 * interpolation是动态改变的参数！！是关键
	 * view1 mHeaderLogo，view2 getActionBarIconView
	 */
	private void interpolate(View view1, View view2, float interpolation) {
		getOnScreenRect(mRect1, view1);
		getOnScreenRect(mRect2, view2);

		float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
		float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
		float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
		float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));
		// 很多地方用到了ViewHelper（translation位移）
		// 下面两句是logo位移动画
		ViewHelper.setTranslationX(view1, translationX);
		ViewHelper.setTranslationY(view1, translationY - ViewHelper.getTranslationY(mHeader));
		// 下面两句是logo大小动画
		ViewHelper.setScaleX(view1, scaleX);
		ViewHelper.setScaleY(view1, scaleY);
	}

	/**
	 * 设置并返回获取View在屏幕中的RectF
	 */
	private RectF getOnScreenRect(RectF rect, View view) {
		rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		return rect;
	}

	/**
	 * 获取ActionBar的高度
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}
		
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
			getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
		}else{
			getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
		}
		
		mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
		
		return mActionBarHeight;
	}

	/**
	 * 设置Title的透明度
	 */
	private void setTitleAlpha(float alpha) {
		mAlphaForegroundColorSpan.setAlpha(alpha);
		mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getSupportActionBar().setTitle(mSpannableString);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ImageView getActionBarIconView() {
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			return (ImageView)findViewById(android.R.id.home);
		}

		return (ImageView)findViewById(android.support.v7.appcompat.R.id.home);
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
		private final String[] TITLES = { "Page 1", "Page 2", "Page 3", "Page 4"};
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
			ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) SampleListFragment.newInstance(position);

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
}
