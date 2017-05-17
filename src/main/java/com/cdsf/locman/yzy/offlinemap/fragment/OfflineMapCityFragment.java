package com.cdsf.locman.yzy.offlinemap.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.offlinemap.activity.OfflineMapManageActivity;
import com.cdsf.locman.yzy.offlinemap.adapter.OfflineMapCityAdapter;
import com.cdsf.locman.yzy.util.LocateUtils;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/10/27.
 */
public class OfflineMapCityFragment extends OfflineMapBaseFragment{
    RecyclerView mRecyclerView;
    private MKOfflineMap mOffline;
    private MKOLSearchRecord mLocRecordBean;//当前记录的位置
    private MKOLSearchRecord mBaseRecordBean;//基础包
    private List<MKOLSearchRecord> mLocCitySource=new ArrayList<>();//当前位置
    private List<MKOLSearchRecord> hotCitySource=new ArrayList<>();//热闹城市列表
    private List<MKOLSearchRecord> citySource=new ArrayList<>();//所有城市
    private List<MKOLUpdateElement> mCityUpdateSource=new ArrayList<>();//所有更新信息
    private LocateUtils locateUtils;//地图公共使用类
    private OfflineMapCityAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offlinemap_map_city_layout, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        return view;
    }
    private void initView(View view){
        mRecyclerView=(RecyclerView)view.findViewById(R.id.recyclerview);
    }
    public void setMKOfflineMap(MKOfflineMap offlineMap){
        mOffline=offlineMap;
    }
    private void setupRecyclerView(){
        mAdapter=new OfflineMapCityAdapter(getActivity(),mLocCitySource,hotCitySource,citySource,mCityUpdateSource,mBaseRecordBean);
        mAdapter.setMKOfflineMap(mOffline);
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
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OfflineMapManageActivity mActivity=(OfflineMapManageActivity)getActivity();
        mOffline=mActivity.getMKOfflineMap();
        setupRecyclerView();
        fetchCity();
        locateUtils = new LocateUtils(getActivity());
        locateUtils.setListener(bdLocationListener);
        locateUtils.startLocate();
    }
    public void notifyUpdate(){
        fetchCity();
    }
    private void fetchCity(){
        hotCitySource = mOffline.getHotCityList();
        hotCitySource.add(0, null);
        citySource=mOffline.getOfflineCityList();
        mBaseRecordBean=citySource.get(0);
        citySource.add(0, null);
        setLocRecordSource();
        mCityUpdateSource=mOffline.getAllUpdateInfo();
        if(mCityUpdateSource==null){
            mCityUpdateSource=new ArrayList<>();
        }
        if(mAdapter!=null){
            mAdapter.setDataSource(mLocCitySource, hotCitySource, citySource, mCityUpdateSource, mBaseRecordBean);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void setLocRecordSource(){
        mLocCitySource.clear();
        mLocCitySource.add(0,null);
        mLocCitySource.add(mLocRecordBean);
        mLocCitySource.add(mBaseRecordBean);
    }


    //定位监听
    BDLocationListener bdLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(final BDLocation loc) {
           getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   locateUtils.stopLocate();
                   if(loc!=null&&mOffline!=null){
                       if(!TextUtils.isEmpty(loc.getCity())){
                           List<MKOLSearchRecord> source=mOffline.searchCity(loc.getCity());
                           if(source!=null&&source.size()>0){
                               mLocRecordBean=source.get(0);
                           }
                           setLocRecordSource();
                           mAdapter.setDataSource(mLocCitySource,hotCitySource,citySource,mCityUpdateSource,mBaseRecordBean);
                           mAdapter.notifyDataSetChanged();
                       }
                   }
               }
           });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };
}
