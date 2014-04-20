package com.example.polyucloud.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tom on 4/14/14.
 */
public class RegisterActivity extends Activity {
    private CloudBackupApplication app = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        app = (CloudBackupApplication)this.getApplication();
    }

    class RegisterTask extends AsyncTask<HashMap<String, String>, Void, String> {
        private ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Registering....");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(HashMap<String, String>... maps) {
            HashMap<String, String> data = maps[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(app.PHP_ROOT_URL+"register.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("fstName", data.get("fstName")));
                nameValuePairs.add(new BasicNameValuePair("lstName", data.get("lstName")));
                nameValuePairs.add(new BasicNameValuePair("email", data.get("email")));
                nameValuePairs.add(new BasicNameValuePair("password", data.get("password")));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
            catch (ClientProtocolException e) { return null; }
            catch (IOException e) { return null; }
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
        }

        @Override
        protected void onPostExecute(String jsonString) {
            Log.d("Tom", jsonString);
            if(jsonString == null)
                showErrorDialog("Error", "Connection error.");
            else
            {
                try
                {
                    JSONObject root = new JSONObject(jsonString);
                    if(root.getInt("response")==1)
                    {
                        if(root.getInt("affected_row")<=0)
                            showErrorDialog("Email in use", "The email has been already used. Please use another email.");
                        else
                        {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle("Registered")
                                    .setMessage("You are successfully registered!")
                                    .setCancelable(false)
                                    .setPositiveButton("Return to Login", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            progressDialog.dismiss();
                                            RegisterActivity.this.finish();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .show();
                        }
                    }
                    else
                        showErrorDialog("Error","Response error: "+root.getInt("response"));
                }
                catch (JSONException e)
                {
                    showErrorDialog("Error","Response format error.");
                }
            }
        }

        private void showErrorDialog(String title, String message)
        {
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle(title)
                    .setMessage(message)
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
    }

    public void register(View view){
        String fstName = ((EditText)findViewById(R.id.txtRegFstName)).getText().toString();
        String lstName = ((EditText)findViewById(R.id.txtRegLstName)).getText().toString();
        String email = ((EditText)findViewById(R.id.txtRegEmail)).getText().toString();
        String password = ((EditText)findViewById(R.id.txtRegPassword)).getText().toString();
        if(fstName.length()<=0 || lstName.length()<= 0|| email.length()<= 0 || password.length()<=0){
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Error")
                    .setMessage("Not null is not allowed.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else if(!new EmailValidator().validate(email)) {
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Email Invalid")
                    .setMessage("You email is invalid. Please check.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
        {
            HashMap<String, String> map = new HashMap<String,String>();
            map.put("fstName" , fstName);
            map.put("lstName" , lstName);
            map.put("email" , email);
            map.put("password" , password);
            new RegisterTask().execute(map);
        }
    }
}
