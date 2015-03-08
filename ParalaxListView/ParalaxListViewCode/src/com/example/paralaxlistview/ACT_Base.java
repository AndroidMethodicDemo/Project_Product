package com.example.paralaxlistview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public abstract class ACT_Base extends FragmentActivity{
	
	protected Activity mContext;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		mContext = ACT_Base.this;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		initViews();
		
		bindEvents();
		
		initDatas();
	}
	
	protected abstract void initViews();
	
	protected abstract void bindEvents();
	
	protected abstract void initDatas();
}
