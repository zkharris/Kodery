package com.zacharyharris.kodery.Model;

import java.util.HashMap;

/**
 * Created by zacharyharris on 5/24/17.
 */

public class Update {

    public String text;
    private String board;
    private String key;


    public Update() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<String, String> toFirebaseObject() {
        HashMap<String, String> update = new HashMap<String, String>();
        update.put("text", text);
        update.put("board", board);
        update.put("key", key);

        return update;

    }
}