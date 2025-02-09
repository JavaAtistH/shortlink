package com.huangkeqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.admin.common.convention.exception.ClientException;
import com.huangkeqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.huangkeqin.shortlink.admin.dao.entity.UserDO;
import com.huangkeqin.shortlink.admin.dao.mapper.UserMapper;
import com.huangkeqin.shortlink.admin.dto.req.UserLoginReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserRespDTO;
import com.huangkeqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.huangkeqin.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.huangkeqin.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.huangkeqin.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIST;


/**
 * 用户接口实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

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
            throw new ClientException(USER_NAME_EXIST);
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
                return;
            }
            //如果获取锁失败或用户名已存在，抛出相应异常
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            //最后释放锁
            lock.unlock();
        }

    }

    /**
     * 修改用户信息
     *
     * @param requestParam 修改用户名参数
     */
    @Override
    public void update(UserUpdateReqDTO requestParam) {
        //TODO 验证当前用户是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    /**
     * 用户登录方法
     *
     * @param requestParam 用户登录请求DTO，包含用户名和密码
     * @return 用户登录响应DTO，包含登录成功的令牌
     * @throws ClientException 如果用户不存在，则抛出客户端异常
     */
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        // 根据查询条件从数据库中查询用户信息
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 如果用户信息为空，抛出用户不存在异常
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        // 从Redis中获取用户登录信息
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        // 如果用户已登录，更新登录状态有效期并返回token
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
            // 从登录信息中提取token
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        // 如果用户未登录，生成新的token并保存用户登录状态到Redis
        String uuid = UUID.randomUUID().toString();
        // 使用Redis的Hash操作，将用户登录信息存储到缓存中
        // 这里的键是用户登录键加上用户名，值是用户的UUID和用户信息的JSON字符串表示
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        // 设置用户登录信息在缓存中的过期时间
        // 这里设置30分钟的过期时间，以确保用户信息的安全性和缓存的有效性  expire:到期
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查用户是否登录
     *
     * @param token 用户令牌
     * @return 用户是否登录
     */
    @Override
    public Boolean checkLogin(String username,String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY+username, token) != null;
    }

    /**
     * 用户退出登录
     * @param username
     * @param token
     */
    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }

}
