package org.bbs.android.webmediadownload.webmediadownload;

import android.app.Application;
import android.media.DeniedByServerException;
import android.os.Environment;
import android.text.TextUtils;

import org.bbs.android.commonlib.ExceptionCatcher;
import org.bbs.android.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qiiluo on 1/19/17.
 */
public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    public static File CONFIG_DIR;
    public static Map<String, String> REPLACE_PATTERN = new HashMap<>();
    public static List<String> DENIED_HOST = new ArrayList();

    @Override
    public void onCreate() {
        super.onCreate();

        ExceptionCatcher.attachExceptionHandler(this);

        CONFIG_DIR = Environment.getExternalStorageDirectory();
        Log.d(TAG, "config dir:" + CONFIG_DIR.getPath());
        initReplacePattern(CONFIG_DIR);
        initDeniedHost(CONFIG_DIR);
    }

    void initReplacePattern(File configDir) {
        File urlFile = new File(configDir, "url.replace.txt");
        Log.d(TAG, "url.replace.txt:" + urlFile);
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

    static public void  initDeniedHost(File configDir) {
        File urlFile = new File(configDir, "url.denied.txt");
        Log.d(TAG, "url.denied.txt:" + urlFile);
        String line = null;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(urlFile));
            while ((line = r.readLine()) != null) {
                if (!TextUtils.isEmpty(line)
                        && !line.startsWith("#")) {
                    line = line.trim();
                    Log.d(TAG, "denied host " + line);
                    DENIED_HOST.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
