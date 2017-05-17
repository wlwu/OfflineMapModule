package com.cdsf.locman.yzy.offlinemap.adapter;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.cdsf.locman.yzy.offlinemap.R;
import com.cdsf.locman.yzy.util.OfflineMapUtil;
import com.h6ah4i.android.widget.advrecyclerview.expandable.ExpandableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Administrator on 2016/8/26.
 */
public class OfflineMapDownloadAdapter extends AbstractExpandableItemAdapter<OfflineMapDownloadAdapter.MyGroupBaseViewHolder, OfflineMapDownloadAdapter.MyChildViewHolder> {
    private Context mContext;
    private List<MKOLUpdateElement> mDownloadingSource = new ArrayList<>();//正在下载
    private List<MKOLUpdateElement> mDownloadedSource = new ArrayList<>();//下载完成
    private final static int HEADER_DOWNLOADING = 1;//正在下载
    private final static int HEADER_DOWNLOADED = 2;//下载完成
    private final int TYPE_EMPTY_HEADER = 0x009;//空头
    private final int TYPE_DOWNLOADING_HEADER = 0x003;//头部
    private final int TYPE_DOWNLOADED_HEADER = 0x004;
    private final int TYPE_DOWNLOADING_CONTENT = 0x005;
    private final int TYPE_DOWNLOADED_CONTENT = 0x006;
    private MKOfflineMap mOffline;
    public OfflineMapDownloadAdapter(Context context, MKOfflineMap offline, List<MKOLUpdateElement> downlodingSource, List<MKOLUpdateElement> downlodedSource) {
        mContext = context;
        mOffline = offline;
        mDownloadingSource = downlodingSource;
        mDownloadedSource = downlodedSource;
        setHasStableIds(true);
    }

    public void setDataSource(List<MKOLUpdateElement> downlodingSource, List<MKOLUpdateElement> downlodedSource) {
        mDownloadingSource = downlodingSource;
        mDownloadedSource = downlodedSource;
    }

    @Override
    public int getGroupCount() {
        return (mDownloadingSource.size() + mDownloadedSource.size() + 2);
    }

