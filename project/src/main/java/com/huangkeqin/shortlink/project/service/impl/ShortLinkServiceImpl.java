package com.huangkeqin.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.project.common.convention.exception.ClientException;
import com.huangkeqin.shortlink.project.common.convention.exception.ServiceException;
import com.huangkeqin.shortlink.project.common.enums.ValidDateTypeEnum;
import com.huangkeqin.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.huangkeqin.shortlink.project.dao.entity.*;
import com.huangkeqin.shortlink.project.dao.mapper.*;
import com.huangkeqin.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.huangkeqin.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.huangkeqin.shortlink.project.service.ShortLinkService;
import com.huangkeqin.shortlink.project.toolkit.HashUtil;
import com.huangkeqin.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.huangkeqin.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.huangkeqin.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;



    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    /**
     * 创建短链接
     * 此方法接收一个ShortLinkCreateReqDTO对象作为参数，用于创建一个新的短链接
     * 它首先生成短链接的后缀，然后将请求参数中的相同字段名赋值给ShortLinkDO对象
     * （相当于对象拷贝），并组装完整的短链接URL，最后插入数据库
     *
     * @param requestParam 短链接创建请求数据传输对象，包含创建短链接所需的信息
     * @return 返回短链接创建响应数据传输对象，包含生成的完整短链接URL、原始URL和全局ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 生成短链接的后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        // 组装完整的短链接URL
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        // 将 requestParam 里面的 相同字段名 赋值给 ShortLinkDO 对象，相当于 对象拷贝
        //ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        // 插入数据库
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
                throw new ServiceException("短链接生成重复");
        }
        //缓存预热，创建出来就加到缓存中
        stringRedisTemplate.opsForValue()
                .set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        requestParam.getOriginUrl(),
                        LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        // 构建并返回短链接创建响应对象
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(shortLinkDO.getGid())
                .build();
    }

    /**
     * 生成短链接的后缀
     *
     * @param requestParam
     * @return 返回生成的短链接的后缀
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        //使用hashToBase62方法生成短链接可能会发生冲突，所以需要进行重试
        int customGenerate = 0;
        String shortUri;
        while (true) {
            if (customGenerate > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            //原始链接
            String originUrl = requestParam.getOriginUrl();
            //如果冲突了,则url+System.currentTimeMillis()重新生成短链接,如果不这样，同一个URL生成的短链接始终是一样的
            //这里加上 System.currentTimeMillis(）不会有什么影响，因为原始链接已经保存在数据库了，短链接与原始链接是映射的关系，
            //  这里只是为了防止重复，加上时间戳
            //originUrl += System.currentTimeMillis();
            originUrl += UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);

            //布隆过滤器判断数据库中是否存在该短链接
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shortUri)) {
                break;
            } else {
                customGenerate++;
            }
        }
        return shortUri;
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询短链接请求参数
     * @return
     */

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        //构建查询条件
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                //查询 gid 等于 gid 变量的记录
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                //查询 enableStatus 等于 0 的记录
                .eq(ShortLinkDO::getEnableStatus, 0)
                //查询 delFlag 等于 0 的记录
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        //将查询结果转换为 ShortLinkPageRespDTO 对象
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 统计分组下的短链接数量
     *
     * @param requestParam
     * @return
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid,count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOlist = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOlist, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 修改短链接
     * 通过fullShortUrl和gid修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            //如果不存在，则抛出异常
            throw new ClientException("短链接记录不存在");
        }
        //如果存在，创建一个新的短链接，可修改的字段来源于requestParam，其他不可修改的字段，来自查找出来的数据
        //由于是按照gid将短链接分表的，所以gid不同所在的表就不同，所以需要判断gid是否相同，如果相同，则更新，如果不同，则删除原来的，再插入新的
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 为什么监控表要加上Gid？不加的话是否就不存在读写锁
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.delete(linkGotoQueryWrapper);
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
            } finally {
                rLock.unlock();
            }
        }
        // 短链接如何保障缓存和数据库一致性
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())
                || !Objects.equals(hasShortLinkDO.getOriginUrl(), requestParam.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            Date currentDate = new Date();
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(currentDate)) {
                if (Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(currentDate)) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    /**
     * 跳转功能
     *
     * @param shortUri 短链接后缀
     * @param request  请求
     * @param response 响应
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        //获取域名
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        //域名拼接短链接后缀
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        //原始短链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(fullShortUrl, null, request, response);
            response.sendRedirect(originalLink);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        //如果布隆过滤器中不存在，则直接返回
        if (!contains) {
            response.sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            response.sendRedirect("/page/notfound");
            return;
        }
        //分布式锁解决缓存击穿。  String.format fullShortUrl 的值格式化到 LOCK_GOTO_SHORT_LINK_KEY 中的指定位置，生成一个新的字符串。
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双重锁
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                shortLinkStats(fullShortUrl, null, request, response);
                response.sendRedirect(originalLink);
                return;
            }
            //查询短链接对应的linkGoto
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                //当没有此端链接的时候
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                //严谨来说此处要进行封控
                return;
            }
            //用linkGoto里面的gid查找t_link中短链接原始url
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())) {
                //当查出来的短链接有效时间小于当前时间，处理方法和没有查到短链接的时候一样处理
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                return;
            }
            //缓存预热，创建出来就加到缓存中
            stringRedisTemplate.opsForValue()
                    .set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl)
                            , shortLinkDO.getOriginUrl()
                            , LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            //不为空，重定向到原始url
            response.sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            //释放锁
            lock.unlock();
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .currentDate(new Date())
                .build();
    }

    @Override
    public void shortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 消息队列为什么选用RocketMQ

        shortLinkStatsSaveProducer.send(producerMap);
    }
    /**
     * 统计功能，监控锻炼链接的pv,uv,ip
     *
     * @param fullShortUrl
     * @param gid
     * @param request
     * @param response
     */
    private void shortLinkStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = request.getCookies();
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
                //设置UV，判断Cookie是否是同一个
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setMaxAge(3600 * 24 * 30);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                response.addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> each.getName().equals("uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }
            //获取访问ip
            String remoteAddr = LinkUtil.getActualIp(request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            //获取小时
            int hour = DateUtil.hour(new Date(), true);
            //获取星期几
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeParamMap = new HashMap<>();
            //https://restapi.amap.com/v3/ip?ip=114.247.50.2&output=xml&key=<用户的key>
            //高德地图API用户密钥
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResult = JSON.parseObject(localeResultStr);
            String province = localeResult.getString("province");
            String city = localeResult.getString("city");
            String infoCode = localeResult.getString("infocode");
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : city)
                        .adcode(unknownFlag ? "未知" : localeResult.getString("adcode"))
                        .gid(gid)
                        .date(new Date())
                        .country("China")
                        .cnt(1)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);
                String os = LinkUtil.getOs(request);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .cnt(1)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
                String browser = LinkUtil.getBrowser(request);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .cnt(1)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
                String device = LinkUtil.getDevice(request);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
                String network = LinkUtil.getNetwork(request);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .network(network)
                        .cnt(1)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(uv.get())
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-", "中国", linkLocaleStatsDO.getProvince(), linkLocaleStatsDO.getCity()))
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
            }
        } catch (Throwable ex) {
            log.error("短链接统计短链接访问异常", ex);
        }
    }

    /**
     * 获取短链接原始链接网站图标
     *
     * @param url
     * @return
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}
