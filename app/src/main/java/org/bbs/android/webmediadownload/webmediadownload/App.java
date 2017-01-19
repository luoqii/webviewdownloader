package org.bbs.android.webmediadownload.webmediadownload;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiiluo on 1/19/17.
 */
public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    public static Map<String, String> REPLACE_PATTERN = new HashMap<>();
    @Override
    public void onCreate() {
        super.onCreate();

        initReplacePattern();
    }

    void initReplacePattern() {
        File urlFile = new File(Environment.getExternalStorageDirectory(), "url.replace.txt");
        String line = null;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(urlFile));
            while ((line = r.readLine()) != null) {
                if (!TextUtils.isEmpty(line)
                        && !line.startsWith("#")) {
                    String[] splits = line.split(" ");
                    Log.d(TAG, "replace " + splits[0] + " with " + splits[1]);
                    REPLACE_PATTERN.put(splits[0], splits[1]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
