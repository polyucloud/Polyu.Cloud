package com.example.polyucloud.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CloudListActivity extends Activity implements CloudExplorer.Listener, AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener
{
    private CloudBackupApplication app = null;
    private CloudExplorer explorer = null;
    private CloudListAdapter adapter = null;
    private ArrayList<CloudExplorer.File> list = null;
    private ListView fileListView;
    private ProgressDialog progressDialog = null;

    private File mainfolder=null;
    private String downloadurl=null;
    public ProgressDialog progressBar=null;
    private int deleteType;

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

        progressBar = new ProgressDialog(CloudListActivity.this);
        progressBar.setMessage("Downloading");
        //progressBar.setMax(100);
        progressBar.setCancelable(false);
        progressBar.setIndeterminate(false); //Disable Indeterminate effect
        progressBar.setProgressStyle(progressBar.STYLE_HORIZONTAL);
        progressBar.hide();


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
        fileListView.setOnItemLongClickListener(this);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("Tom", "del ready");
        CloudExplorer.File selected = (CloudExplorer.File)list.get(i);
        if(selected.IS_DIR){
            //del all file in folder
            String deletefoldername=selected.NAME;
            Log.d("Tom",deletefoldername+" "+app.currentSession.UID);
            HashMap<String, String> map = new HashMap<String,String>();
            map.put("UID",app.currentSession.UID+"");
            map.put("delfilename" , deletefoldername);
            deleteType=0;
            confirmDelete(map);
        }
        else
        {

            String deletefilename=selected.NAME;
            Log.d("Tom",deletefilename.substring(0,deletefilename.lastIndexOf("."))+" "+app.currentSession.UID);
            HashMap<String, String> map = new HashMap<String,String>();
            map.put("UID",app.currentSession.UID+"");
            map.put("delfilename" , deletefilename.substring(0,deletefilename.lastIndexOf(".")));
            deleteType=1;
            confirmDelete(map);
        }
        return true;
    }

    private void confirmDelete( HashMap<String, String> deletedata) {
        final HashMap<String, String> deleteFile = deletedata;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CloudListActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Confirm download...");

        // Setting Dialog Message
        if(deleteType==1){
            alertDialog.setMessage("Do you want to download this file?");
        }
        else{
            alertDialog.setMessage("Do you want to download this folder?\nEvery files inside will be also deleted");
        }

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Her
                new DeleteFileTask().execute(deleteFile);

            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here
                //Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    class DeleteFileTask extends AsyncTask<HashMap<String, String>, Void, String> {
        private ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute()
        {
            progressDialog = new ProgressDialog(CloudListActivity.this);
            progressDialog.setMessage("Deleting File....");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(HashMap<String, String>... maps) {
            HashMap<String, String> data = maps[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost;
            if(deleteType==1){
                httppost = new HttpPost(app.PHP_ROOT_URL+"deleteFile.php");
            }
            else{
                httppost = new HttpPost(app.PHP_ROOT_URL+"deleteFolder.php");
            }
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                Log.d("Tom",data.get("UID"));
                Log.d("Tom",data.get("delfilename"));
                nameValuePairs.add(new BasicNameValuePair("UID", data.get("UID")));
                nameValuePairs.add(new BasicNameValuePair("delfilename", data.get("delfilename")));
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

                        new AlertDialog.Builder(CloudListActivity.this)
                                .setTitle("Deleted")
                                .setMessage("Fail delete success")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        progressDialog.dismiss();

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();

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
            new AlertDialog.Builder(CloudListActivity.this)
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


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CloudExplorer.File selected = (CloudExplorer.File)list.get(i);
        if(selected.IS_DIR)
            explorer.goToChild(i);
        else
        {
            Log.d("Tom", app.FILE_ROOT_URL+selected.PHYSICAL_PATH);
            mainfolder = new File(Environment.getExternalStorageDirectory()+"/polyucloud");
            if(!mainfolder.isDirectory()){
                Log.d("Tom","open main directory");
                mainfolder.mkdir();
            }
            Log.d("Tom","see mian"+ mainfolder);
            //DownloadActivit dlTask=new DownloadActivit(mainfolder,app.FILE_ROOT_URL+selected.PHYSICAL_PATH,CloudListActivity.this);
            downloadurl=app.FILE_ROOT_URL+selected.PHYSICAL_PATH;
            comfirmDownload();
        }
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
                ArrayList<String> siblings = new ArrayList<String>();
                for(int i=0;i<list.size();i++)
                    siblings.add(list.get(i).NAME);
                intent.putExtra("siblings", siblings);//
                startActivityForResult(intent, 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1 && data.hasExtra("newFileList"))
        {
            ArrayList<String> newFileList = data.getStringArrayListExtra("newFileList");
            for(int i=0;i<newFileList.size();i+=3)
                explorer.addChildOffline(newFileList.get(i), false ,newFileList.get(i+1));
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
    public void onBackPressed() {
        if(!explorer.backToParent())
            super.onBackPressed();
    }

    private void comfirmDownload() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CloudListActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Confirm download...");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to download this file?");

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here
                FileDownloadTask dl=new FileDownloadTask(downloadurl);
                dl.execute();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here
                //Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    class FileDownloadTask extends AsyncTask<String,Integer,Void> {

        File storagedir=new File(Environment.getExternalStorageDirectory()+"/polyucloud");

        String fileURL;
        FileDownloadTask( String fileURL){
            this.fileURL=fileURL;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            Log.d("Tom", "start to download");
            progressBar.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            int fileSize,currentDataSize;
            Log.d("Tom","getting file on "+fileURL);
            try{
                Log.d("Tom","main is "+storagedir);
                URL fileurl=new URL(fileURL);
                HttpURLConnection connection=(HttpURLConnection)fileurl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                fileSize=connection.getContentLength();
                currentDataSize=0;
                String fileName=fileURL.substring(fileURL.lastIndexOf("/"));


                File checkfile=new File(storagedir+"/"+fileName);
                File orginal=new File(storagedir+"/"+fileName);
                int i=0;
                do{
                    if(i!=0){
                        String temp=orginal.toString().substring(0,orginal.toString().lastIndexOf(".")-1)+"("+i+")"+orginal.toString().substring(orginal.toString().lastIndexOf("."));
                        checkfile=new File(temp);
                    }

                    i++;
                    Log.d("Tom","check file="+checkfile.toString());
                }while(checkfile.exists());



                FileOutputStream outputStream=new FileOutputStream(checkfile);
                Log.d("Tom","main is "+storagedir);
                InputStream inputStream=connection.getInputStream();

                byte[] buffer=new byte[1024];
                int len1 = 0;
                long total = 0;

                while((len1=inputStream.read(buffer))>0){
                    total+=len1;
                    publishProgress(((int)((total*100)/fileSize)));
                    outputStream.write(buffer, 0, len1);
                }
                outputStream.flush();
                outputStream.close();
                connection.disconnect();

            }catch(Exception ex){
                Log.d("Tom", "exception "+ ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);

            //Log.d("Tom", values[0]+"");
        }

        @Override
        protected void onPostExecute(Void v) {
            progressBar.dismiss();
            Log.d("Tom", " Download finished");
        }
    }


}
