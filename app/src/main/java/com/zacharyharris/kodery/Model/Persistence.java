package com.zacharyharris.kodery.Model;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by zacharyharris on 6/23/17.
 */

public class Persistence extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

}


