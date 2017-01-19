package org.bbs.android.downloadui;

;import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.ThreadLocalSelectArg;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * a lite wrapper for {@link DownloadManager}
 * add support concurrent downloadings
 *
 * Created by qiiluo on 12/28/16.
 */

public class DownloadManagerLite {
    private static final boolean DEBUG = true;
    private static final String TAG = DownloadManagerLite.class.getSimpleName();

    private static DownloadManagerLite sInstance;
    private int mCount;
    private DownloadManager mDownloadM;
    private List<Request> mRunningQueue;
    private List<Request> mFinishedRequest;
    private DatabaseHelper mDbHelper = null;

    public static DownloadManagerLite getInstance(){
        if (null == sInstance){
            sInstance = new DownloadManagerLite();
        }

        return sInstance;
    }

    public void init(Context context) {
        mDownloadM = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        getHelper(context);
        try {
            syncFromDb();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        new MointorThread().start();
    }

    private void syncFromDb() throws SQLException {

        mRunningQueue = new ArrayList<>();
        mFinishedRequest = new ArrayList<>();
        Dao<Request, Integer> dao = mDbHelper.getDao(Request.class);
        QueryBuilder<Request, Integer> builder = dao.queryBuilder();
        builder.where().eq(Request.FIELD_DOWNLOD_ID, 0);
        mRunningQueue = dao.query(builder.prepare());
    }

    public void setMaxConcurrentDownload(int count) {
        mCount = count;
        tryDownload();
    }

    private DatabaseHelper getHelper(Context context) {
        if (mDbHelper == null) {
            mDbHelper =
                    OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return mDbHelper;
    }

    public void enqueue(Request r) {
        mRunningQueue.add(r);

        try {
            Dao<Request, Integer> dao = mDbHelper.getDao(Request.class);
            dao.create(r);
            tryDownload();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void tryDownload() {
        final int SIZE = mRunningQueue.size();
        if (DEBUG) {
            Log.d(TAG, "running Q size:" + SIZE + " maxConcurrentDownloadlSize:" + mCount);
        }

        if (SIZE < mCount) {
            doDownload(mRunningQueue.remove(0));
        }
    }

    private void doDownload(Request r) {
        DownloadManager.Request request = fromRequest(r);
        mDownloadM.enqueue(request);
    }

    @NonNull
    private DownloadManager.Request fromRequest(Request r) {
        return new DownloadManager.Request(Uri.parse(r.url));
    }

    @DatabaseTable
    public static class Request {
        public static final String FIELD_DOWNLOD_ID = "downloadId";

        @DatabaseField(generatedId = true)
        private Integer id;


        @DatabaseField
        long startTime;
        @DatabaseField
        boolean finished;

        @DatabaseField
        String url;
        @DatabaseField
        String filePath;
        @DatabaseField
        String referenceUrl;
        @DatabaseField
        long lastModifyTime;
        @DatabaseField
        long byteDownloadedSofar;
        @DatabaseField
        int status;
        @DatabaseField
        int reason;

        /** id for android DownloadManager */
        @DatabaseField()
        long downloadId;

    }

    class MointorThread extends Thread {
        @Override
        public void run() {
            super.run();

            while (true) {
                DownloadManager.Query q = new DownloadManager.Query();
                Cursor c = null;
                try {
                    c = mDownloadM.query(q);
                    if (DEBUG) {
                        Log.d(TAG, "count:" + c.getCount());
                    }
                    while (c != null && !c.isLast()) {
                        dump(c);
                        c.moveToNext();
                    }
                } finally {
                    if (null != c) {
                        c.close();
                    }
                }

                SystemClock.sleep(1000);
            }
        }

        private void dump(Cursor c) {
//            String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
//            String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
//            long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
//            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            Log.d(TAG, "dump cousor");
//            Log.d(TAG, "uri :" + uri);
//            Log.d(TAG, "title: " + title);
//            Log.d(TAG, "id :" + id);
//            Log.d(TAG, "status:" + status);
        }
    }
}
