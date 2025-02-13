package com.huangkeqin.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huangkeqin.shortlink.admin.common.biz.user.UserContext;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.dao.entity.GroupDO;
import com.huangkeqin.shortlink.admin.dao.mapper.GroupMapper;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.huangkeqin.shortlink.admin.remote.ShortLinkRemoteService;
import com.huangkeqin.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huangkeqin.shortlink.admin.service.GroupService;
import com.huangkeqin.shortlink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(RandomGenerator.generateRandom())
                .sortOrder(0)
                .username(UserContext.getUsername())
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 判断Gid是否存在
     *
     * @param gid
     * @return
     */
    private boolean hasGid(String gid) {
        //使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                /*GroupDO::getGid 这个写法是方法引用，它的作用是获取 GroupDO 实体类中的 gid 字段，并用于构造查询条件*/
                //查询 gid 等于 gid 变量的记录
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag == null;
    }

    /**
     * 查询短链接分组集合
     *
     * @return
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        //使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                //查询 delFlag 等于 0 的记录
                .eq(GroupDO::getDelFlag, 0)
                //查询 username 等于 当前用户名 的记录
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        //远程调用查询分组内短链接数量
        // groupDOList.stream().map(GroupDO::getGid).toList()是一个流式操作，
        // 用于获取 groupDOList 中每个 GroupDO 对象的 gid，并返回一个包含这些 gid 的列表。
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService
                .linkGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());
        // 将 groupDOList 中的每个 GroupDO 对象转换为 ShortLinkGroupRespDTO 对象，
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
        shortLinkGroupRespDTOList.forEach(each -> {
            //TODO 理解代码
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream().
                    filter(item -> Objects.equals(item.getGid(), each.getGid())).
                    findFirst();
            each.setShortLinkCount(first.get().getShortLinkCount());
        });
        return shortLinkGroupRespDTOList;
    }

    /**
     * 更新短链接分组名称
     *
     * @param requestParam
     */
    @Override
    /**
     * 更新短链接组信息
     * 此方法根据用户请求参数更新特定短链接组的信息
     * 它首先构建一个更新条件，确保只有当前用户拥有的、未删除的组才能被更新
     * 然后，根据请求参数中的组名创建一个新的GroupDO实例，并应用这些更新
     *
     * @param requestParam 包含要更新的组信息的请求参数对象
     */
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        // 构建更新条件，确保更新操作只针对当前用户、指定组ID且未被禁用的组
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        /*UPDATE group_table SET name = ? WHERE username = ? AND gid = ? AND del_flag = 0;*/
        // 创建一个新的GroupDO实例，并设置新的组名
        GroupDO groupDO = new GroupDO();
        //MyBatis-Plus 的 update(T entity, Wrapper<T> updateWrapper) 方法 只会更新 非 null 的字段
        groupDO.setName(requestParam.getName());
        // 执行更新操作，应用上述设置的更新条件和新的组信息
        baseMapper.update(groupDO, updateWrapper);
    }

    /**
     * 删除用户短链接分组
     *
     * @param gid
     */
    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> deleteWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        // 执行更新操作，应用上述设置的更新条件和新的组信息
        baseMapper.update(groupDO, deleteWrapper);
    }

    /**
     * 短链接分组排序
     * 这里是前端将分组序号传递过来，后端将每个用户下面的分组序号在数据库里面修改就是
     *
     * @param requestParam
     */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        //forEach循环遍历集合里面的元素，获取前端传过来的每个分组的序号值将其修改
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }
}
