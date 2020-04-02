package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.SysLocalization;
import cn.ztuo.bitrade.service.LocalizationService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/cms/localization")
@Api(tags = "国际化")
public class LocalizationController extends BaseController {
    @Autowired
    private LocalizationService localizationService;

    //@RequiresPermissions("cms:system-localization:update")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.CMS, operation = "更新国际化配置")
    public MessageResult update(@Valid SysLocalization localization, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        return success(localizationService.updateMessage(localization));
    }

   // @RequiresPermissions("cms:system-localization:all")
    @PostMapping("/all")
    @AccessLog(module = AdminModule.CMS, operation = "查询国际化配置")
    public MessageResult all() {
        return success(localizationService.getAllMessageWithoutCache());
    }
}
