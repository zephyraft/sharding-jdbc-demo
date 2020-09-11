package org.zephyr.sharding.hash;

import java.util.*;

/**
 * Created by zephyr on 2018/9/12.
 */
public class ConsistentHashingWithVirtualNode implements ConsistentHashing{

    /**
     * 真实结点列表,考虑到服务器上线、下线的场景，即添加、删除的场景会比较频繁，这里使用LinkedList会更好
     */
    private static final List<String> realNodes = new LinkedList<>();

    /**
     * 虚拟节点，key表示虚拟节点的hash值，value表示虚拟节点的名称
     */
    private static final SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点的数目，这里写死，为了演示需要，一个真实结点对应5个虚拟节点
     */
    private static final int VIRTUAL_NODES = 5;

    private static final String SPLIT = " && ";

    public void initServer(String[] servers) {
        // 先把原始的服务器添加到真实结点列表中
        realNodes.addAll(Arrays.asList(servers));
        // 再添加虚拟节点
        for (String str : realNodes) {
            addServer(str);
        }
    }

    public void addServer(String server) {
        if (!realNodes.contains(server)) {
            realNodes.add(server);
        }

        for (int i = 0; i < VIRTUAL_NODES; i++) {
            String virtualNodeName = server + SPLIT + i;
            int hash = Fnv132Hash.getHash(virtualNodeName);
            virtualNodes.put(hash, virtualNodeName);
        }
    }

    /**
     * 得到应当路由到的结点
     */
    public String getServer(String node) {
        // 得到带路由的结点的Hash值
        int hash = Fnv132Hash.getHash(node);
        // 得到大于该Hash值的所有Map
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        // 第一个Key就是顺时针过去离node最近的那个结点
        String virtualNode;
        if (subMap.isEmpty()) {
            // 环从0点钟重新开始
            virtualNode = virtualNodes.get(virtualNodes.firstKey());
        } else {
            // 返回对应的服务器名称
            virtualNode = subMap.get(subMap.firstKey());
        }
        // 返回对应的虚拟节点名称，这里字符串稍微截取一下
        return virtualNode.substring(0, virtualNode.indexOf(SPLIT));
    }

    @Override
    public SortedMap<Integer, String> getServerMap() {
        return Collections.unmodifiableSortedMap(virtualNodes);
    }

}
