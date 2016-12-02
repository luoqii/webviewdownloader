package org.bbs.android.webmediadownload.webmediadownload;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
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

        String url;
        url = ("http://91.b9d.club/index.php");
//        url = ("http://youku.com");
        url = ("http://www.baidu.com");
        mWebView.loadUrl(url);
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
            parseUrl(url);
            Log.d(TAG, "onLoadResource. url:" + url + " view:" + view);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading. url:" + request.getUrl()
//                    + " mime:" + request.getRequestHeaders().get("mime")
            );
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
            Log.d(TAG, "shouldInterceptRequest. url:" + request.getUrl());
            String mime= mime(request.getUrl().toString());
            if (!TextUtils.isEmpty(mime)) {
                if (mime.startsWith("image")) {
//                    return new WebResourceResponse("hack", "", null);
                }
                if (mime.startsWith("video")) {
                    String url = request.getUrl().toString();
                    if (!url.equals(mLastUrl)) {
                        startExternalDownloader(request, mime);
                    }

                    mLastUrl = url;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        private void startExternalDownloader(WebResourceRequest request, String mimeInHeader) {
            Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setAction(Intent.ACTION_VIEW);
//                    intent.setData(request.getUrl());
            intent.putExtra(Intent.EXTRA_TITLE, "downalod");
            intent.putExtra(Intent.EXTRA_SUBJECT, "downalod");
            intent.putExtra(Intent.EXTRA_TEXT, request.getUrl().toString());

            intent.setType(mimeInHeader);
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

        String mime(String url){
            String mime = null;
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            int index = path.lastIndexOf(".");
            if (index > 0) {
                String extension = path.substring(index + 1);
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                Log.d(TAG, "mime:" + mime + "\textension:" + extension + "\t url:" + url);
            }

            return mime;
        }
    }
}
