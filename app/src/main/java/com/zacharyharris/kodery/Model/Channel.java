package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zacharyharris on 6/4/17.
 */

public class Channel implements Serializable {

    public String name;
    public String description;
    public String key;
    public String type;
    public Map<String, Object> messages = new HashMap<>();

    public Channel() {}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Map<String, Object> getMessages() { return messages; }

    public void setMessages(Map<String, Object> messages) { this.messages = messages; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> channel = new HashMap<>();
        channel.put("name", name);
        channel.put("description", description);
        channel.put("key", key);
        channel.put("type", type);

        return channel;
    }


}
