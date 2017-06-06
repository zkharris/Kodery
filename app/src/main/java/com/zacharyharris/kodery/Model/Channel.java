package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zacharyharris on 6/4/17.
 */

public class Channel implements Serializable {

    public String name;
    public String key;
    public Map<String, Object> messages = new HashMap<>();

    public Channel() {}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public Map<String, Object> getMessages() { return messages; }

    public void setMessages(Map<String, Object> messages) { this.messages = messages; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> channel = new HashMap<>();
        channel.put("name", name);
        channel.put("key", key);

        return channel;
    }


}
