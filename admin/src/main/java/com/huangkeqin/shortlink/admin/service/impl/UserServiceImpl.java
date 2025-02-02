package com.huangkeqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.admin.common.convention.exception.ClientException;
import com.huangkeqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.huangkeqin.shortlink.admin.dao.entity.UserDO;
import com.huangkeqin.shortlink.admin.dao.mapper.UserMapper;
import com.huangkeqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserRespDTO;
import com.huangkeqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.huangkeqin.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;


/**
 * 用户接口实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

    @Override
    /**
     * 根据用户名获取用户信息
     *
     * @param userName 用户名，用于查询用户信息
     * @return UserRespDTO 用户响应DTO，包含用户信息
     * @throws ClientException 当用户不存在时，抛出客户端异常
     */
    public UserRespDTO getUserByUsername(String userName) {
        // 创建查询条件，查询用户名与传入参数相同的用户
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userName);
        // 执行查询，获取用户实体
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 如果用户不存在，抛出异常
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        // 创建用户响应DTO对象
        UserRespDTO result = new UserRespDTO();
        // 将用户实体属性复制到响应DTO中
        BeanUtils.copyProperties(userDO, result);
        // 返回用户响应DTO
        return result;
    }

    /**
     * 判断用户名是否已存在
     * 通过查询用户注册缓存的布隆过滤器来实现，以快速判断用户名是否存在
     * 使用布隆过滤器的好处是查询效率高，但可能存在少量误判，即实际上不存在的用户名也可能被判断为存在
     *
     * @param username 待检查的用户名
     * @return 如果布隆过滤器中不包含该用户名，则返回true，表示用户名不存在；否则返回false，表示用户名可能已存在
     */
    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (!hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        //获取分布式锁，确保并发安全
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        try {
            //尝试获取锁成功后，将用户信息插入数据库。
            if (lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (insert < 1) {
                    //如果插入失败，抛出用户新增失败异常
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                //插入成功后，将用户名添加到布隆过滤器中，用于解决缓存穿透问题
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            }
            //如果获取锁失败或用户名已存在，抛出相应异常
            throw new ClientException("用户名已存在");
        } finally {
            //最后释放锁
            lock.unlock();
        }

    }


}
