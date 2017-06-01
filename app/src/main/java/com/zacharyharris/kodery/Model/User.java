package com.zacharyharris.kodery.Model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class User{

    public String username;
    public String email;
    public String uid;
    public String photoURL;
    public String companyName;

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

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public User(String username, String email, String uid, String photoURL) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.photoURL = photoURL;
    }


}
