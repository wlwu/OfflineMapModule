package com.cdsf.locman.yzy.offlinemap.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.cdsf.locman.yzy.Bean.LocManNotifyBean;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.util.LocManNotifyType;
import com.cdsf.locman.yzy.util.OfflineMapUtil;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/8/26.
 */
public class OfflineMapCityAdapter extends AbstractExpandableItemAdapter<OfflineMapCityAdapter.MyGroupViewHolder, OfflineMapCityAdapter.MyChildViewHolder> {
    private Context mContext;
    private MKOfflineMap mOffline;
    private MKOLSearchRecord mBaseRecordBean;//基础包
    private List<MKOLSearchRecord> mLocCitySource = new ArrayList<>();//当前位置
    private List<MKOLSearchRecord> mHotCitySource = new ArrayList<>();//热闹城市列表
    private List<MKOLSearchRecord> mCitySource = new ArrayList<>();//所有城市
    private List<MKOLUpdateElement> mCityUpdateSource=new ArrayList<>();//所有更新信息
    public OfflineMapCityAdapter(Context context, List<MKOLSearchRecord> locCitySource, List<MKOLSearchRecord> hotCitySource, List<MKOLSearchRecord> citySource, List<MKOLUpdateElement> cityUpdateSource, MKOLSearchRecord baseRecordBean) {
        mContext = context;
        mLocCitySource = locCitySource;
        mHotCitySource = hotCitySource;
        mCitySource = citySource;
        mCityUpdateSource=cityUpdateSource;
        mBaseRecordBean=baseRecordBean;
        setHasStableIds(true);
    }

    public void setDataSource(List<MKOLSearchRecord> locCitySource, List<MKOLSearchRecord> hotCitySource, List<MKOLSearchRecord> citySource, List<MKOLUpdateElement> cityUpdateSource, MKOLSearchRecord baseRecordBean) {
        mLocCitySource = locCitySource;
        mHotCitySource = hotCitySource;
        mCitySource = citySource;
        mCityUpdateSource=cityUpdateSource;
        mBaseRecordBean=baseRecordBean;
    }

    public void setMKOfflineMap(MKOfflineMap offline){
        mOffline=offline;
    }
    @Override
    public int getGroupCount() {
        return (mLocCitySource.size() + mHotCitySource.size() + mCitySource.size());
    }

    @Override
    public int getChildCount(int i) {
        return 1;
    }

    @Override
    public long getGroupId(int group) {
        return group;
    }

    @Override
    public long getChildId(int i, int child) {
        return child;
    }

