package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;


public class CloudListActivity extends Activity implements CloudExplorer.Listener{
    private CloudBackupApplication app = null;
    private CloudExplorer explorer = null;
    private CloudListAdapter adapter = null;
    private ListView fileListView;
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_list);
        //set app
        app = (CloudBackupApplication)this.getApplication();
        //set explorer
        explorer = CloudExplorer.instantiateFromSession(app.currentSession);
        explorer.addListener(this);
        //set bar
        ActionBar actionBar = getActionBar();
        actionBar.show();
        //set list view
        fileListView = (ListView) findViewById(R.id.cloud_list);
        //set progress dialog
        progressDialog = new ProgressDialog(CloudListActivity.this);
        progressDialog.setMessage("Retrieving list....");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        //set explorer
        explorer.start();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void explorerStarted() {
        progressDialog.dismiss();
    }

    @Override
    public void listUpdated(ArrayList<CloudExplorer.File> list) {
        fileListView.setAdapter(new CloudListAdapter(this, list));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_content, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_upload:
                Intent intent = new Intent(this, UploadActivity.class);
                startActivity(intent);

            case R.id.action_add_folder:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}