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
import java.util.Stack;

/**
 * Created by Tom on 4/17/14.
 */
public class CloudExplorer {

    private static HashMap<Integer, CloudExplorer> explorers = null;

    private int UID;
    private HashSet<Listener> listeners;
    private Stack<JSONArray> JSONArrayStack;
    private Stack<String> parentStack;

    private CloudExplorer(int id)
    {
        UID = id;
        listeners = new HashSet<Listener>();
        JSONArrayStack = new Stack<JSONArray>();
        parentStack = new Stack<String>();
    }

    public String getCurrentParent()
    {
        return parentStack.lastElement();
    }

    public int getCurrentLevel()
    {
        return parentStack.size()-1;
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
        if(JSONArrayStack.size() >0) return;
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
                Log.d("Tom", jsonString);
                try
                {
                    JSONObject returnObj = new JSONObject(jsonString);
                    if(returnObj.getInt("response") != 1)
                    {
                        for(Listener l:listeners)
                            l.explorerStartFailed();
                    }
                    else
                    {
                        for(Listener l:listeners)
                            l.explorerStarted();
                        CloudExplorer.this.JSONArrayStack.push( returnObj.getJSONArray("result"));
                        CloudExplorer.this.goToChild(0);//go to root

                    }

                } catch (JSONException e)
                {
                    for(Listener l:listeners)
                        l.explorerStartFailed();
                }
            }
        }.execute(UID);
    }

    public boolean backToParent()
    {

        if(JSONArrayStack.size()<=2) return false;
        JSONArrayStack.pop();
        parentStack.pop();
        try
        {
            JSONArray childs = JSONArrayStack.lastElement();
            ArrayList<File> files = new ArrayList<File>();
            for(int j=0;j<childs.length();j++)
            {
                String name = ((JSONObject) childs.get(j)).getString("name");
                String path = "";
                boolean is_dir = ((JSONObject) childs.get(j)).getString("type").equals("d");
                if(!is_dir) path = ((JSONObject) childs.get(j)).getString("path");
                File f = new File(name,path,is_dir);
                files.add(f);
            }
            for(Listener l:listeners)
                l.listUpdated(files);
        }catch (JSONException e)
        {
            for(Listener l:listeners)
                l.listUpdateFailed();
        }
        return true;
    }

    public void goToChild(int i)
    {
        try
        {
            JSONObject selection = (JSONObject)JSONArrayStack.lastElement().get(i);
            if(!selection.getString("type").equals("d"))
            {
            }
            else
            {
                JSONArray childs = selection.getJSONArray("child");
                ArrayList<File> files = new ArrayList<File>();
                for(int j=0;j<childs.length();j++)
                {
                    String name = ((JSONObject) childs.get(j)).getString("name");
                    String path = "";
                    boolean is_dir = ((JSONObject) childs.get(j)).getString("type").equals("d");
                    if(!is_dir) path = ((JSONObject) childs.get(j)).getString("path");
                    File f = new File(name,path,is_dir);
                    files.add(f);
                }
                JSONArrayStack.push(childs);
                parentStack.push(selection.getString("name"));
                for(Listener l:listeners)
                    l.listUpdated(files);
            }
        }catch (JSONException e)
        {
            for(Listener l:listeners)
                l.listUpdateFailed();
        }
    }

    public void addChild(final String name, final boolean isDir)
    {
        if(isDir && name != null && name.length()>0)
        new AsyncTask<Object, Void, String>() {
            @Override
            protected void onPreExecute() {}

            @Override
            protected String doInBackground(Object... values) {
                HttpPost httppost = new HttpPost(CloudBackupApplication.PHP_ROOT_URL+"add_folder.php");
                HttpClient httpclient = new DefaultHttpClient();
                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                    nameValuePairs.add(new BasicNameValuePair("id", ""+values[0]));
                    nameValuePairs.add(new BasicNameValuePair("parent", ""+values[1]));
                    nameValuePairs.add(new BasicNameValuePair("level", ""+values[2]));
                    nameValuePairs.add(new BasicNameValuePair("folder_name", ""+values[3]));
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
                try
                {
                    JSONObject returnObj = new JSONObject(jsonString);
                    if(returnObj.getInt("response") != 1)
                        for(Listener l:listeners)
                            l.childAddFailed(name, isDir);
                    else
                    {
                        if(returnObj.getInt("affected_row") >0)
                        {
                            for(Listener l:listeners)
                                l.childAdded(name, isDir);
                        }
                        else
                            for(Listener l:listeners)
                                l.childAddFailed(name, isDir);
                    }

                } catch (JSONException e)
                {
                    for(Listener l:listeners)
                        l.childAddFailed(name, isDir);
                }
            }
        }.execute(UID, getCurrentParent(), getCurrentLevel(), name);
    }

    public void update () {

        //listeners = new HashSet<Listener>();
        JSONArrayStack = new Stack<JSONArray>();
        parentStack = new Stack<String>();

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
                Log.d("Tom", jsonString);
                try
                {
                    JSONObject returnObj = new JSONObject(jsonString);
                    if(returnObj.getInt("response") != 1)
                    {
                        for(Listener l:listeners)
                            l.explorerStartFailed();
                    }
                    else
                    {
                        for(Listener l:listeners)
                            l.explorerStarted();
                        CloudExplorer.this.JSONArrayStack.push( returnObj.getJSONArray("result"));
                        CloudExplorer.this.goToChild(0);//go to root

                    }

                } catch (JSONException e)
                {
                    for(Listener l:listeners)
                        l.explorerStartFailed();
                }
            }
        }.execute(UID);

    }

    public static interface Listener
    {
        public void explorerStarted();
        public void explorerStartFailed();
        public void listUpdated(ArrayList<File> list);
        public void listUpdateFailed();
        public void childAdded(String childName, boolean isDir);
        public void childAddFailed(String childName, boolean isDir);
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
