package com.example.polyucloud.app;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Tom on 4/17/14.
 */
public class CloudExplorer {

    private static HashMap<Integer, CloudExplorer> explorers = null;

    private int currentLevel;
    private HashSet<Listener> listeners;

    private CloudExplorer()
    {
        currentLevel = -1;
        listeners = new HashSet<Listener>();
    }

    public static CloudExplorer instantiateFromSession(CloudBackupApplication.Session session)
    {
        if(explorers == null)
            explorers = new HashMap<Integer, CloudExplorer>();
        if(explorers.get(session.UID) == null);
            explorers.put(session.UID, new CloudExplorer());
        return explorers.get(session.UID);
    }

    public boolean addListener(Listener l) { return listeners.add(l); }
    public boolean removeListener(Listener l) { return listeners.remove(l); }

    public void start()
    {

    }


    public static class Listener
    {

    }
}
