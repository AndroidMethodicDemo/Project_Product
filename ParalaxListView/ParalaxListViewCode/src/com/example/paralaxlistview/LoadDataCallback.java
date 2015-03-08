package com.example.paralaxlistview;

import android.widget.BaseAdapter;

/**
 * @author andong
 * RefreshListViewˢ���¼��ļ���
 */
public interface LoadDataCallback {

	/**
	 * �����������
	 */
	public BaseAdapter loadNewData();
	
	/**
	 * ������ǰ�����
	 */
	public BaseAdapter loadOldData();
}
