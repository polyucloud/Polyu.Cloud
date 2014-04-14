package com.example.polyucloud.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;


public class UploadFile extends Activity implements View.OnClickListener {

    private String uploadFile = Environment.getExternalStorageDirectory().toString()+"/Download/test.txt";
    Button testUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        init();
        Log.i("File want to upload:", uploadFile);
    }

    private void init() {
        testUp = (Button) findViewById(R.id.test_upload);
        testUp.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload_file, menu);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_upload:

                /** Call AsyncTask to upload file at background **/
                FileUploadTask fileUploadTask = new FileUploadTask(uploadFile);
                fileUploadTask.execute();
                //test_ListFile();
                break;
        }
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

    /** AsyncTask to upload file to server **/
    class FileUploadTask extends AsyncTask<Object, Integer, Void> {

        private ProgressDialog progressDialog = null;
        private String uploadFile;
        private File upFile;
        private String uploadPhpPage = "http://daisunhong.com/polyucloud/php/upload_file.php";
        private String boundary = "*****";
        private String lineEnd = "\r\n";
        private String twoHyphens = "--";

        private DataOutputStream outputStream = null;
        //private DataInputStream inputStream = null;

        HttpURLConnection connection = null;

        FileUploadTask(String uploadFile) {
            this.uploadFile = uploadFile;
        }

        @Override
        protected void onPreExecute() {
            Log.i("Infor:", "start background task to upload file");
            progressDialog = new ProgressDialog(UploadFile.this);
            progressDialog.setMessage("Uploading");
            progressDialog.setIndeterminate(false); //Disable Indeterminate effect
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Object... objects) {

            upFile = new File(uploadFile);

            long length = 0;
            int progress;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 256 * 1024; // 256KB
            long totalSize = upFile.length();

            try {
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

                FileInputStream fileInputStream = new FileInputStream(upFile);

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

