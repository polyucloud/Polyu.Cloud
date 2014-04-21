package com.example.polyucloud.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Chan on 14年4月21日.
 */
public class DownloadActivit {

    File storagedir;
    private ProgressDialog progressBar=null;
    CloudListActivity cloudListActivity;
    DownloadActivit(File storagedir, String fileURL, CloudListActivity cloudListActivity){
        Log.d("Tom","getting file on "+fileURL);
        this.storagedir=storagedir;
        this.cloudListActivity=cloudListActivity;
        FileDownloadTask download=new FileDownloadTask();
        download.execute(fileURL);
    }

     class FileDownloadTask extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            Log.d("Tom", "start to download");

            progressBar = new ProgressDialog(cloudListActivity.getApplicationContext());
            progressBar.setMessage("Downloading");
            progressBar.setCancelable(false);
            progressBar.setIndeterminate(false); //Disable Indeterminate effect
            progressBar.setProgressStyle(progressBar.STYLE_HORIZONTAL);
            progressBar.setProgress(0);

            if(progressBar==null){
                Log.d("Tom","null exception");
            }
            else{

                Log.d("Tom",progressBar.toString());
            }
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            int fileSize,currentDataSize;
            Log.d("Tom","getting file on "+strings[0]);
            try{
                URL fileurl=new URL(strings[0]);
                HttpURLConnection connection=(HttpURLConnection)fileurl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                fileSize=connection.getContentLength();
                currentDataSize=0;
                String fileName=strings[0].substring(strings[0].lastIndexOf("/"));
                //check if file exist
                File checkfile=new File(storagedir+"/"+fileName);
                int i=0;
                do{
                    if(i==0){
                        checkfile=new File(storagedir+"/"+fileName);
                    }
                    else{
                        String temp=fileName.substring(0,checkfile.toString().lastIndexOf(".")-1)+"("+i+")"+fileName.substring(checkfile.toString().lastIndexOf("."));
                        checkfile=new File(temp);
                    }
                }while(checkfile.exists());
                Log.d("Tom","check file="+checkfile.toString());

                FileOutputStream outputStream=new FileOutputStream(checkfile);
                InputStream inputStream=connection.getInputStream();

                byte[] buffer=new byte[1024];
                int len1 = 0;
                long total = 0;

                while((len1=inputStream.read(buffer))>0){
                    total+=len1;
                    //Log.d("Tom",total+" "+len1);
                    publishProgress((int)((total*100)/fileSize)+"");
                }

            }catch(Exception ex){
                Log.d("Tom", "exception "+ ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            super.onProgressUpdate(values);
            //Log.d("Tom",values[0]);
        }

        @Override
         protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d("Tom",s+" finished");
        }
    }
}
