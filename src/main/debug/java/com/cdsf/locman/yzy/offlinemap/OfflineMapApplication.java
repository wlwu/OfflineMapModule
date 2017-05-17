package com.cdsf.locman.yzy.offlinemap;

import com.baidu.mapapi.SDKInitializer;
import com.cdsf.locman.yzy.application.BaseApplication;

/**
 * Created by wuwenliang on 2017/5/10.
 */

public class OfflineMapApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
