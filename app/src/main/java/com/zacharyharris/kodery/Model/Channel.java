package com.zacharyharris.kodery.Model;

import java.io.Serializable;

/**
 * Created by zacharyharris on 6/4/17.
 */

public class Channel implements Serializable {

    public String name;
    public String channelKey;

    public Channel() {}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getChannelKey() { return channelKey; }

    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }


}
