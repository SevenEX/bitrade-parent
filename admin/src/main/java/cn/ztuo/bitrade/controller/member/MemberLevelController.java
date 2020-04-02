package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.MemberLevel;
import cn.ztuo.bitrade.service.MemberLevelService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author MrGao
 * @description 会员等级管理类
 * @date 2017/12/27 10:16
 */
@RestController
@Slf4j
@RequestMapping("member/member-level")
@Api(tags = "会员等级管理")
public class MemberLevelController extends BaseAdminController {

    @Autowired
    private MemberLevelService memberLevelService;

    @RequiresPermissions("member:member-level:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "所有会员等级MemberLevel")
    @ApiOperation(value = "获取所有会员等级")
    @MultiDataSource(name = "second")
    public MessageResult findAll() {
        List<MemberLevel> memberLevels = memberLevelService.findAll();
        MessageResult messageResult = success();
        messageResult.setData(memberLevels);
        return messageResult;
    }

    @RequiresPermissions("member:member-level:update")
    @PostMapping("update")
    @AccessLog(module = AdminModule.MEMBER, operation = "更新会员等级MemberLevel")
    @ApiOperation(value = "更新会员等级")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(@Valid MemberLevel memberLevel, BindingResult bindingResult) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        if (memberLevel.getId() == null)
            return error("主键不得为空");
        MemberLevel one = memberLevelService.findOne(memberLevel.getId());
        if (one == null)
            return error("修改对象不存在");
        if (memberLevel.getIsDefault() && !one.getIsDefault())
            //修改对象为默认 原本为false 则 修改默认的等级的isDefault为false
            memberLevelService.updateDefault();
        MemberLevel save = memberLevelService.save(memberLevel);
        return success(save);
    }

}
