package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class CloudListActivity extends Activity implements CloudExplorer.Listener, AdapterView.OnItemClickListener
{
    private CloudBackupApplication app = null;
    private CloudExplorer explorer = null;
    private CloudListAdapter adapter = null;
    private ArrayList<CloudExplorer.File> list = null;
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
        fileListView.setOnItemClickListener(this);
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
    public void explorerStartFailed() {

    }

    @Override
    public void listUpdated(ArrayList<CloudExplorer.File> list) {
        fileListView.setAdapter(new CloudListAdapter(this, this.list = list));
        fileListView.setOnItemClickListener(this);
    }

    @Override
    public void listUpdateFailed() {

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
                intent.putExtra("currentLevel", explorer.getCurrentLevel());
                intent.putExtra("parent", explorer.getCurrentParent());
                startActivity(intent);
            case R.id.action_add_folder:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CloudExplorer.File selected = (CloudExplorer.File)list.get(i);
        if(selected.IS_DIR)
                explorer.goToChild(i);
            else
            /* Download file code here */
                Log.d("Tom", selected.PHYSICAL_PATH);
    }

    @Override
    public void onBackPressed() {
        if(!explorer.backToParent())
            super.onBackPressed();
    }
}
