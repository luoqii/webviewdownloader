package org.bbs.android.webmediadownload.webmediadownload;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebView = new WebView(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(mWebView, lp);

        initWebView(mWebView);

        String url = getStartUrl();
        mWebView.loadUrl(url);
    }

    @NonNull
    private String getStartUrl() {
        String url;
//        url = ("http://91.b9d.club/index.php");
//        url = ("http://youku.com");
        url = ("http://www.baidu.com");

        File urlFile = new File(Environment.getExternalStorageDirectory(), "url.txt");
        String line = null;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(urlFile));
            while ((line = r.readLine()) != null) {
                if (!TextUtils.isEmpty(line)
                        && !line.startsWith("#")) {
                    url = line;
                    Log.d(TAG, "found url:" + url);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void initWebView(WebView webView) {
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
    }

    class WebViewClient extends android.webkit.WebViewClient {

        private String mLastUrl;

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
//            parseUrl(url);
//            Log.d(TAG, "onLoadResource. url:" + url + " view:" + view);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            Log.d(TAG, "shouldOverrideUrlLoading. url:" + request.getUrl()
//                    + " mime:" + request.getRequestHeaders().get("mime")
//            );
            String mime= mime(request.getUrl().toString());
            if (!TextUtils.isEmpty(mime)) {
                if (mime.startsWith("image")) {
//                    return true;
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//            Log.d(TAG, "shouldInterceptRequest. url:" + request.getUrl());
            String mime= mime(request.getUrl().toString());
            if (!TextUtils.isEmpty(mime)) {
                if (mime.startsWith("image")) {
//                    return new WebResourceResponse("hack", "", null);
                }
                if (mime.startsWith("video")) {
                    String url = request.getUrl().toString();
                    if (!url.equals(mLastUrl)) {
                        postStartActivity(request.getUrl().toString(), mime);
                    }

                    mLastUrl = url;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        void postStartActivity(final String url, final String mime) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    startExternalDownloader(url, mime);
                }
            });
        }

        private void startExternalDownloader(final String url, final String mimeFromExtension) {
            final String referenceUrl  = mWebView.getOriginalUrl();
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    doStartDownload(url, referenceUrl, mimeFromExtension);
                }
            });
        }

        void doStartDownload(String url, String referenceUrl, String mime) {
            url = applyPatternReplace(url);
            saveUrl(referenceUrl,filename(url));
            Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setAction(Intent.ACTION_VIEW);
//                    intent.setData(request.getUrl());
            intent.putExtra(Intent.EXTRA_TITLE, "downalod");
            intent.putExtra(Intent.EXTRA_SUBJECT, "downalod");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            Log.d(TAG, "url          :" + url);
            Log.d(TAG, "reference url:" + referenceUrl);
            intent.putExtra("org.bbs.android.EXTRA_X_REFERENCE_URL", mWebView.getUrl());

            intent.setType(mime);
//                    intent.setType("video/*");
//                    ClipDescription clipDesc = new ClipDescription("download_title", new String[]{mime});
//                    ClipData clip = new ClipData(clipDesc, new ClipData.Item(request.getUrl().toString()));
//                    intent.setClipData(clip);
//                    intent.putExtra(Intent.EXTRA_STREAM, request.getUrl());
//                    intent.putExtra(Intent.EXTRA_TEXT, mime);
//                    intent.putExtra(Intent.EXTRA_TITLE, "title");
//                    intent.setData(request.getUrl());
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//                    intent = Intent.createChooser(intent, "download");
            startActivity(intent);
        }

        private void saveUrl(String url, String fileName) {
            Log.d(TAG, "fileName:" + fileName);
            fileName += ".txt";
            File dir = new File(Environment.getExternalStorageDirectory(), "wmd");
            dir.mkdirs();
            File urlFile = new File(dir, fileName);
            if (urlFile.exists()) {
                urlFile.delete();
            }

            urlFile.getParentFile().mkdirs();
            Writer w = null;
            try {
                w = new FileWriter(urlFile);
                w.write(url);
                w.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != w) {
                    try {
                        w.close();
                    } catch (IOException e) {
                        ;; // silent is golden
                    }
                }
            }
        }

        private String applyPatternReplace(String url) {
            Log.d(TAG, "before url:" + url);
            for (String key : App.REPLACE_PATTERN.keySet()) {
                if (url.contains(key)) {
                    url = url.replace(key, App.REPLACE_PATTERN.get(key));
                }
            }

            Log.d(TAG, "after  url:" + url);
            return url;
        }

        void parseUrl(String url){
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            int index = path.lastIndexOf(".");
            if (index > 0) {
                String extension = path.substring(index + 1);
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                Log.d(TAG, "mime:" + mime + "\textension:" + extension + "\t url:" + url);
            }
        }

        String filename(String url){
            String fileName = "";
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            int index = path.lastIndexOf("/");
            if (index > 0) {
                fileName = path.substring(index + 1);
            }

            return fileName;
        }

        String mime(String url){
            String mime = null;
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            int index = path.lastIndexOf(".");
            if (index > 0) {
                String extension = path.substring(index + 1);
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
//                Log.d(TAG, "mime:" + mime + "\textension:" + extension + "\t url:" + url);
            }

            return mime;
        }
    }
}
