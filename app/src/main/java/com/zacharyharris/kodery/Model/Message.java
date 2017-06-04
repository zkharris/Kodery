package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zacharyharris on 6/3/17.
 */

public class Message implements Serializable {

    public String text;
    public String author;
    public String authorUid;
    public String channelKey;
    public String messageKey;
    public String messagePhotoURL;

    public Message(){}

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getMessagePhotoURL() { return messagePhotoURL; }

    public void setMessagePhotoURL(String messagePhotoURL) { this.messagePhotoURL = messagePhotoURL; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getAuthorUid() { return authorUid; }

    public void setAuthorUid(String authorUid) { this.authorUid = authorUid; }

    public String getChannelKey() { return channelKey; }

    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }

    public String getMessageKey() { return messageKey; }

    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> message = new HashMap<>();
        message.put("text", text);
        message.put("messagePhotoURL", messagePhotoURL);
        message.put("author", author);
        message.put("authorUid", authorUid);
        message.put("channelKey", channelKey);
        message.put("messageKey", messageKey);

        return message;
    }
}
