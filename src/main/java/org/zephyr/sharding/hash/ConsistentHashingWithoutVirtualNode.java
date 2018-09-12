package org.zephyr.sharding.hash;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by zephyr on 2018/9/12.
 */
public class ConsistentHashingWithoutVirtualNode implements ConsistentHashing{

    /**
     * key表示服务器的hash值，value表示服务器的名称
     */
    private static SortedMap<Integer, String> sortedMap = new TreeMap<>();

    public void initServer(String[] servers) {
        //程序初始化，将所有的服务器放入sortedMap中
        for (String server : servers) {
            addServer(server);
        }
    }

    public void addServer(String server) {
        int hash = Fnv132Hash.getHash(server);
        sortedMap.put(hash, server);
    }

    /**
     * 得到应当路由到的结点
     */
    public String getServer(String node) {
        // 得到带路由的结点的Hash值
        int hash = Fnv132Hash.getHash(node);
        // 得到大于该Hash值的所有Map
        SortedMap<Integer, String> subMap = sortedMap.tailMap(hash);
        // 第一个Key就是顺时针过去离node最近的那个结点
        if (subMap.isEmpty()) {
            // 环从0点钟重新开始
            return sortedMap.get(sortedMap.firstKey());
        } else {
            // 返回对应的服务器名称
            return subMap.get(subMap.firstKey());
        }
    }

    public SortedMap<Integer, String> getServerMap() {
        return Collections.unmodifiableSortedMap(sortedMap);
    }
}
