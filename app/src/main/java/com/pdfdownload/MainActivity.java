package com.pdfdownload;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn_download;
    String finalURL = "http://abc/ddf.pdf";
    String pdfNameDisplay;
    String tempString;
    Dialog dialog;
    ProgressBar progressBar;
    TextView cur_val;
    File sdCardRoot;
    int downloadedSize = 0;
    int totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_download = (Button) findViewById(R.id.btn_download);

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = "abc.pdf";

                if (isFileExist(title)) {
                    try {
                        File file = new File(tempString);
                        PackageManager packageManager = getPackageManager();
                        Intent testIntent = new Intent(Intent.ACTION_VIEW);
                        testIntent.setType("application/pdf");
                        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri uri = Uri.fromFile(file);
                        intent.setDataAndType(uri, "application/pdf");
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        showProgress(finalURL);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadFile(title);
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean isFileExist(String fileName) {
        try {
            sdCardRoot = new File(Environment.getExternalStorageDirectory(), "College");
            /**
             * Create the storage directory(MiiSurpriseVideos) if it does not exist
             **/
            if (!sdCardRoot.exists()) {
                sdCardRoot.mkdir();
            }

            pdfNameDisplay = finalURL.toString();
            /** Get the File name to Store in director MiiVDODownloads **/
            pdfNameDisplay = pdfNameDisplay.substring(
                    pdfNameDisplay.lastIndexOf("/") + 1,
                    pdfNameDisplay.lastIndexOf("."));

            /** Check the File name to Store in director MiiVDODownloads **/
            tempString = fileName + "_" + pdfNameDisplay + ".pdf";
            /** get all the files present in director MiiVDODownloads **/
            File myFiles[] = sdCardRoot.listFiles();

            for (int i = 0; i < myFiles.length; i++) {
                File newTempFILe = new File(myFiles[i].getName());
                if (tempString.equals(newTempFILe.toString())) {
                    // file already downloaded
                    tempString = myFiles[i].getAbsolutePath();
                    Log.i("File found:", myFiles[i].toString());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    void showProgress(String file_path) {
        try {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.my_progress_dialog);
            // dialog.setTitle("Downloading file....");
            dialog.getWindow().setLayout(ActionBarOverlayLayout.LayoutParams.MATCH_PARENT,
                    ActionBarOverlayLayout.LayoutParams.WRAP_CONTENT);

            TextView text = (TextView) dialog.findViewById(R.id.tv1);
            text.setText("Downloading file....");
            dialog.setCancelable(false);

            cur_val = (TextView) dialog.findViewById(R.id.cur_pg_tv);
            // cur_val.setText("Starting download...");
            dialog.show();

            progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
            progressBar.setProgress(0);
            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    void downloadFile(String eventName) {
        try {
            URL url = new URL(finalURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            File downloadedFile = new File(sdCardRoot.getPath()
                    + File.separator + eventName + "_" + pdfNameDisplay
                    + ".pdf");

            FileOutputStream outPutStream = new FileOutputStream(downloadedFile);

            InputStream inputStream = urlConnection.getInputStream();
            totalSize = urlConnection.getContentLength();

            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.setMax(totalSize);
                }
            });

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                outPutStream.write(buffer, 0, bufferLength);
                downloadedSize = downloadedSize + bufferLength;

                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setProgress(downloadedSize);
                        float per = ((float) downloadedSize / totalSize) * 100;
                        cur_val.setText("(" + (int) per + "%)");
                    }
                });
            }

            if (totalSize == downloadedSize) {
                dialog.dismiss();
                try {
                    File file = new File(downloadedFile.getAbsolutePath());
                    PackageManager packageManager = getPackageManager();
                    Intent testIntent = new Intent(Intent.ACTION_VIEW);
                    testIntent.setType("application/pdf");
                    List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = Uri.fromFile(file);
                    intent.setDataAndType(uri, "application/pdf");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                downloadedSize = 0;
            }
            outPutStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
