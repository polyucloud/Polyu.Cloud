package com.example.polyucloud.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
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
public class LoginActivity extends Activity{
    private CloudBackupApplication app = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (CloudBackupApplication)this.getApplication();
    }

    public void register(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }

    public void login(View v) {
        String email = ((EditText)findViewById(R.id.txtLogEmail)).getText().toString();
        String password = ((EditText)findViewById(R.id.txtLogPassword)).getText().toString();
        HashMap<String, String> map = new HashMap<String,String>();
        map.put("email" , email);
        map.put("password" , password);
        new LoginTask().execute(map);
    }

    class LoginTask extends AsyncTask<HashMap<String, String>, Void, String> {
        private ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Logging in....");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(HashMap<String, String>... maps) {
            HashMap<String, String> data = maps[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(app.PHP_ROOT_URL+"login.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
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
        protected void onProgressUpdate(Void... voids) {}

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
                        if(root.getInt("count")<=0)
                            showErrorDialog("Error", "Email or password is wrong.");
                        else
                        {
                            JSONObject userObj = root.getJSONArray("result").getJSONObject(0);
                            app.currentSession = new CloudBackupApplication.Session(
                                    userObj.getInt("id"),
                                    userObj.getString("email"),
                                    userObj.getString("first_name"),
                                    userObj.getString("last_name"));
                            progressDialog.hide();
                            Intent intent = new Intent(LoginActivity.this, CloudListActivity.class);
                            startActivity(intent);
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
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle(title)
                    .setMessage(message)
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
