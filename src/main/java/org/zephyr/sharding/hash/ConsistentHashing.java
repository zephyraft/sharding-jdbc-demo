package org.zephyr.sharding.hash;

import java.util.SortedMap;

/**
 * Created by zephyr on 2018/9/12.
 */
public interface ConsistentHashing {

    void initServer(String[] servers);

    void addServer(String server);

    String getServer(String node);

    SortedMap<Integer, String> getServerMap();
}
