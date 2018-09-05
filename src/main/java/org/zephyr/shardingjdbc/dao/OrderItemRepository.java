package org.zephyr.shardingjdbc.dao;

import org.springframework.stereotype.Repository;
import org.zephyr.shardingjdbc.model.Order;
import org.zephyr.shardingjdbc.model.OrderItem;

import java.util.List;

/**
 * Created by zephyr on 2018/9/4.
 */
@Repository
public interface OrderItemRepository {

    Long insert(OrderItem model);

    void delete(Long orderItemId);

    List<Order> selectAll();
}
