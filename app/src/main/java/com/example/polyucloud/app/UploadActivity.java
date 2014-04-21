package com.example.polyucloud.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


public class UploadActivity extends Activity implements AdapterView.OnItemClickListener {

    private CloudBackupApplication app = null;
    private ArrayList<HashMap> fileList = null;
    private ListView fileListView;
    public File uploadFile;
    Button testUp;
    private int currentLevel;
    private String parent;

    private FileListAdapter fileListAdapter = null;

    private Stack<File> pastFolder = new Stack<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (CloudBackupApplication)this.getApplication();
        setContentView(R.layout.activity_upload_file);
        init();
        currentLevel = getIntent().getExtras().getInt("currentLevel");
        parent = getIntent().getExtras().getString("parent");
        //Log.i("File want to upload:", uploadFile);
    }

    private void init() {
        fileList = new ArrayList<HashMap>();
        fileListView = (ListView) findViewById(R.id.sd_file_list);
        fileListView.setOnItemClickListener(this);

        File sdcard = Environment.getExternalStorageDirectory();
        obtainSDfiles(sdcard);
        fileListAdapter = new FileListAdapter(this, fileList);
        fileListView.setAdapter(fileListAdapter);
    }

    private void obtainSDfiles(File target) {

        File dirs = new File(target.getAbsolutePath());

        Log.i("SD Card path:", dirs.toString());

        if(dirs.exists()) {
            File[] files = dirs.listFiles();

            if(files!=null)
            for (File f:files) {
                HashMap item = new HashMap();

                item.put("f_name", f.getName());
                item.put("f_path", f);
                item.put("f_parent", dirs);

                if(f.isDirectory()) {
                    item.put("f_type", "DIR");
                } else {
                    item.put("f_type", "FILE");
                }
                fileList.add(item);
                Log.i("File:", f.toString());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload_file, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(pastFolder.size() > 0) {
            File f = pastFolder.pop();
            fileList.clear();
            obtainSDfiles( f );
            fileListAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
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


    private void test_ListFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        File dirs = new File(sdcard.getAbsolutePath());

        Log.i("SD Card path:", dirs.toString());

        if(dirs.exists()) {
            String[] files = dirs.list();

            for (String fileName:files) {
                Log.i("File:", fileName);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File f = (File) fileList.get(i).get("f_path");

        String f_type = (String) fileList.get(i).get("f_type");
        if(f_type.equalsIgnoreCase("DIR")) {
            //File f = (File) fileList.get(i).get("f_path");

            pastFolder.push(f.getParentFile());

            fileList.clear();
            obtainSDfiles( f );
            fileListAdapter.notifyDataSetChanged();
        } else {
            uploadFile = f;
            comfirmUpload();
        }
    }

    private void comfirmUpload() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UploadActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Confirm upload...");

        // Setting Dialog Message
        alertDialog.setMessage("Do you want to upload this file?");

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here
                FileUploadTask fileUploadTask = new FileUploadTask(uploadFile);
                fileUploadTask.execute();
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

    /** AsyncTask to upload file to server **/
    class FileUploadTask extends AsyncTask<Object, Integer, Void> {

        private ProgressDialog progressDialog = null;
        private File uploadFile;
        private String uploadPhpPage = "http://daisunhong.com/polyucloud/php/upload_file.php";
        private String boundary = "*****";
        private String lineEnd = "\r\n";
        private String twoHyphens = "--";

        private DataOutputStream outputStream = null;
        //private DataInputStream inputStream = null;

        HttpURLConnection connection = null;

        FileUploadTask(File uploadFile) {
            this.uploadFile = uploadFile;
        }

        @Override
        protected void onPreExecute() {
            Log.i("Infor:", "start background task to upload file");
            progressDialog = new ProgressDialog(UploadActivity.this);
            progressDialog.setMessage("Uploading");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false); //Disable Indeterminate effect
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... objects) {

            //upFile = new File(uploadFile);
            //ttt

            long length = 0;
            int progress;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 256 * 1024; // 256KB
            long totalSize = uploadFile.length();

            try {
                Log.i("UID", app.currentSession.UID + " ");
                uploadPhpPage += "?uid="+app.currentSession.UID;
                uploadPhpPage += "&parent="+parent;
                uploadPhpPage += "&level="+currentLevel;
                URL url = new URL(uploadPhpPage);
                connection = (HttpURLConnection) url.openConnection();

                // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
                // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
                connection.setChunkedStreamingMode(maxBufferSize);

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);  //boundary used to split multipart data

                FileInputStream fileInputStream = new FileInputStream(uploadFile);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: from-data; name=\"uploadedfile\";filename=\""+uploadFile+"\""+lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    length += bufferSize;
                    progress = (int) ((length * 100) / totalSize);
                    publishProgress(progress);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                publishProgress(100);

                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                InputStream stream = connection.getInputStream();
                InputStreamReader isReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(isReader);
                String result = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                Log.i("Server c:", result);

                Log.i("Server Response Code:", ""+serverResponseCode);
                Log.i("Server Response Msg:", serverResponseMessage);

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

            } catch (Exception ex) {
                Log.e("Upload file error: ", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("Upload file error: ", e.getMessage());
            }
        }
    }

}

