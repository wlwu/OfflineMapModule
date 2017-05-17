package com.cdsf.locman.yzy.offlinemap.fragment;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.cdsf.locman.yzy.Bean.LocManNotifyBean;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.offlinemap.activity.OfflineMapManageActivity;
import com.cdsf.locman.yzy.offlinemap.adapter.OfflineMapDownloadAdapter;
import com.cdsf.locman.yzy.receiver.NetStatusChangeReceiver;
import com.cdsf.locman.yzy.util.LocManNotifyType;
import com.cdsf.locman.yzy.util.NetUtil;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by Administrator on 2016/10/27.
 */
public class OfflineMapDownloadFragment extends OfflineMapBaseFragment {
    private MKOfflineMap mOffline;
    RecyclerView mRecyclerView;
    TextView mAllUpdateView;//全部更新
    TextView mAllDownloadView;//全部下载
    TextView mAllPauseView;//全部暂停
    TextView mNetWorkTipView;//网络提示
    private NetStatusChangeReceiver mNetStatusReceiver;
    private OfflineMapDownloadAdapter mAdapter;
    private List<MKOLUpdateElement> mDownloadingSource = new ArrayList<>();//正在下载
    private List<MKOLUpdateElement> mDownloadedSource = new ArrayList<>();//下载完成
    private List<MKOLUpdateElement> mCityUpdateSource = new ArrayList<>();//所有更新信息
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offlinemap_download_layout, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        return view;
    }
    private void initView(View view){
        mRecyclerView=(RecyclerView)view.findViewById(R.id.recyclerview);
        mAllUpdateView=(TextView)view.findViewById(R.id.all_update_view);
        mAllDownloadView=(TextView)view.findViewById(R.id.all_download_view);
        mAllPauseView=(TextView)view.findViewById(R.id.all_pause_view);
        mNetWorkTipView=(TextView)view.findViewById(R.id.network_tip_view);
    }
    public void setMKOfflineMap(MKOfflineMap offlineMap) {
       // mOffline = offlineMap;
    }

    private void setupRecyclerView() {
        mAdapter = new OfflineMapDownloadAdapter(getActivity(), mOffline, mDownloadingSource, mDownloadedSource);
        RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(null);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);
    }
    public boolean getScrollStatus(){
        boolean Idle=(mRecyclerView.getScrollState()== RecyclerView.SCROLL_STATE_IDLE);
        return Idle;
    }
    //动态注册网络变化广播
    private void RegisterNetStatusReceiver() {
        if (mNetStatusReceiver != null) {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getActivity().registerReceiver(mNetStatusReceiver, mFilter);
        }
    }
    private void unRegisterNetStatusReceiver() {
        if (mNetStatusReceiver != null) {
            getActivity().unregisterReceiver(mNetStatusReceiver);
            mNetStatusReceiver=null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OfflineMapManageActivity mActivity=(OfflineMapManageActivity)getActivity();
        mOffline=mActivity.getMKOfflineMap();
        setupRecyclerView();
        fetchSource();
        EventBus.getDefault().register(this);
        mNetStatusReceiver=new NetStatusChangeReceiver();
        RegisterNetStatusReceiver();
        checkCurrentNetWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unRegisterNetStatusReceiver();
    }

    public void notifyUpdate() {
        fetchSource();
    }

    private void fetchSource() {
        mCityUpdateSource = mOffline.getAllUpdateInfo();
        mDownloadingSource.clear();
        mDownloadedSource.clear();
        if (mCityUpdateSource == null) {
            mCityUpdateSource = new ArrayList<>();
        }
        for (MKOLUpdateElement element : mCityUpdateSource) {
            if (element.status == MKOLUpdateElement.FINISHED) {
                mDownloadedSource.add(element);
            } else {
                mDownloadingSource.add(element);
            }
        }
        mAdapter.setDataSource(mDownloadingSource, mDownloadedSource);
        mAdapter.notifyDataSetChanged();
        updateBottomLayout();
    }

    private void updateBottomLayout() {
        int updateSize=0;
        int downloadSize=0;
        int pauseSize=0;
        for(MKOLUpdateElement element:mDownloadingSource){
               if(element.status== MKOLUpdateElement.SUSPENDED||element.status== MKOLUpdateElement.eOLDSNetError){
                   //暂停
                   pauseSize++;
               }else {
                   //下载
                   downloadSize++;
               }
        }
        for(MKOLUpdateElement element:mDownloadedSource){
            if(element.update){
                updateSize++;
            }
        }
        judgeAllHandleView(mAllUpdateView, updateSize);
        judgeAllHandleView(mAllPauseView, downloadSize);
        judgeAllHandleView(mAllDownloadView, pauseSize);
    }
    private void judgeAllHandleView(TextView mAllHandleView, int size){
        if(size>0){
            mAllHandleView.setClickable(true);
            mAllHandleView.setTextColor(getResources().getColor(R.color.white));
        }else {
            mAllHandleView.setClickable(false);
            mAllHandleView.setTextColor(getResources().getColor(R.color.order_fragment_divider));
        }
    }
    private void updateAllOfflineMap(){
        //更新所有离线地图
        for(MKOLUpdateElement element:mDownloadedSource){
            if(element.update){
                 mOffline.update(element.cityID);
            }
        }
    }
    private void downloadAllMap(){
        for(MKOLUpdateElement element:mDownloadingSource){
            if(element.status== MKOLUpdateElement.SUSPENDED||element.status== MKOLUpdateElement.eOLDSNetError){
                mOffline.start(element.cityID);
            }
        }
    }
    private void pauseAllMap(){
        for(MKOLUpdateElement element:mDownloadingSource){
            if(element.status!= MKOLUpdateElement.SUSPENDED){
                mOffline.pause(element.cityID);
            }
        }
    }
    private void checkCurrentNetWork(){
        //if(mDownloadingSource.size()>0){
            int netWorkType= NetUtil.netType(getActivity());
            if(netWorkType== NetUtil.NET_WIFI){
                mNetWorkTipView.setVisibility(View.GONE);
            }else if(netWorkType==NetUtil.NET_WWAN){
                mNetWorkTipView.setVisibility(View.VISIBLE);
                mNetWorkTipView.setText("当前非WIFI网络，下载会耗费手机流量");
                pauseAllMap();
            }else if(netWorkType==NetUtil.NET_NONE){
                mNetWorkTipView.setVisibility(View.VISIBLE);
                mNetWorkTipView.setText("网络关闭，请先打开网络");
            }
       //}
    }
    //采用通知的方式来进行操作
    @Subscribe
    public void onEventMainThread(LocManNotifyBean notifyBean) {
        if (TextUtils.equals(notifyBean.getNotifyTyp(), LocManNotifyType.NOTIFY_NETWORK_STATUS_CHANGE)) {
            //网络变化
            checkCurrentNetWork();
        }
    }
/*    @OnClick(value = {R.id.all_update_view, R.id.all_download_view, R.id.all_pause_view})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.all_update_view:
                updateAllOfflineMap();
                break;
            case R.id.all_download_view:
                downloadAllMap();
                break;
            case R.id.all_pause_view:
                pauseAllMap();
                break;
        }
    }*/
}
