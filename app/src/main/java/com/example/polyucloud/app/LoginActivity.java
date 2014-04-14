package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
/**
 * Created by Tom on 4/14/14.
 */
public class LoginActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void onClick(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }

}
