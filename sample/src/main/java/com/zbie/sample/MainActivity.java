package com.zbie.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zbie.library.DropDownRefListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements DropDownRefListView.OnRefreshListener {

    private DropDownRefListView mListView;
    private ViewPager           mPager;
    private int[] mPics = new int[]{R.drawable.pic_1,
            R.drawable.pic_2,
            R.drawable.pic_3,
            R.drawable.pic_4};

    private List<String>           mDatas;
    private DropDownRefreshAdapter mAdapter;

    private int mRefreshCount  = 1;
    private int mLoadMoreCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (DropDownRefListView) findViewById(R.id.listview);

        View header = View.inflate(this, R.layout.header, null);
        mPager = (ViewPager) header.findViewById(R.id.viewpager);
        mPager.setAdapter(new HeaderAdapter());


        mDatas = new ArrayList<String>();
        for (int i = 1; i <= 30; i++) {
            mDatas.add("数据条目---" + i);
        }

        mAdapter = new DropDownRefreshAdapter();
        mListView.setAdapter(mAdapter);
        //listview添加自定义的头
        mListView.addHeaderView(header);

        mListView.addOnRefreshListener(this);
    }

    @Override
    public void OnRefreshing() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDatas.clear();
                for (int i = 1; i <= 30; i++) {
                    mDatas.add("第" + mRefreshCount + "次的刷新后的数据条目---" + i);
                }
                mAdapter.notifyDataSetChanged();
                mRefreshCount++;
                mListView.refreshingFinish();
            }
        }, 1200);
    }

    private class HeaderAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = new ImageView(MainActivity.this);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageResource(mPics[position]);

            container.addView(iv);

            return iv;
        }

        @Override
        public int getCount() {
            if(mPics != null) {
                return mPics.length;
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
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
