package com.huangkeqin.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.admin.dao.entity.GroupDO;
import com.huangkeqin.shortlink.admin.dao.mapper.GroupMapper;
import com.huangkeqin.shortlink.admin.service.GroupService;
import com.huangkeqin.shortlink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
