package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.entity.MemberApplicationConfig;
import cn.ztuo.bitrade.service.MemberApplicationConfigService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/system/member-application-config")
@Api(tags = "实名认证配置")
public class MemberApplicationConfigController {

    @Autowired
    private MemberApplicationConfigService memberApplicationConfigService ;

    @RequiresPermissions("system:member-application-config:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.MEMBER, operation = "实名认证配置修改")
    @ApiOperation(value = "实名认证配置修改")
    public MessageResult merge(@Valid MemberApplicationConfig memberApplicationConfig, BindingResult bindingResult){
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if(result!=null)
            return result ;
        memberApplicationConfigService.save(memberApplicationConfig);
        return MessageResult.getSuccessInstance("保存成功",memberApplicationConfig);
    }

    @RequiresPermissions("system:member-application-config:detail")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.MEMBER, operation = "实名认证配置详情")
    @ApiOperation(value = "获取所有用户")
    @MultiDataSource(name = "second")
    public MessageResult query(){
        return MessageResult.getSuccessInstance("获取成功",memberApplicationConfigService.get());
    }
}
