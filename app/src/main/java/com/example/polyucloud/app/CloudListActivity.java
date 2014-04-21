package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
    public void childAdded(String childName, boolean isDir) {
        progressDialog.dismiss();
    }

    @Override
    public void childAddFailed(String childName, boolean isDir) {
        new AlertDialog.Builder(CloudListActivity.this)
                .setTitle("Error")
                .setMessage((isDir?"Folder ":"File ")+childName+" cannot be added.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        progressDialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
                return true;
            case R.id.action_add_folder:
                addDirectory();
                return true;
            case R.id.action_access_download:
                //List the downloaded file
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addDirectory()
    {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_directory_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.add_directory_name);
        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // get user input and set it to result
                            // edit text
                            progressDialog = new ProgressDialog(CloudListActivity.this);
                            progressDialog.setMessage("Adding....");
                            progressDialog.setIndeterminate(false);
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            explorer.addChild(userInput.getText().toString(), true);
                        }
                    })
            .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CloudExplorer.File selected = (CloudExplorer.File)list.get(i);
        if(selected.IS_DIR)
                explorer.goToChild(i);
            else
            /* Download file code here */
                Log.d("Tom", app.FILE_ROOT_URL+selected.PHYSICAL_PATH);
    }

    @Override
    public void onBackPressed() {
        if(!explorer.backToParent())
            super.onBackPressed();
    }

    private boolean firstTimeResume = true;

    @Override
    protected void onResume() {
        if (!firstTimeResume) {
            Log.i("Eric", "I'm resume....");
            progressDialog = new ProgressDialog(CloudListActivity.this);
            progressDialog.setMessage("Retrieving list....");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            explorer.update();
        }
        firstTimeResume = false;
        super.onResume();
    }
}
