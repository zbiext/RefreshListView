package com.zbie.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zbie.library.DropDownRefListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements DropDownRefListView.OnRefreshListener {

    private DropDownRefListView    mListView;
    private List<String>           mDatas;
    private DropDownRefreshAdapter mAdapter;
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (DropDownRefListView) findViewById(R.id.listview);

        mDatas = new ArrayList<String>();
        for (int i = 1; i <= 30; i++) {
            mDatas.add("数据条目---" + i);
        }

        mAdapter = new DropDownRefreshAdapter();
        mListView.setAdapter(mAdapter);

        mListView.addOnRefreshListener(this);
    }

    @Override
    public void OnRefreshing() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDatas.clear();
                for (int i = 1; i <= 30; i++) {
                    mDatas.add("第"+count + "次的刷新后的数据条目---" + i);
                }
                mAdapter.notifyDataSetChanged();
                count++;
                mListView.refreshingFinish();
            }
        }, 5000);
    }

    private class DropDownRefreshAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mDatas != null) {
                return mDatas.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mDatas != null) {
                return mDatas.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv.setText(mDatas.get(position));
            return convertView;
        }

        private class ViewHolder {
            public final TextView tv;
            public final View     root;

            public ViewHolder(View root) {
                this.root = root;
                tv = (TextView) root.findViewById(R.id.item_tv);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListView.removeOnRefreshListener(this);
    }
}
