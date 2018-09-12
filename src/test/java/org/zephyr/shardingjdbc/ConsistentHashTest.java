package org.zephyr.shardingjdbc;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyr.sharding.hash.ConsistentHashing;
import org.zephyr.sharding.hash.ConsistentHashingWithVirtualNode;
import org.zephyr.sharding.hash.ConsistentHashingWithoutVirtualNode;
import org.zephyr.sharding.hash.Fnv132Hash;

import java.util.Map;

/**
 * Created by zephyr on 2018/9/12.
 */
public class ConsistentHashTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashTest.class);

    private static final String[] servers = {"192.168.0.0:111", "192.168.0.1:111", "192.168.0.2:111", "192.168.0.3:111", "192.168.0.4:111"};

    private ConsistentHashingWithVirtualNode consistentHashingWithVirtualNode;
    private ConsistentHashingWithoutVirtualNode consistentHashingWithoutVirtualNode;

    @Before
    public void init() {
        consistentHashingWithVirtualNode = new ConsistentHashingWithVirtualNode();
        consistentHashingWithoutVirtualNode = new ConsistentHashingWithoutVirtualNode();
        // 待添加入Hash环的服务器列表
        consistentHashingWithVirtualNode.initServer(servers);
        consistentHashingWithoutVirtualNode.initServer(servers);
    }

    @Test
    public void testIpWithoutVirtualNode() {
        String[] nodes = {"127.0.0.1:1111", "221.226.0.1:2222", "101.211.121.1:3313", "101.211.121.1:3333", "101.211.121.14:2333", "101.211.121.14:3333"};
        for (String node : nodes) {
            printRoute(node, consistentHashingWithoutVirtualNode);
        }
        addServer(consistentHashingWithoutVirtualNode);
        for (String node : nodes) {
            printRoute(node, consistentHashingWithoutVirtualNode);
        }
    }


    @Test
    public void testIpWithVirtualNode() {
        String[] nodes = {"127.0.0.1:1111", "221.226.0.1:2222", "101.211.121.1:3313", "101.211.121.1:3333", "101.211.121.14:2333", "101.211.121.14:3333"};
        for (String node : nodes) {
            printRoute(node, consistentHashingWithVirtualNode);
        }
        addServer(consistentHashingWithVirtualNode);
        for (String node : nodes) {
            printRoute(node, consistentHashingWithVirtualNode);
        }
    }

    //模拟数据库
    @Test
    public void testIntegerWithoutVirtualNode() {
        for (long i = 1000000L; i <= 1100000L; i = i + 10000) {
            printRoute(String.valueOf(i), consistentHashingWithoutVirtualNode);
        }
        addServer(consistentHashingWithoutVirtualNode);
        for (long i = 1000000L; i <= 1100000L; i = i + 10000) {
            printRoute(String.valueOf(i), consistentHashingWithoutVirtualNode);
        }
    }

    @Test
    public void testIntegerWithVirtualNode() {
        for (long i = 1000000L; i <= 1100000L; i = i + 10000) {
            printRoute(String.valueOf(i), consistentHashingWithVirtualNode);
        }
        addServer(consistentHashingWithVirtualNode);
        for (long i = 1000000L; i <= 1100000L; i = i + 10000) {
            printRoute(String.valueOf(i), consistentHashingWithVirtualNode);
        }
    }

    private void printRoute(String node, ConsistentHashing consistentHashing) {
        LOGGER.info("[{}]的hash值为{}, 被路由到结点[{}]", node,  Fnv132Hash.getHash(node), consistentHashing.getServer(node));
    }

    private void printServerAndHash(ConsistentHashing consistentHashing) {
        LOGGER.info("=== SERVER HASH INFO START ===");
        for (Map.Entry<Integer, String> entry : consistentHashing.getServerMap().entrySet()) {
            LOGGER.info("hash:{} --- server:{}", entry.getKey(), entry.getValue());
        }
        LOGGER.info("=== SERVER HASH INFO END   ===");
    }

    private void addServer(ConsistentHashing consistentHashing) {
        //add "192.168.0.5:111"
        String node = "192.168.0.5:111";
        LOGGER.info("=== ADD NODE [{}] ===", node);
        consistentHashing.addServer(node);
        printServerAndHash(consistentHashing);
    }
}
