package com.huanghkeqin.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanghkeqin.shortlink.project.common.convention.exception.ServiceException;
import com.huanghkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huanghkeqin.shortlink.project.dao.mapper.ShortLinkMapper;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.huanghkeqin.shortlink.project.service.ShortLinkService;
import com.huanghkeqin.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 短链接接口实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    /**
     * 创建短链接
     * 此方法接收一个ShortLinkCreateReqDTO对象作为参数，用于创建一个新的短链接
     * 它首先生成短链接的后缀，然后将请求参数中的相同字段名赋值给ShortLinkDO对象
     * （相当于对象拷贝），并组装完整的短链接URL，最后插入数据库
     *
     * @param requestParam 短链接创建请求数据传输对象，包含创建短链接所需的信息
     * @return 返回短链接创建响应数据传输对象，包含生成的完整短链接URL、原始URL和全局ID
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 生成短链接的后缀
        String shortLinkSuffix = generateSuffix(requestParam);
        // 组装完整的短链接URL
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        // 将 requestParam 里面的 相同字段名 赋值给 ShortLinkDO 对象，相当于 对象拷贝
        //ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        // 插入数据库
        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException e) {//保存唯一索引冲突
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            //如果不为空，则说明数据库中确实已经有该短链接，判断为存在，没有误判
            if (hasShortLinkDO != null) {
                log.warn("短链接：{}重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
            //如果为空，则说明数据库中确实没有该短链接，判断为了存在，有误判
        }
        shortUriCreateCachePenetrationBloomFilter.add(shortLinkSuffix);
        // 构建并返回短链接创建响应对象
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
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
            shortUri = HashUtil.hashToBase62(originUrl);
            //如果冲突了,则url+System.currentTimeMillis()重新生成短链接,如果不这样，同一个URL生成的短链接始终是一样的
            //这里加上 System.currentTimeMillis(）不会有什么影响，因为原始链接已经保存在数据库了，短链接与原始链接是映射的关系，
            //  这里只是为了防止重复，加上时间戳
            originUrl += System.currentTimeMillis();
            //布隆过滤器判断数据库中是否存在该短链接
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            } else {
                customGenerate++;
            }
        }
        return shortUri;
    }


    /**
     * 分页查询短链接
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
        IPage<ShortLinkDO> resultPage= baseMapper.selectPage(requestParam, queryWrapper);
        //将查询结果转换为 ShortLinkPageRespDTO 对象
        return resultPage.convert(each -> BeanUtil.toBean(each,ShortLinkPageRespDTO.class));
    }

    /**
     * 统计分组下的短链接数量
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
        List<Map<String,Object>> shortLinkDOlist = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOlist, ShortLinkGroupCountQueryRespDTO.class);
    }
}
