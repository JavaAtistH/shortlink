package com.huangkeqin.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huangkeqin.shortlink.admin.dao.entity.GroupDO;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;


/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);
    /**
     * 新增短链接分组
     * @param groupName 用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username,String groupName);

    /**
     * 查询短链接分组集合
     * @return
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param requestParam
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
