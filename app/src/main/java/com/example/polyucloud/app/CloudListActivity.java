package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class CloudListActivity extends Activity {
    private CloudBackupApplication app = null;
    private CloudExplorer explorer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_list);
        app = (CloudBackupApplication)this.getApplication();
        explorer = CloudExplorer.instantiateFromSession(app.currentSession);
        initActionBar();
    }

    protected void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.show();
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
