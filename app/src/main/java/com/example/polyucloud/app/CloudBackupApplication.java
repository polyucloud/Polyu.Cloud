package com.example.polyucloud.app;

import android.app.Application;

/**
 * Created by Tom on 4/16/14.
 */
public class CloudBackupApplication extends Application {
    public static final String PHP_ROOT_URL = "http://daisunhong.com/polyucloud/php/";
    public static final String FILE_ROOT_URL = "http://daisunhong.com/polyucloud/user_files/";

    public Session currentSession = null;

    public static class Session {

        public final int UID;
        public final String FIRST_NAME, LAST_NAME, EMAIL;

        public Session(int id, String email, String first, String last)
        {
            this.UID = id;
            this.EMAIL = email;
            this.FIRST_NAME = first;
            this.LAST_NAME = last;
        }
    }
}
