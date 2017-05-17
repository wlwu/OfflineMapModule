package com.cdsf.locman.yzy.offlinemap.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.cdsf.locman.yzy.Bean.LocManNotifyBean;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.util.LocManNotifyType;
import com.cdsf.locman.yzy.util.OfflineMapUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/9/8.
 * 省份下面的城市
 */
public class OfflineMapProvinceCityAdapter extends RecyclerView.Adapter<OfflineMapProvinceCityAdapter.MyCityViewHolder> {
    private MKOLSearchRecord mBaseRecordBean;//基础包
    private MKOLSearchRecord mProviceRecord;//省记录
    private List<MKOLSearchRecord> mDataSource = new ArrayList<>();
    private List<MKOLUpdateElement> mCityUpdateSource = new ArrayList<>();//所有更新信息
    private MKOfflineMap mOffline;
    private Context mContext;
    public OfflineMapProvinceCityAdapter(Context context, List<MKOLSearchRecord> dataSource, MKOfflineMap offline, MKOLSearchRecord baseRecordBean, MKOLSearchRecord proviceRecord) {
        mContext = context;
        mDataSource = dataSource;
        mOffline = offline;
        mBaseRecordBean = baseRecordBean;
        mProviceRecord = proviceRecord;
        mCityUpdateSource = mOffline.getAllUpdateInfo();
        if (mCityUpdateSource == null) {
            mCityUpdateSource = new ArrayList<>();
        }
        if (mDataSource.size() > 1) {
            MKOLSearchRecord record=mDataSource.get(0);
            if(record.cityType==1){
                //现在就是省份了
            }else {
                mDataSource.add(0, mProviceRecord);
            }
        }
    }

    @Override
    public MyCityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.offlinemap_province_city_item_layout, parent, false);
        MyCityViewHolder holder = new MyCityViewHolder(view);
        return holder;
    }


    private void handleCityView(MyCityViewHolder mHolder, final MKOLSearchRecord record) {
        String dataSize = OfflineMapUtil.formatDataSize(record.size);
        mHolder.mCityDataSizeView.setText(dataSize);
        MKOLUpdateElement mUpdateElement = null;
        for (MKOLUpdateElement updateElement : mCityUpdateSource) {
            if (updateElement.cityID == record.cityID) {
                mUpdateElement = updateElement;
                break;
            }
        }
        if (mUpdateElement != null) {
            //找到了信息
            mHolder.mCityDataDownloadView.setImageResource(R.drawable.icon_offline_city_downloaded);
            mHolder.mCityDownloadStatusView.setVisibility(View.VISIBLE);
            //找到了设备信息
            OfflineMapUtil.setOfflineDownloadStatus(mContext, mHolder.mCityDownloadStatusView, mUpdateElement);
        } else {
            //未找到信息
            mHolder.mCityDownloadStatusView.setText("");
            mHolder.mCityDownloadStatusView.setVisibility(View.GONE);
            mHolder.mCityDataDownloadView.setImageResource(R.drawable.icon_offline_city_download);
        }
        mHolder.mCityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean startDownlod = false;
                if (mBaseRecordBean != null) {
                    MKOLUpdateElement baseElement = mOffline.getUpdateInfo(mBaseRecordBean.cityID);
                    if (baseElement == null) {
                        //判断基础地图状态
                        startDownlod = mOffline.start(mBaseRecordBean.cityID);
                    } else if (baseElement.status != MKOLUpdateElement.FINISHED) {
                        startDownlod = mOffline.start(mBaseRecordBean.cityID);
                    }
                }
                MKOLUpdateElement downloadElement = mOffline.getUpdateInfo(record.cityID);
                if (downloadElement == null) {
                    startDownlod = mOffline.start(record.cityID);
                }
                if (startDownlod == false) {
                    //已经有了下载 那么就跳转到下载管理去
                    LocManNotifyBean notifyBean = new LocManNotifyBean();
                    notifyBean.setNotifyTyp(LocManNotifyType.NOTIFY_OFFLINE_SCROLL_TO_DOWNLOAD);
                    notifyBean.setNotifyData("");
                    EventBus.getDefault().post(notifyBean);
                }
                if (startDownlod) {
/*                    mCityUpdateSource = mOffline.getAllUpdateInfo();
                    if (mCityUpdateSource == null) {
                        mCityUpdateSource = new ArrayList<MKOLUpdateElement>();
                    }
                    notifyDataSetChanged();*/
                }
            }
        });
    }


    @Override
    public void onBindViewHolder(MyCityViewHolder holder, int position) {
        MKOLSearchRecord cityRecord = mDataSource.get(position);
        if(position==0&&cityRecord.cityType==1){
            holder.mCityNameView.setText("全省地图");
        }else {
            holder.mCityNameView.setText(cityRecord.cityName);
        }
        handleCityView(holder, cityRecord);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public static class MyCityViewHolder extends RecyclerView.ViewHolder {
        TextView mCityNameView;//城市名称
        RelativeLayout mCityLayout;
        TextView mCityDownloadStatusView;//下载状态view
        TextView mCityDataSizeView;//数据大
        ImageView mCityDataDownloadView;//地图下载

        public MyCityViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mCityNameView=(TextView)view.findViewById(R.id.city_name_view);
            mCityLayout=(RelativeLayout)view.findViewById(R.id.city_layout);
            mCityDownloadStatusView=(TextView)view.findViewById(R.id.city_download_status);
            mCityDataSizeView=(TextView)view.findViewById(R.id.city_data_size_view);
            mCityDataDownloadView=(ImageView)view.findViewById(R.id.city_data_download);
        }
    }

}
