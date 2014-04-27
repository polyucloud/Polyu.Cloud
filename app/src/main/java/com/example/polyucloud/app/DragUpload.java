package com.example.polyucloud.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DragUpload extends Activity {

    private CloudBackupApplication app = null;

    private int currentLevel;
    private String parent;

    private ImageView myImage;
    private static final String IMAGEVIEW_TAG = "The Android Logo";

    private String file;
    private ArrayList<String> newFileList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_upload);

        myImage = (ImageView)findViewById(R.id.file_icon);
        // Sets the tag
        myImage.setTag(IMAGEVIEW_TAG);

        // set the listener to the dragging data
        myImage.setOnLongClickListener(new MyClickListener());

        Intent intent = getIntent();
        file = intent.getStringExtra("path");
        currentLevel = intent.getIntExtra("currentLevel", 0);
        parent = intent.getStringExtra("parent");

        app = (CloudBackupApplication)this.getApplication();

        findViewById(R.id.toplinear).setOnDragListener(new MyDragListener());
        findViewById(R.id.bottomlinear).setOnDragListener(new MyDragListener());

    }

    private final class MyClickListener implements View.OnLongClickListener {

        // called when the item is long-clicked
        @Override
        public boolean onLongClick(View view) {
            // TODO Auto-generated method stub

            // create it from the object's tag
            ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());

            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            view.startDrag( data, //data to be dragged
                    shadowBuilder, //drag shadow
                    view, //local data about the drag and drop operation
                    0   //no needed flags
            );


            view.setVisibility(View.INVISIBLE);
            return true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drag_upload, menu);
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

    class MyDragListener implements View.OnDragListener {
        //Drawable normalShape = getResources().getDrawable(R.drawable.normal_shape);
        //Drawable targetShape = getResources().getDrawable(R.drawable.target_shape);

        @Override
        public boolean onDrag(View v, DragEvent event) {

            // Handles each of the expected events
            switch (event.getAction()) {

                //signal for the start of a drag and drop operation.
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;

                //the drag point has entered the bounding box of the View
                case DragEvent.ACTION_DRAG_ENTERED:
                    //v.setBackground(targetShape);	//change the shape of the view
                    break;

                //the user has moved the drag shadow outside the bounding box of the View
                case DragEvent.ACTION_DRAG_EXITED:
                    //v.setBackground(normalShape);	//change the shape of the view back to normal
                    break;

                //drag shadow has been released,the drag point is within the bounding box of the View
                case DragEvent.ACTION_DROP:
                    // if the view is the bottomlinear, we accept the drag item
                    if (v == findViewById(R.id.toplinear)) {
                        View view = (View) event.getLocalState();
                        ViewGroup viewgroup = (ViewGroup) view.getParent();
                        viewgroup.removeView(view);

                        //change the text
                        TextView text = (TextView) v.findViewById(R.id.text);
                        text.setText("The item is dropped");

                        File f = new File(file);

                        FileUploadTask fileUploadTask = new FileUploadTask(f);
                        fileUploadTask.execute();

                        LinearLayout containView = (LinearLayout) v;
                        containView.addView(view);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        View view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                        Context context = getApplicationContext();
                        Toast.makeText(context, "You can't drop the image here",
                                Toast.LENGTH_LONG).show();
                        break;
                    }
                    break;

                //the drag and drop operation has concluded.
                case DragEvent.ACTION_DRAG_ENDED:
                    //ßv.setBackground(normalShape);	//go back to normal shape

                default:
                    break;
            }
            return true;
        }


        class FileUploadTask extends AsyncTask<Object, Integer, String> {

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
                progressDialog = new ProgressDialog(DragUpload.this);
                progressDialog.setMessage("Uploading");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(false); //Disable Indeterminate effect
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(0);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Object... objects) {

                //upFile = new File(uploadFile);

                long length = 0;
                int progress;
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 256 * 1024; // 256KB
                long totalSize = uploadFile.length();

                try {
                    Log.i("UID", app.currentSession.UID + " ");
                    uploadPhpPage += "?uid=" + app.currentSession.UID;
                    uploadPhpPage += "&parent=" + parent;
                    uploadPhpPage += "&level=" + currentLevel;
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
                    outputStream.writeBytes("Content-Disposition: from-data; name=\"uploadedfile\";filename=\"" + uploadFile + "\"" + lineEnd);
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

                    Log.i("Server Response Code:", "" + serverResponseCode);
                    Log.i("Server Response Msg:", serverResponseMessage);

                    fileInputStream.close();
                    outputStream.flush();
                    outputStream.close();
                    return result;

                } catch (Exception ex) {
                    return null;
                    //Log.e("Upload file error: ", ex.getMessage());
                    //Toast.makeText(UploadActivity.this, "Upload error. Please check network connection and try again.", Toast.LENGTH_LONG).show();
                }
                //return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String jsonString) {
                progressDialog.dismiss();
                if (jsonString == null) {
                    Toast.makeText(DragUpload.this, "Upload error. Please check network connection and try again.", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Log.d("Tom", jsonString);
                    JSONObject root = new JSONObject(jsonString);
                    if (root.getInt("response") == 1) {
                        String name = uploadFile.getName();
                        String storage_path = root.getString("storage_path");
                        String type = "f";
                        newFileList.add(name);
                        newFileList.add(storage_path);
                        newFileList.add(type);
                    } else
                        showErrorDialog("Error", "Response error: " + root.getInt("response"));
                } catch (JSONException e) {
                    showErrorDialog("Error", "Response format error.");
                }

            }

            private void showErrorDialog(String title, String message) {
                new AlertDialog.Builder(DragUpload.this)
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
    }

    }
