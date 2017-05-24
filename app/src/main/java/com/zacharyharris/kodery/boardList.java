package com.zacharyharris.kodery;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zacharyharris on 5/24/17.
 */

public class boardList implements Serializable {

    public String name;
    public String key;
    public String board;

    public boardList(){}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public String getBoard() { return  board; }

    public void setBoard(String board) { this.board = board; }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> list = new HashMap<String, String>();
        list.put("name", name);
        list.put("key", key);
        list.put("board", board);

        return list;
    }
}
