package com.zbie.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private static final String TAG                   = DropDownRefListView.class.getSimpleName();
    private static final int    STATE_PULL_DOWN       = 0;//下拉的状态(move拖动)
    private static final int    STATE_RELEASE_REFRESH = 1;//释放刷新状态(up松手)
    private static final int    STATE_REFRESHING      = 2;//正在刷新(请求数据)
    private List<OnRefreshListener> mListenerContainer;
    private View                    mRefreshHeader;
    private ProgressBar             mPbLoading;
    private ImageView               mIvArrow;
    private TextView                mTvState;
    private TextView                mTvData;
    private int                     mRefreshHeight;
    private RotateAnimation         mDown2UpAnimation;
    private RotateAnimation         mUp2DownAnimation;
    private float                   mDownY;
    private int                     mCurrentState;

    public DropDownRefListView(Context context) {
        super(context);
    }

    public DropDownRefListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //初始化刷新的头
        initRefreshheader();
        //由向下变为向上
        mDown2UpAnimation = new RotateAnimation(0,
                180,
                Animation.RELATIVE_TO_SELF,
                .5f,
                Animation.RELATIVE_TO_SELF,
                .5f);
        mDown2UpAnimation.setDuration(400);
        mDown2UpAnimation.setFillAfter(true);

        //由向上变为向下
        mUp2DownAnimation = new RotateAnimation(-180,
                0,
                Animation.RELATIVE_TO_SELF,
                .5f,
                Animation.RELATIVE_TO_SELF,
                .5f);
        mUp2DownAnimation.setDuration(400);
        mUp2DownAnimation.setFillAfter(true);

        //初始化监听器集合
        mListenerContainer = new ArrayList<>();
    }

    private void initRefreshheader() {
        //添加头
        mRefreshHeader = View.inflate(getContext(), R.layout.refresh_header, null);
        addHeaderView(mRefreshHeader);

        //查找控件
        mPbLoading = (ProgressBar) mRefreshHeader.findViewById(R.id.refresh_header_pb_loading);
        mIvArrow = (ImageView) mRefreshHeader.findViewById(R.id.refresh_header_iv_arrow);
        mTvState = (TextView) mRefreshHeader.findViewById(R.id.refresh_header_tv_state);
        mTvData = (TextView) mRefreshHeader.findViewById(R.id.refresh_header_tv_data);

        //希望看不到刷新的头,如果用户手势拖动时才可以看到
        //先隐藏头
        //measure(width,height) ---> layout ---> draw(需要先测量)
        mRefreshHeader.measure(0, 0);
        mRefreshHeight = mRefreshHeader.getMeasuredHeight();
//        Log.d(TAG, "mRefreshHeight:" + mRefreshHeight);
        mRefreshHeader.setPadding(0, -mRefreshHeight, 0, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE://拖动时

                float moveY = ev.getY();
                //移动的差值
                float diffY = moveY - mDownY;//向下滑,值大于0;向上滑,值小于0

                //如果当前是正在刷新
                if (mCurrentState == STATE_REFRESHING) {//让用户不可以继续拖动
                    break;
                }

                //当第0个可见时，用户是由上往下拉动时(diffY > 0)，需要刷新头可见
                if (diffY > 0 && getFirstVisiblePosition() == 0) {
                    //需要刷新头可见
                    int top = (int) (diffY - mRefreshHeight + .5f);
//                    Log.d(TAG, "top：" + top);
                    mRefreshHeader.setPadding(0, top, 0, 0);
                    //top大于0,就是diffY大于mRefreshHeight,即可以显示释放刷新
                    if (top >= 0 && mCurrentState != STATE_RELEASE_REFRESH) {//当用户拉到到某个临界点时，显示为释放刷新
                        Log.d(TAG, "释放刷新");
                        mCurrentState = STATE_RELEASE_REFRESH;
                        refreshUI();
                    } else if (top < 0 && mCurrentState != STATE_PULL_DOWN) {//当用户没有超过某个临界点时，显示为下拉刷新
                        Log.d(TAG, "下拉过程");
                        mCurrentState = STATE_PULL_DOWN;
                        refreshUI();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:

                // 松开时正在刷新

                //如果是释放刷新时松开的，变为正在刷新
                if (mCurrentState == STATE_RELEASE_REFRESH) {
                    //状态改变
                    mCurrentState = STATE_REFRESHING;
                    //变化UI
                    refreshUI();

                    int start = mRefreshHeader.getPaddingTop();
                    int end = 0;//停留下来等待刷新
                    //刷新头得刚好完全显示或者已经超过了(是需要执行刷新任务的),接下就慢慢的归位
                    doHeaderAnimator(start, end, true);

                } else if (mCurrentState == STATE_PULL_DOWN) {//如果是下拉刷新时松开,刷新头没有完全露出(就认为不需要执行刷新任务),只需要慢慢的归位
                    int start = mRefreshHeader.getPaddingTop();
                    int end = -mRefreshHeight;//继续隐藏
                    doHeaderAnimator(start, end, false);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 刷新头归位的动画
     *
     * @param start
     * @param end
     * @param isNeedRefresh
     */
    private void doHeaderAnimator(int start, int end, final boolean isNeedRefresh) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        //动画播放时长一个算法限制
        long duration = Math.abs(start - end) * 10;
        if (duration > 700) {
            duration = 700;
        }
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mRefreshHeader.setPadding(0, value, 0, 0);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isNeedRefresh) {
                    //结束时通知刷新
                    //触发了正在刷新...
                    //使用者可能去网络加载数据，或是数据库....
                    notifyOnRefreshing();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    /**
     * 刷新(数据请求)结束
     */
    public void refreshingFinish() {
        //改变刷新的状态+刷新UI
        mCurrentState = STATE_PULL_DOWN;
        refreshUI();
        //刷新头回去,全部隐藏起来
        int start = mRefreshHeader.getPaddingTop();
        int end   = -mRefreshHeight;//继续隐藏
        doHeaderAnimator(start, end, false);
        mTvData.setText("上次更新时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));

    }

    /**
     * 刷新三种状态分别对应UI的变化
     */
    private void refreshUI() {
        switch (mCurrentState) {
            case STATE_PULL_DOWN://下拉过程
                //文本改变
                mTvState.setText("下拉刷新");

                //箭头动画(由上往下转,箭头是由向下变为向上)
                mIvArrow.startAnimation(mUp2DownAnimation);

                //刷新圈显示
                mPbLoading.setVisibility(View.INVISIBLE);
                break;
            case STATE_RELEASE_REFRESH://松手状态
                //文本改变
                mTvState.setText("释放刷新");

                //箭头动画(由下往上转,箭头是由向上变为向下)
                mIvArrow.startAnimation(mDown2UpAnimation);

                //刷新圈显示
                mPbLoading.setVisibility(View.INVISIBLE);
                break;
            case STATE_REFRESHING://正在刷新
                //文本改变
                mTvState.setText("正在刷新");

                //隐藏箭头
                mIvArrow.clearAnimation();
                mIvArrow.setVisibility(View.INVISIBLE);

                //显示进度圈
                mPbLoading.setVisibility(View.VISIBLE);
                break;
        }
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

    /**
     * 通知所有刷新监听开始工作
     */
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
