package com.zacharyharris.kodery;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

/**
 * Created by zacharyharris on 5/23/17.
 */

@IgnoreExtraProperties
public class User{

    private String username;
    private String email;
    private String uid;
    private String photoURL;

    public User() {
        // Default constructer
    }

    public String getUsername() { return username;}

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid;}

    public String getPhotoURL() { return photoURL; }

    public void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public User(String username, String email, String uid, String photoURL) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.photoURL = photoURL;
    }


}
