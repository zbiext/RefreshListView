package com.zbie.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.Iterator;
import java.util.List;

/**
 * 项目名          RefreshListView
 * 包名            com.zbie.library
 * 创建时间        2016/02/25 09:33
 * 创建者          zbie
 * 邮箱            hyxt2011@163.com
 * 描述            TODO
 */
public class DropDownRefListView extends ListView {

    private List<OnRefreshListener> mListenerContainer;
    private View                    mRefreshHeader;

    public DropDownRefListView(Context context) {
        super(context);
    }

    public DropDownRefListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //初始化刷新的头
        initRefreshheader();
    }

    private void initRefreshheader() {
        mRefreshHeader = View.inflate(getContext(), R.layout.refresh_header, null);
    }

    public void refreshingFinish() {

    }

    public void addOnRefreshListener(OnRefreshListener listener) {
        if (mListenerContainer.contains(listener)) {
            return;
        }
        mListenerContainer.add(listener);
    }

    public void removeOnRefreshListener(OnRefreshListener listener) {
        if (mListenerContainer.contains(listener)) {
            mListenerContainer.remove(listener);
        }
    }

    /**通知所有监听开始工作*/
    public void notifyOnRefreshing() {
        for (Iterator<OnRefreshListener> iterator = mListenerContainer.iterator(); iterator.hasNext(); ) {
            OnRefreshListener listener = iterator.next();
            listener.OnRefreshing();
        }
    }

    /**
     * 刷新监听
     */
    public interface OnRefreshListener {
        /**
         * 正在刷新的回调
         */
        void OnRefreshing();
    }
}
