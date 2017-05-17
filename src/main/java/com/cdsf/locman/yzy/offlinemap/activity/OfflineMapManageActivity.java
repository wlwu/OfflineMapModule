package com.cdsf.locman.yzy.offlinemap.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.cdsf.locman.yzy.Bean.LocManNotifyBean;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.offlinemap.fragment.OfflineMapCityFragment;
import com.cdsf.locman.yzy.offlinemap.fragment.OfflineMapDownloadFragment;
import com.cdsf.locman.yzy.provider.LocManProvider;
import com.cdsf.locman.yzy.util.LocManNotifyType;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by Administrator on 2016/10/27.
 */
@Route(path=LocManProvider.PROVIDER_OFFLINE_MAP)
public class OfflineMapManageActivity extends FragmentActivity implements MKOfflineMapListener {
    TextView mDownloadMangeView;
    TextView mCityManageView;
    ViewPager mViewPager;
    private List<Fragment> mFragmentSource = new ArrayList<>();
    private FragmentPagerAdapter mFragmentAdapter;
    private MKOfflineMap mOffline;
    private OfflineMapDownloadFragment downloadFragment;
    private OfflineMapCityFragment cityFragment;
    private UpdateHandler mHandler;
    private final static int UPDATE_NOTIFY_FLAG=0x126;
    private final static int UPDATE_NOTIFY_TIME=300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offlinemap_manage_layout);
        ButterKnife.bind(this);
        initView();
        mOffline = new MKOfflineMap();
        mOffline.init(this);
        initSource();
        EventBus.getDefault().register(this);
        mHandler=new UpdateHandler();
        mHandler.sendEmptyMessageDelayed(UPDATE_NOTIFY_FLAG, UPDATE_NOTIFY_TIME);
    }
    private void initView(){
        mDownloadMangeView=(TextView)findViewById(R.id.download_manage_view);
        mCityManageView=(TextView)findViewById(R.id.city_manage_view);
        mViewPager=(ViewPager)findViewById(R.id.viepager);
    }
    public MKOfflineMap getMKOfflineMap(){
        return mOffline;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 退出时，销毁离线地图模块
         */
        mHandler.removeCallbacksAndMessages(null);
        mHandler=null;
        mOffline.destroy(); //因为在MineSetting 中在使用
        EventBus.getDefault().unregister(this);
    }

    class UpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_NOTIFY_FLAG:
                    notifyUpdate();
                    if(mHandler!=null){
                        mHandler.sendEmptyMessageDelayed(UPDATE_NOTIFY_FLAG, UPDATE_NOTIFY_TIME);
                    }
                    break;
            }
        }
    }
    public void notifyUpdate(){
        int currentShow=mViewPager.getCurrentItem();
        if(downloadFragment!=null&&downloadFragment.getScrollStatus()&&currentShow==0){
            downloadFragment.notifyUpdate();
        }
        if(cityFragment!=null&&cityFragment.getScrollStatus()&&currentShow==1){
            cityFragment.notifyUpdate();
        }
    }
    private void initSource() {
        downloadFragment = new OfflineMapDownloadFragment();
       // downloadFragment.setMKOfflineMap(mOffline);
        cityFragment = new OfflineMapCityFragment();
        //cityFragment.setMKOfflineMap(mOffline);
        mFragmentSource.add(downloadFragment);
        mFragmentSource.add(cityFragment);
        mFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mFragmentSource.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragmentSource.get(position);
            }
        };
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                handleViewPagerChange(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(mFragmentSource.size());
        mViewPager.setCurrentItem(0);
        handleViewPagerChange(0);
    }

/*    @OnClick(value = {R.id.back_layout, R.id.download_manage_view, R.id.city_manage_view})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.download_manage_view:
                mViewPager.setCurrentItem(0);
                handleViewPagerChange(0);
                break;
            case R.id.city_manage_view:
                mViewPager.setCurrentItem(1);
                handleViewPagerChange(1);
                break;
        }
    }*/

    private void handleViewPagerChange(int choosePosition) {
        if (choosePosition == 0) {
            //下载管理
            mDownloadMangeView.setTextColor(getResources().getColor(R.color.white));
            mDownloadMangeView.setBackgroundResource(R.color.primaryColor);
            mCityManageView.setTextColor(getResources().getColor(R.color.primaryColor));
            mCityManageView.setBackgroundResource(android.R.color.transparent);
        } else {
            //城市列表
            mCityManageView.setTextColor(getResources().getColor(R.color.white));
            mCityManageView.setBackgroundResource(R.color.primaryColor);
            mDownloadMangeView.setTextColor(getResources().getColor(R.color.primaryColor));
            mDownloadMangeView.setBackgroundResource(android.R.color.transparent);
        }
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {

    }
    //采用通知的方式来进行操作
    @Subscribe
    public void onEventMainThread(LocManNotifyBean notifyBean) {
        if (TextUtils.equals(notifyBean.getNotifyTyp(), LocManNotifyType.NOTIFY_OFFLINE_SCROLL_TO_DOWNLOAD)) {
            //跳转到下载管理界面
            mViewPager.setCurrentItem(0,true);
        }
    }
}
