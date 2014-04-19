package com.example.polyucloud.app;

import android.os.AsyncTask;
import android.util.Log;

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
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Tom on 4/17/14.
 */
public class CloudExplorer {

    private static HashMap<Integer, CloudExplorer> explorers = null;

    private int UID;
    private int currentLevel;
    private String parent;
    private HashSet<Listener> listeners;

    private CloudExplorer(int id)
    {
        currentLevel = -1;
        UID = id;
        listeners = new HashSet<Listener>();
    }

    public static CloudExplorer instantiateFromSession(CloudBackupApplication.Session session)
    {
        if(explorers == null)
            explorers = new HashMap<Integer, CloudExplorer>();
        if(explorers.get(session.UID) == null);
            explorers.put(session.UID, new CloudExplorer(session.UID));
        return explorers.get(session.UID);
    }

    public boolean addListener(Listener l) { return listeners.add(l); }
    public boolean removeListener(Listener l) { return listeners.remove(l); }

    public void start()
    {
        if(currentLevel >= 0) return;
        currentLevel = 0;
        parent = "root";
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected void onPreExecute() {}

            @Override
            protected String doInBackground(Integer... values) {
                HttpPost httppost = new HttpPost(CloudBackupApplication.PHP_ROOT_URL+"file_structure.php");
                HttpClient httpclient = new DefaultHttpClient();
                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                    nameValuePairs.add(new BasicNameValuePair("id", ""+values[0]));
                    nameValuePairs.add(new BasicNameValuePair("level", "0"));
                    nameValuePairs.add(new BasicNameValuePair("depth", "0"));
                    nameValuePairs.add(new BasicNameValuePair("parent_folder", "root"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
            catch (ClientProtocolException e) { Log.d("Tom",e.toString()); return null; }
            catch (IOException e) { Log.d("Tom",e.toString()); return null; }
        }

        @Override
        protected void onProgressUpdate(Void... voids) {}

        @Override
        protected void onPostExecute(String jsonString) {
            for(Listener l:listeners)
                l.explorerStarted();
            CloudExplorer.this.notifyListChange(jsonString);
            }
        }.execute(UID);
    }

    public void backToParent()
    {

    }

    public void goToChild(String name)
    {

    }

    private void notifyListChange(String json)
    {
        try
        {
            JSONObject returnObj = new JSONObject(json);
            if(returnObj.getInt("response") != 1) return;
            JSONObject rootObj =  (JSONObject) returnObj.getJSONArray("result").get(0);
            JSONArray childs = rootObj.getJSONArray("child");
            ArrayList<File> files = new ArrayList<File>();
            for(int i=0;i<childs.length();i++)
            {
                String name = ((JSONObject) childs.get(i)).getString("name");
                String path = "abc";
                boolean is_dir = ((JSONObject) childs.get(i)).getString("type").equals("d");
                File f = new File(name,path,is_dir);
                files.add(f);
            }
            for(Listener l:listeners)
                l.listUpdated(files);
        }
        catch (JSONException e){}
    }

    public static interface Listener
    {
        public void explorerStarted();
        public void listUpdated(ArrayList<File> list);
    }

    public static class File
    {
        public final String NAME, PHYSICAL_PATH;
        public final boolean IS_DIR;
        public File(String name, String path, boolean is_dir)
        {
            NAME = name;
            PHYSICAL_PATH = path;
            IS_DIR = is_dir;
        }
    }
}
