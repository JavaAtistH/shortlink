package com.huanghkeqin.shortlink.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration
public class RBloomFilterConfiguration {
    /**
     * 防止短链接创建查询数据库的布隆过滤器
     */
    @Bean
    //该方法用于创建并初始化一个布隆过滤器，防止用户注册时查询数据库。
    public RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter(RedissonClient redissonClient) {
        // 获取名为"userRegisterCachePenetrationBloomFilter"的布隆过滤器实例
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("shortUriCreateCachePenetrationBloomFilter");
        // 尝试初始化布隆过滤器，参数分别为预计插入的元素数量和假阳性率
        cachePenetrationBloomFilter.tryInit(100000000L, 0.001);
        // 返回初始化过的布隆过滤器实例
        return cachePenetrationBloomFilter;
    }
}
