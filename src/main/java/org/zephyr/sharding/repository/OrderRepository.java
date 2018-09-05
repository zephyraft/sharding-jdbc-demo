package org.zephyr.sharding.repository;

import org.springframework.stereotype.Repository;
import org.zephyr.sharding.entity.Order;

import java.util.List;

/**
 * Created by zephyr on 2018/9/4.
 */
@Repository
public interface OrderRepository {

    Long insert(Order model);

    List<Order> selectFirstPage();
}
