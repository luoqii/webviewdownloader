package org.bbs.android.downloadui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Text;

import java.io.File;

/**
 * A placeholder fragment containing a simple view.
 */
public class EditActivityFragment extends Fragment {
    private static final String TAG = EditActivityFragment.class.getSimpleName();
    public static final String EXTRA_X_REFERENCE_URL = "org.bbs.android.EXTRA_X_REFERENCE_URL";
    private EditText mUrl;
    private EditText mFileName;
    private Button mDownload;
    private DownloadManager mDownloadM;

    public EditActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUrl = (EditText)getView().findViewById(R.id.url);
        mFileName = (EditText)getView().findViewById(R.id.name);
        mDownload = (Button)getView().findViewById(R.id.download);
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryDownload();
            }
        });
    }

    private void tryDownload() {
        if (TextUtils.isEmpty(mUrl.getText()) || TextUtils.isEmpty(mFileName.getText())) {
            return;
        }

        DownloadManagerLite dl = DownloadManagerLite.getInstance();
        DownloadManagerLite.Request r = new DownloadManagerLite.Request();
        r.url = mUrl.getText().toString();
        dl.enqueue(r);

//        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(mUrl.getText().toString()));
//        mDownloadM.enqueue(r);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        String url = "";
        String fileName = "";

        Uri data = intent.getData();
        String action = intent.getAction();
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String extraReferenceUrl = intent.getStringExtra(EXTRA_X_REFERENCE_URL);
        Log.d(TAG, "data             :" + data);
        Log.d(TAG, "action           :" + action);
        Log.d(TAG, "extraText        :" + extraText);
        Log.d(TAG, "extraReferenceUrl:" + extraReferenceUrl);
        if (Intent.ACTION_SEND.equals(action)) {
            url = extraText;
            fileName = getFilename(url);
        }

        mUrl.setText(url);
        mFileName.setText(fileName);

        mDownloadM = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    static String getFilename(String path){
        String name = new File(path).getName();
        if (name.contains("?")) {
            name = name.substring(0, name.indexOf("?"));
        }
        return name;
    }


}
