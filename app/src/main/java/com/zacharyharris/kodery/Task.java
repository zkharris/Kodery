package com.zacharyharris.kodery;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zacharyharris on 5/23/17.
 */

public class Task implements Serializable {


    private String key;
    private String name;
    private String description;
    private String list;
    private String board;

    public Task() {

    }


    public String getKey() { return key; }

    public void setKey(String key) { this.key = key;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getList() { return list; }

    public void setList(String list) { this.list = list; }

    public String board() { return board; }

    public void setBoard(String board) { this.board = board; }


    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> task =  new HashMap<String,String>();
        task.put("key", key);
        task.put("name", name);
        task.put("description", description);
        task.put("list", list);
        task.put("board", board);

        return task;
    }
}