    @Override
    public MyGroupViewHolder onCreateGroupViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.offlinemap_city_expand_header_layout, viewGroup, false);
        return new MyGroupViewHolder(v);
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.offlinemap_city_expand_content_layout, viewGroup, false);
        return new MyChildViewHolder(v);
    }

    private interface Expandable extends ExpandableItemConstants {

    }
    private void handleCityGroupView(MyGroupViewHolder myGroupViewHolder,final MKOLSearchRecord record){
        String dataSize= OfflineMapUtil.formatDataSize(record.size);
        myGroupViewHolder.mCityDataSizeView.setText(dataSize);
        MKOLUpdateElement mUpdateElement=null;
        for(MKOLUpdateElement updateElement:mCityUpdateSource){
            if(updateElement.cityID==record.cityID){
                mUpdateElement=updateElement;
                break;
            }
        }
        if(mUpdateElement!=null){
            //找到了信息
            myGroupViewHolder.mCityDataDownloadView.setImageResource(R.drawable.icon_offline_city_downloaded);
            myGroupViewHolder.mCityDownloadStatusView.setVisibility(View.VISIBLE);
            //找到了设备信息
            OfflineMapUtil.setOfflineDownloadStatus(mContext,myGroupViewHolder.mCityDownloadStatusView,mUpdateElement);
        }else {
            //未找到信息
            myGroupViewHolder.mCityDownloadStatusView.setText("");
            myGroupViewHolder.mCityDownloadStatusView.setVisibility(View.GONE);
            myGroupViewHolder.mCityDataDownloadView.setImageResource(R.drawable.icon_offline_city_download);
        }
        myGroupViewHolder.mCityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean startDownlod=false;
                if(mBaseRecordBean!=null){
                    MKOLUpdateElement baseElement=mOffline.getUpdateInfo(mBaseRecordBean.cityID);
                    if(baseElement==null){
                        //判断基础地图状态
                        startDownlod= mOffline.start(mBaseRecordBean.cityID);
                    }else if(baseElement.status!= MKOLUpdateElement.FINISHED){
                        startDownlod=mOffline.start(mBaseRecordBean.cityID);
                    }
                }
                MKOLUpdateElement downloadElement=mOffline.getUpdateInfo(record.cityID);
                if(downloadElement==null){
                    startDownlod=mOffline.start(record.cityID);
                }
                if(startDownlod==false){
                    //已经有了下载 那么就跳转到下载管理去
                    LocManNotifyBean notifyBean = new LocManNotifyBean();
                    notifyBean.setNotifyTyp(LocManNotifyType.NOTIFY_OFFLINE_SCROLL_TO_DOWNLOAD);
                    notifyBean.setNotifyData("");
                    EventBus.getDefault().post(notifyBean);
                }
                if(startDownlod){
      /*              mCityUpdateSource = mOffline.getAllUpdateInfo();
                    if (mCityUpdateSource == null) {
                        mCityUpdateSource = new ArrayList<MKOLUpdateElement>();
                    }
                    notifyDataSetChanged();*/
                }
            }
        });
    }
    @Override
    public void onBindGroupViewHolder(MyGroupViewHolder myGroupViewHolder, int groupPosition, int viewType) {
        if (groupPosition < mLocCitySource.size()) {
            //现在是处于定位模块
            if (groupPosition == 0) {
                myGroupViewHolder.mHeaderLayout.setVisibility(View.VISIBLE);
                myGroupViewHolder.mHeaderTextView.setText("当前城市");
                myGroupViewHolder.mCityLayout.setVisibility(View.GONE);
            } else {
                if (groupPosition == 1) {
                    MKOLSearchRecord locRecord = mLocCitySource.get(1);
                    if (locRecord == null) {
                        myGroupViewHolder.mCityNameView.setText("定位中");
                        setGroupViewVisible(myGroupViewHolder, false,false);
                        myGroupViewHolder.mExpandTagView.setVisibility(View.GONE);
                    } else {
                        myGroupViewHolder.mCityNameView.setText(locRecord.cityName);
                        setGroupViewVisible(myGroupViewHolder, true, false);
                        handleCityGroupView(myGroupViewHolder, locRecord);
                    }
                } else if (groupPosition == 2){
                    MKOLSearchRecord locRecord =mLocCitySource.get(2);
                    if(locRecord!=null){
                        myGroupViewHolder.mCityNameView.setText(locRecord.cityName);
                        setGroupViewVisible(myGroupViewHolder, true, true);
                        handleCityGroupView(myGroupViewHolder, locRecord);
                    }else {
                        myGroupViewHolder.mHeaderLayout.setVisibility(View.GONE);
                        myGroupViewHolder.mCityLayout.setVisibility(View.GONE);
                        myGroupViewHolder.mExpandTagView.setVisibility(View.GONE);
                        myGroupViewHolder.mCityMessageLayout.setVisibility(View.GONE);
                    }
                }
            }
        } else if (groupPosition >= mLocCitySource.size() && groupPosition < (mLocCitySource.size() + mHotCitySource.size())) {
            //热门城市
            if (groupPosition == mLocCitySource.size()) {
                myGroupViewHolder.mHeaderLayout.setVisibility(View.VISIBLE);
                myGroupViewHolder.mHeaderTextView.setText("热门城市");
                myGroupViewHolder.mCityLayout.setVisibility(View.GONE);
            } else {
                int position = groupPosition - mLocCitySource.size();
                MKOLSearchRecord record = mHotCitySource.get(position);
                setGroupViewVisible(myGroupViewHolder, true, false);
                myGroupViewHolder.mCityNameView.setText(record.cityName);
                handleCityGroupView(myGroupViewHolder, record);
            }
        } else if (groupPosition >= (mLocCitySource.size() + mHotCitySource.size())) {
            //城市列表
            if (groupPosition == (mLocCitySource.size() + mHotCitySource.size())) {
                myGroupViewHolder.mHeaderLayout.setVisibility(View.VISIBLE);
                myGroupViewHolder.mHeaderTextView.setText("按地区查询");
                myGroupViewHolder.mCityLayout.setVisibility(View.GONE);
            } else {
                int position = groupPosition - mLocCitySource.size() - mHotCitySource.size();
                MKOLSearchRecord record = mCitySource.get(position);
                myGroupViewHolder.mCityNameView.setText(record.cityName);
                if (record.cityType == 1) {
                    //省份
                    setGroupViewVisible(myGroupViewHolder, false,false);
                    final int expandState = myGroupViewHolder.getExpandStateFlags();
                    if ((expandState & Expandable.STATE_FLAG_IS_UPDATED) != 0) {
                        boolean isExpanded;
                        boolean animateIndicator = ((expandState & Expandable.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED) != 0);
                        if ((expandState & Expandable.STATE_FLAG_IS_EXPANDED) != 0) {
                            isExpanded = true;
                        } else {
                            isExpanded = false;
                        }
                        if (isExpanded) {
                            myGroupViewHolder.mExpandTagView.setImageResource(R.drawable.icon_offline_map_shrink);
                        } else {
                            myGroupViewHolder.mExpandTagView.setImageResource(R.drawable.icon_offline_map_expand);
                        }
                    } else {
                        Log.d("TAG", "teste");
                    }
                } else {
                    if(position==1){
                        setGroupViewVisible(myGroupViewHolder,true,true);
                    }else {
                        setGroupViewVisible(myGroupViewHolder,true,false);
                    }
                    handleCityGroupView(myGroupViewHolder, record);
                }
            }
        }

    }
    private void setGroupViewVisible(MyGroupViewHolder myGroupViewHolder,boolean city,boolean basicCity){
        if(city){
            myGroupViewHolder.mHeaderLayout.setVisibility(View.GONE);
            myGroupViewHolder.mCityMessageLayout.setVisibility(View.VISIBLE);
            myGroupViewHolder.mExpandTagView.setVisibility(View.GONE);
            myGroupViewHolder.mCityLayout.setVisibility(View.VISIBLE);
            myGroupViewHolder.mCityLayout.setClickable(true);
            if(basicCity){
                myGroupViewHolder.mBasicCityTipView.setVisibility(View.VISIBLE);
            }else {
                myGroupViewHolder.mBasicCityTipView.setVisibility(View.GONE);
            }
        }else {
            myGroupViewHolder.mHeaderLayout.setVisibility(View.GONE);
            myGroupViewHolder.mCityMessageLayout.setVisibility(View.GONE);
            myGroupViewHolder.mExpandTagView.setVisibility(View.VISIBLE);
            myGroupViewHolder.mCityLayout.setVisibility(View.VISIBLE);
            myGroupViewHolder.mCityLayout.setClickable(false);
            myGroupViewHolder.mBasicCityTipView.setVisibility(View.GONE);
            //需要处理下载的状态
            myGroupViewHolder.mCityDownloadStatusView.setText("");
            myGroupViewHolder.mCityDownloadStatusView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onBindChildViewHolder(MyChildViewHolder myChildViewHolder, int groupPosition, int childPosition, int viewType) {
        setChildRecycleView(myChildViewHolder.mRecyclerView);
        int position = groupPosition - mLocCitySource.size() - mHotCitySource.size();
        MKOLSearchRecord recordBean = mCitySource.get(position);
        OfflineMapProvinceCityAdapter adapter = new OfflineMapProvinceCityAdapter(mContext, recordBean.childCities,mOffline,mBaseRecordBean,recordBean);
        myChildViewHolder.mRecyclerView.setAdapter(adapter);
    }

    private void setChildRecycleView(RecyclerView mRecyclerView) {
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupViewHolder myBaseViewHolder, int groupPosition, int x, int y, boolean expand) {
        if (groupPosition > (mLocCitySource.size() + mHotCitySource.size())) {
            int position = groupPosition - mLocCitySource.size() - mHotCitySource.size();
            MKOLSearchRecord recordBean = mCitySource.get(position);
            if (recordBean != null && recordBean.cityType == 1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static class MyGroupViewHolder extends AbstractExpandableItemViewHolder {
        RelativeLayout mHeaderLayout;//头文件
        TextView mHeaderTextView;//头信息
        RelativeLayout mCityLayout;//城市信息
        TextView mBasicCityTipView;//基础包提示信息
        TextView mCityNameView;//城市名字
        TextView mCityDownloadStatusView;//下载状态信息
        ImageView mExpandTagView;//展开
        LinearLayout mCityMessageLayout;//城市数据信息
        TextView mCityDataSizeView;//城市数据大小
        ImageView mCityDataDownloadView;//下载城市数据

        public MyGroupViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mHeaderLayout=(RelativeLayout)view.findViewById(R.id.header_layout);
            mHeaderTextView=(TextView)view.findViewById(R.id.header_view);
            mCityLayout=(RelativeLayout)view.findViewById(R.id.city_layout);
            mBasicCityTipView=(TextView)view.findViewById(R.id.basic_city_tip_view);
            mCityNameView=(TextView)view.findViewById(R.id.city_name_view);
            mCityDownloadStatusView=(TextView)view.findViewById(R.id.city_download_status);
            mExpandTagView=(ImageView)view.findViewById(R.id.expand_tag);
            mCityMessageLayout=(LinearLayout)view.findViewById(R.id.city_message_layout);
            mCityDataSizeView=(TextView) view.findViewById(R.id.city_data_size_view);
            mCityDataDownloadView=(ImageView)view.findViewById(R.id.city_data_download);
        }
    }

    public static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        public RecyclerView mRecyclerView;

        public MyChildViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mRecyclerView=(RecyclerView)view.findViewById(R.id.province_recyclerview);
        }
    }
}