    @Override
    public int getChildCount(int group) {
        int viewType = getGroupItemViewType(group);
        if (viewType == TYPE_DOWNLOADING_CONTENT || viewType == TYPE_DOWNLOADED_CONTENT) {
            return 1;
        } else {
            return 0;
        }
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
    public MyGroupBaseViewHolder onCreateGroupViewHolder(ViewGroup viewGroup, int viewType) {
        MyGroupBaseViewHolder holder = null;
        if (viewType == TYPE_EMPTY_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.offlinemap_download_expand_header_empty_layout, viewGroup, false);
            holder = new MyGroupEmptyHeaderViewHolder(view);
        } else if (viewType == TYPE_DOWNLOADING_HEADER || viewType == TYPE_DOWNLOADED_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.offlinemap_download_expand_header_layout, viewGroup, false);
            holder = new MyGroupHeaderViewHolder(view);
        } else if (viewType == TYPE_DOWNLOADING_CONTENT || viewType == TYPE_DOWNLOADED_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.offlinemap_download_expand_city_layout, viewGroup, false);
            holder = new MyGroupCityViewHolder(view);
        }
        return holder;
    }

    @Override
    public MyChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.offlinemap_download_expand_content_layout, viewGroup, false);
        return new MyChildViewHolder(v);
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        int type = TYPE_DOWNLOADING_HEADER;
        if (groupPosition == 0) {
            type = TYPE_DOWNLOADING_HEADER;
            if (mDownloadingSource.size() == 0) {
                type = TYPE_EMPTY_HEADER;
            }
        } else if (groupPosition == (mDownloadingSource.size() + 1)) {
            type = TYPE_DOWNLOADED_HEADER;
            if (mDownloadedSource.size() == 0) {
                type = TYPE_EMPTY_HEADER;
            }
        } else if (groupPosition >= 1 && groupPosition < (1 + mDownloadingSource.size())) {
            type = TYPE_DOWNLOADING_CONTENT;
        } else if (groupPosition > (1 + mDownloadingSource.size())) {
            type = TYPE_DOWNLOADED_CONTENT;
        }
        return type;
    }

    private interface Expandable extends ExpandableItemConstants {

    }

    private void handleElement(MyGroupCityViewHolder myGroupViewHolder, MKOLUpdateElement updateElement, int viewType) {
        if (viewType == TYPE_DOWNLOADING_CONTENT) {
            myGroupViewHolder.mUpdateTipView.setVisibility(View.GONE);
            myGroupViewHolder.mDownloadProgressView.setVisibility(View.VISIBLE);
        } else {
            myGroupViewHolder.mDownloadProgressView.setVisibility(View.GONE);
            if (updateElement.update) {
                myGroupViewHolder.mUpdateTipView.setVisibility(View.VISIBLE);
            } else {
                myGroupViewHolder.mUpdateTipView.setVisibility(View.GONE);
            }
        }
        myGroupViewHolder.mCityNameView.setText(updateElement.cityName);
        List<MKOLSearchRecord> recordSource=mOffline.searchCity(updateElement.cityName);
        String dataSize ="";
        if(recordSource!=null&&recordSource.size()>0){
            MKOLSearchRecord record=recordSource.get(0);
            dataSize = OfflineMapUtil.formatDataSize(record.size);
        }
        myGroupViewHolder.mCityDataSizeView.setText(dataSize);
        OfflineMapUtil.setOfflineDownloadStatusWithPercent(mContext, myGroupViewHolder.mCityDownloadStatusView, updateElement);
        myGroupViewHolder.mDownloadProgressView.setProgress(updateElement.ratio);
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
                myGroupViewHolder.mExpandView.setImageResource(R.drawable.icon_offline_map_shrink);
            } else {
                myGroupViewHolder.mExpandView.setImageResource(R.drawable.icon_offline_map_expand);
            }
        } else {
            Log.d("TAG", "teste");
        }
    }

    private void handleHeader(MyGroupHeaderViewHolder mHeaderViewHolder, int type) {
        mHeaderViewHolder.mHeaderLayout.setVisibility(View.VISIBLE);
        if (type == HEADER_DOWNLOADING) {
            mHeaderViewHolder.mHeaderTextView.setText("正在下载");
        } else {
            mHeaderViewHolder.mHeaderTextView.setText("下载完成");
        }

    }

    @Override
    public void onBindGroupViewHolder(MyGroupBaseViewHolder myGroupViewHolder, int groupPosition, int viewType) {
        if (viewType == TYPE_DOWNLOADING_HEADER) {
            MyGroupHeaderViewHolder headerViewHolder = (MyGroupHeaderViewHolder) myGroupViewHolder;
            handleHeader(headerViewHolder, HEADER_DOWNLOADING);
        } else if (viewType == TYPE_DOWNLOADED_HEADER) {
            MyGroupHeaderViewHolder headerViewHolder = (MyGroupHeaderViewHolder) myGroupViewHolder;
            handleHeader(headerViewHolder, HEADER_DOWNLOADED);
        } else if (viewType == TYPE_DOWNLOADING_CONTENT) {
            MyGroupCityViewHolder cityViewHolder = (MyGroupCityViewHolder) myGroupViewHolder;
            int position = groupPosition - 1;
            MKOLUpdateElement downloading = mDownloadingSource.get(position);
            handleElement(cityViewHolder, downloading, viewType);
        } else if (viewType == TYPE_DOWNLOADED_CONTENT) {
            MyGroupCityViewHolder cityViewHolder = (MyGroupCityViewHolder) myGroupViewHolder;
            int position = groupPosition - 2 - mDownloadingSource.size();
            MKOLUpdateElement downloaded = mDownloadedSource.get(position);
            handleElement(cityViewHolder, downloaded, viewType);
        }
    }

    @Override
    public void onBindChildViewHolder(MyChildViewHolder myChildViewHolder, int groupPosition, int childPosition, int type) {
        int viewType = getGroupItemViewType(groupPosition);
        if (viewType == TYPE_DOWNLOADING_CONTENT) {
            int position = groupPosition - 1;
            MKOLUpdateElement element = mDownloadingSource.get(position);
            handleChild(myChildViewHolder, element, viewType);
        } else if (viewType == TYPE_DOWNLOADED_CONTENT) {
            int position = groupPosition - 2 - mDownloadingSource.size();
            MKOLUpdateElement element = mDownloadedSource.get(position);
            handleChild(myChildViewHolder, element, viewType);
        }

    }

    private void handleChild(MyChildViewHolder myChildViewHolder, final MKOLUpdateElement element, int viewType) {
        myChildViewHolder.mDelteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(element);
            }
        });
        if (viewType == TYPE_DOWNLOADING_CONTENT) {
            //正在下载
            myChildViewHolder.mDownloadBtn.setEnabled(true);
            if (element.status == MKOLUpdateElement.WAITING||element.status== MKOLUpdateElement.DOWNLOADING) {
                myChildViewHolder.mDownloadBtn.setText("暂停下载");
            } else {
                myChildViewHolder.mDownloadBtn.setText("开始下载");
            }
            myChildViewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (element.status == MKOLUpdateElement.WAITING||element.status== MKOLUpdateElement.DOWNLOADING) {
                        mOffline.pause(element.cityID);
                    } else {
                        mOffline.start(element.cityID);
                    }
                }
            });
        } else if (viewType == TYPE_DOWNLOADED_CONTENT) {
            //已经下载
            myChildViewHolder.mDownloadBtn.setText("下载更新");
            if (element.update) {
                myChildViewHolder.mDownloadBtn.setEnabled(true);
            } else {
                myChildViewHolder.mDownloadBtn.setEnabled(false);
            }
            myChildViewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOffline.update(element.cityID);
                }
            });
        }
    }

    private void showDeleteDialog(final MKOLUpdateElement element) {
        final MaterialDialog dialog = new MaterialDialog(mContext);
        dialog.setMessage("离线地图包删除后需要重新下载，确认删除?");
        dialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mOffline.remove(element.cityID);
            }
        });
        dialog.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(MyGroupBaseViewHolder myBaseViewHolder, int groupPosition, int x, int y, boolean expand) {
        int viewType = getGroupItemViewType(groupPosition);
        if (viewType == TYPE_DOWNLOADING_CONTENT || viewType == TYPE_DOWNLOADED_CONTENT) {
            return true;
        } else {
            return false;
        }
    }

    public static class MyGroupBaseViewHolder extends AbstractExpandableItemViewHolder {
        public MyGroupBaseViewHolder(View v) {
            super(v);
        }
    }

    public static class MyGroupHeaderViewHolder extends MyGroupBaseViewHolder {
        RelativeLayout mHeaderLayout;//头文件
        TextView mHeaderTextView;//头信息

        public MyGroupHeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mHeaderLayout=(RelativeLayout)view.findViewById(R.id.header_layout);
            mHeaderTextView=(TextView)view.findViewById(R.id.header_view);
        }
    }

    public static class MyGroupEmptyHeaderViewHolder extends MyGroupBaseViewHolder {
        public MyGroupEmptyHeaderViewHolder(View v) {
            super(v);
        }
    }

    public static class MyGroupCityViewHolder extends MyGroupBaseViewHolder {
        RelativeLayout mCityLayoutView;//城市列信息
        TextView mCityNameView;//城市名称
        TextView mCityDataSizeView;//数据大小
        TextView mCityDownloadStatusView;//下载状态
        ImageView mExpandView;//展开
        ProgressBar mDownloadProgressView;
        TextView mUpdateTipView;

        public MyGroupCityViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mCityLayoutView=(RelativeLayout)view.findViewById(R.id.city_layout);
            mCityNameView=(TextView)view.findViewById(R.id.city_name_view);
            mCityDataSizeView=(TextView)view.findViewById(R.id.city_data_size_view);
            mCityDownloadStatusView=(TextView)view.findViewById(R.id.city_download_status);
            mExpandView=(ImageView)view.findViewById(R.id.expand_tag);
            mDownloadProgressView=(ProgressBar)view.findViewById(R.id.download_progress);
            mUpdateTipView=(TextView)view.findViewById(R.id.update_tip_view);
        }
    }

    public static class MyChildViewHolder extends AbstractExpandableItemViewHolder {
        Button mDownloadBtn;//下载按钮
        Button mDelteBtn;//删除按钮

        public MyChildViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mDownloadBtn=(Button)view.findViewById(R.id.download_btn);
            mDelteBtn=(Button)view.findViewById(R.id.delete_btn);
        }
    }
}

