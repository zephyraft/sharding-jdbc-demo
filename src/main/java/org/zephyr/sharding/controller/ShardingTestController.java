package org.zephyr.sharding.controller;

import io.shardingsphere.core.keygen.DefaultKeyGenerator;
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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zephyr on 2018/9/4.
 */
@RestController
@RequestMapping("sharding")
public class ShardingTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingTestController.class);

    private OrderRepository orderRepository;

    @Autowired
    public ShardingTestController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private static final int THREAD_NUM = 200;

    @GetMapping("echo/{string}")
    public DataResult echo(@PathVariable String string) {
        return new SuccessResult(string);
    }

    @GetMapping("selectFirstPage")
    public DataResult selectFirstPage() {
        return new SuccessResult(orderRepository.selectFirstPage());
    }

    @GetMapping("delete/{id}")
    public DataResult deleteById(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return new SuccessResult();
    }

    @GetMapping("insert")
    public DataResult testInsert() {
        // sharding-jdbc默认的分布式主键生成器，采用snowflake算法实现，
        // 主键可以保证递增，但无法保证连续。而snowflake算法的最后4位是在同一毫秒内的访问递增值。
        // 因此，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。
        DefaultKeyGenerator defaultKeyGenerator = new DefaultKeyGenerator();

        CyclicBarrier cyclicBarrier = new CyclicBarrier(THREAD_NUM);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            executorService.submit(getInsertRunnable(cyclicBarrier, defaultKeyGenerator, i));
        }
        return new SuccessResult();
    }

    private Runnable getInsertRunnable(CyclicBarrier cyclicBarrier, DefaultKeyGenerator defaultKeyGenerator, int i) {
        return () -> {
            try {
                LOGGER.info("await:{}", i);
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                LOGGER.error("", e);
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                LOGGER.error("", e);
            }

            //同时触发insert，增加并发，使订单分布均匀
            Order order = new Order();
            order.setOrderId(defaultKeyGenerator.generateKey().longValue());
            order.setUserId(i % 2 + 1);
            order.setStatus("1");
            orderRepository.insert(order);
            LOGGER.info("finish:{}", i);
        };
    }
}
