package com.example.polyucloud.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }


    class RegisterTask extends AsyncTask<HashMap<String, String>, Void, Integer> {
        private ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Registering");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(HashMap<String, String>... maps) {
            HashMap<String, String> data = maps[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://daisunhong.com/polyucloud/php/register.php");
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
                String responseString = EntityUtils.toString(entity, "UTF-8");
                int responseInt = Integer.parseInt(responseString);
                return responseInt;

            }
            catch (ClientProtocolException e) { return -4; }
            catch (IOException e) { return -4; }
            catch (Exception e) { return -4; }
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result>0)
            {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Registered")
                        .setMessage("You are successfully registered!")
                        .setCancelable(false)
                        .setPositiveButton("Return to Login", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                progressDialog.hide();
                                RegisterActivity.this.finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
            else if(result == -1)
            {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Email in use")
                        .setMessage("The email is already in use. Please use another email.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                progressDialog.hide();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else
            {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Error occur")
                        .setMessage("Error code: " + result + ". Please contact admin.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                progressDialog.hide();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }
    }

    public void register(View view)
    {
        String fstName = ((EditText)findViewById(R.id.txtRegFstName)).getText().toString();
        String lstName = ((EditText)findViewById(R.id.txtRegLstName)).getText().toString();
        String email = ((EditText)findViewById(R.id.txtRegEmail)).getText().toString();
        String password = ((EditText)findViewById(R.id.txtRegPassword)).getText().toString();
        if(fstName.length()<=0 || fstName.length()<= 0|| fstName.length()<= 0 || fstName.length()<=0){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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

}
