package org.zephyr.sharding.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zephyr.sharding.common.DataResult;
import org.zephyr.sharding.common.SuccessResult;
import org.zephyr.sharding.entity.Order;
import org.zephyr.sharding.repository.OrderRepository;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zephyr on 2018/9/4.
 */
@RestController
@RequestMapping("sharding")
public class ShardingTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingTestController.class);

    private OrderRepository orderRepository;

    private static final int THREAD_NUM = 4 * 2 + 1;
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM,
                    0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Autowired
    public ShardingTestController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("echo/{string}")
    public DataResult echo(@PathVariable String string) {
        return new SuccessResult(string);
    }

    @GetMapping("selectFirstPage")
    public DataResult selectFirstPage() {
        return new SuccessResult(orderRepository.selectFirstPage());
    }

    @GetMapping("select/{id}")
    public DataResult selectById(@PathVariable Long id) {
        return new SuccessResult(orderRepository.selectById(id));
    }

    @GetMapping("delete/{id}")
    public DataResult deleteById(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return new SuccessResult();
    }

    @GetMapping("update/{id}")
    public DataResult updateById(@PathVariable Long id) {
        Order order = orderRepository.selectById(id);
        order.setOrderId(id);
        order.setStatus("2");
        if (order.getUserId() % 2 == 0) {
            //更改用户id，可以正常切换数据所在的库
            order.setUserId(1);
        } else {
            order.setUserId(2);
        }
        orderRepository.updateById(order);
        return new SuccessResult();
    }

    @GetMapping("insert")
    public DataResult testInsert() {
        // sharding-jdbc默认的分布式主键生成器，采用snowflake算法实现，
        // 主键可以保证递增，但无法保证连续。而snowflake算法的最后4位是在同一毫秒内的访问递增值。
        // 因此，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。

        // 不需要显示指定主键，sharding-jdbc会根据配置规则自行加上主键。
        // 自行new DefaultKeyGenerator会很容易导致主键重复
        for (int i = 0; i < 10000; i++) {
            THREAD_POOL_EXECUTOR.execute(getInsertRunnable(i));
        }
        return new SuccessResult();
    }

    @GetMapping("threadPoolActiveCount")
    public DataResult threadPoolActiveCount() {
        int num = THREAD_POOL_EXECUTOR.getActiveCount();
        LOGGER.info("active count: {}", num);
        return new SuccessResult(num);
    }

    private Runnable getInsertRunnable(int i) {
        return () -> {
            Order order = new Order();
            order.setUserId(i % 2 + 1);
            order.setStatus("1");
            orderRepository.insert(order);
        };
    }
}
