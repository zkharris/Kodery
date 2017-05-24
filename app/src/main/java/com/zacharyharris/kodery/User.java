package com.zacharyharris.kodery;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

/**
 * Created by zacharyharris on 5/23/17.
 */

@IgnoreExtraProperties
public class User implements Serializable{

    private String username;
    private String email;
    private String uid;
    private String photoURL;

    public User() {
        // Default constructer
    }

    public String getUsername() { return username;}

    public void setUsername(String username) { this.username = username; }

    private String getEmail() { return email; }

    private void setEmail(String email) { this.email = email; }

    private String getUid() { return uid; }

    private void setUid(String uid) { this.uid = uid;}

    private String getPhotoURL() { return photoURL; }

    private void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public User(String username, String email, String uid, String photoURL) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.photoURL = photoURL;
    }


}
