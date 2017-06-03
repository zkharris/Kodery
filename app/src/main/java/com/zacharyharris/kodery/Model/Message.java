package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zacharyharris on 6/3/17.
 */

public class Message implements Serializable {

    public String text;
    public String author;
    public String channelKey;
    public String messageKey;

    public Message(){}

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getChannelKey() { return channelKey; }

    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }

    public String getMessageKey() { return messageKey; }

    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> message = new HashMap<>();
        message.put("text", text);
        message.put("author", author);
        message.put("channelKey", channelKey);
        message.put("messageKey", messageKey);

        return message;
    }
}
