package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by AlexLue on 6/4/17.
 */

public class ListofTasks implements Serializable {

    public String name;
    public String key;
    public String board;
    public String description;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public String getBoard() { return board; }

    public void setBoard(String board) { this.board = board; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> list = new HashMap<String, String>();
        list.put("name", name);
        list.put("key", key);
        list.put("board", board);
        return list;
    }
}
