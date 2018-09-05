package org.zephyr.shardingjdbc.dao;

import org.springframework.stereotype.Repository;
import org.zephyr.shardingjdbc.model.Order;

/**
 * Created by zephyr on 2018/9/4.
 */
@Repository
public interface OrderRepository {

    Long insert(Order model);

}
