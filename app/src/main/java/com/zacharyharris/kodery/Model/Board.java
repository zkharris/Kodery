package com.zacharyharris.kodery.Model;

import java.io.Serializable;
import java.util.HashMap;

public class Board implements Serializable {

    public String name;
    public String owner;
    public String ownerUid;
    public String boardKey;
    //public String hc;
    private HashMap<String, Object> peeps;

    public Board(){}

    //public String getHc() { return hc; }

    //public void setHc(String hc) { this.hc = hc;}

    public String getName() { return  name; }

    public void setName(String name) { this.name = name; }

    public String getOwner() { return  owner; }

    public void setOwner(String owner) { this.owner = owner; }

    public String getOwnerUid() { return ownerUid; }

    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }

    public String getBoardKey() { return boardKey; }

    public void setBoardKey(String boardKey) { this.boardKey = boardKey; }

    public HashMap<String, Object> getPeeps() {
        return peeps;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String, String> board = new HashMap<>();
        board.put("name", name);
        board.put("owner", owner);
        board.put("ownerUid", ownerUid);
        board.put("boardKey", boardKey);
        //board.put("HC", hc);

        return board;
    }
}
