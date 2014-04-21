package com.example.polyucloud.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class ListDownloadedFileActivity extends Activity implements AdapterView.OnItemClickListener {

    private ArrayList<HashMap> fileList = null;
    private ListView fileListView;
    private DownloadedFileListAdapter listAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_downloaded_file);
        init();
    }

    private void init() {
        fileList = new ArrayList<HashMap>();
        fileListView = (ListView) findViewById(R.id.downloaded_file_list);
        fileListView.setOnItemClickListener(this);

        listAdapter = new DownloadedFileListAdapter(this, fileList);
        

        File downloadFolder = new File(Environment.getExternalStorageDirectory()+"/polyucloud");
        obtainSDfiles(downloadFolder);
        fileListView.setAdapter(listAdapter);
    }

    private void obtainSDfiles(File downloadFolder) {
        File dirs = new File(downloadFolder.getAbsolutePath());

        Log.i("SD Card path:", dirs.toString());

        if(dirs.exists()) {
            File[] files = dirs.listFiles();

            if(files!=null)
                for (File f:files) {
                    HashMap item = new HashMap();

                    item.put("f_name", f.getName());
                    item.put("f_path", f);

                    fileList.add(item);
                    Log.i("File:", f.toString());
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_downloaded_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i("Eric", fileList.get(i).get("f_path").toString());
        Intent newIntent = null;
        try {
            File file = (File) fileList.get(i).get("f_path");

            MimeTypeMap myMime = MimeTypeMap.getSingleton();

            newIntent = new Intent(android.content.Intent.ACTION_VIEW);

            String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.toString()).substring(1));
            newIntent.setDataAndType(Uri.fromFile(file), mimeType);
            newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Sorry we cannot open this file.", 4000).show();
        }
        /**try {
            //startActivity(newIntent);
        } catch (android.content.ActivityNotFoundException e) {
            //Toast.makeText(this, "Sorry we cannot open this file.", 4000).show();
        }**/
    }

    private String fileExt(String url) {
        if (url.indexOf("?")>-1) {
            url = url.substring(0,url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") );
            if (ext.indexOf("%")>-1) {
                ext = ext.substring(0,ext.indexOf("%"));
            }
            if (ext.indexOf("/")>-1) {
                ext = ext.substring(0,ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
}
