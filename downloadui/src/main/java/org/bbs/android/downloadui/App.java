package org.bbs.android.downloadui;

import android.app.Application;

/**
 * Created by qiiluo on 1/4/17.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DownloadManagerLite.getInstance().init(this);
        DownloadManagerLite.getInstance().setMaxConcurrentDownload(100);
    }
}
