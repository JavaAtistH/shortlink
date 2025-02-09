package com.huangkeqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.admin.dao.entity.GroupDO;
import com.huangkeqin.shortlink.admin.dao.mapper.GroupMapper;
import com.huangkeqin.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.huangkeqin.shortlink.admin.service.GroupService;
import com.huangkeqin.shortlink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(RandomGenerator.generateRandom())
                .sortOrder(0)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 判断Gid是否存在
     * @param gid
     * @return
     */
    private boolean hasGid(String gid){
        //使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                /*GroupDO::getGid 这个写法是方法引用，它的作用是获取 GroupDO 实体类中的 gid 字段，并用于构造查询条件*/
                //查询 gid 等于 gid 变量的记录
                .eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getGid, gid)
                //TODO 设置用户名
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }

    /**
     * 查询短链接分组集合
     * @return
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        //TODO 获取用户名
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, "huangkeqin")
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }
}
