package org.zephyr.sharding.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.cj.jdbc.Driver;
import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.InlineShardingStrategyConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zephyr on 2018/9/5.
 */
@Configuration
public class ShardingJDBCConfig {

    @Bean
    public DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        //配置默认分库规则
        //按用户id分库
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
                new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<>(), new Properties());
    }

    private TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order");
        //ds${0..1}.t_order${0..1}行表达式 http://shardingsphere.io/document/current/cn/features/sharding/other-features/inline-expression/
        result.setActualDataNodes("ds${0..1}.t_order${0..1}");
        result.setKeyGeneratorColumnName("order_id");
        //配置t_order表分表规则
        result.setTableShardingStrategyConfig(
                new InlineShardingStrategyConfiguration("order_id", "t_order${order_id % 2}"));
        return result;
    }

    private TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order_item");
        result.setActualDataNodes("ds${0..1}.t_order_item${0..1}");
        return result;
    }

    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();

        result.put("ds0", createDataSource("ds0"));
        result.put("ds1", createDataSource("ds1"));
        return result;
    }

    private DataSource createDataSource(final String dataSourceName) {
        //使用druid连接数据库
        DruidDataSource result = new DruidDataSource();
        result.setDriverClassName(Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s?useUnicode=true&characterEncoding=utf8&useSSL=false", dataSourceName));
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
