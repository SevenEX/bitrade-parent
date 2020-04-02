package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.create.AppRevisionCreate;
import cn.ztuo.bitrade.model.screen.AppRevisionScreen;
import cn.ztuo.bitrade.model.update.AppRevisionUpdate;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.AppRevision;
import cn.ztuo.bitrade.model.create.AppRevisionCreate;
import cn.ztuo.bitrade.model.screen.AppRevisionScreen;
import cn.ztuo.bitrade.model.update.AppRevisionUpdate;
import cn.ztuo.bitrade.service.AppRevisionService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.create.AppRevisionCreate;
import cn.ztuo.bitrade.model.screen.AppRevisionScreen;
import cn.ztuo.bitrade.model.update.AppRevisionUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:31
 */
@RestController
@RequestMapping("system/app-revision")
@Api(tags = "APP版本管理")
public class AppRevisionController extends BaseAdminController {
    @Autowired
    private AppRevisionService service;

    //新增
    @PostMapping
    @ApiOperation(value = "新增APP版本")
    @AccessLog(module = AdminModule.CMS, operation = "新增APP版本")
    @RequiresPermissions("cms:app")
    public MessageResult create(@Valid AppRevisionCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) return result;
        service.save(model);
        return success();
    }

    //更新
    @PutMapping("{id}")
    @ApiOperation(value = "更新APP版本")
    @AccessLog(module = AdminModule.CMS, operation = "更新APP版本")
    @RequiresPermissions("cms:app")
    public MessageResult put(@PathVariable("id") Long id, AppRevisionUpdate model) {
        AppRevision appRevision = service.findById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        service.update(model, appRevision);
        return success();
    }

    //详情
    @GetMapping("{id}")
    @ApiOperation(value = "APP版本详情")
    @RequiresPermissions("cms:app")
   // @AccessLog(module = AdminModule.APP, operation = "APP版本详情")
    @MultiDataSource(name = "second")
    public MessageResult get(@PathVariable("id") Long id) {
        AppRevision appRevision = service.findById(id);
        Assert.notNull(appRevision, "validate appRevision id!");
        return success(appRevision);
    }

    //分页
    @GetMapping("page-query")
    @ApiOperation(value = "分页查询APP版本")
    @RequiresPermissions("cms:app")
   // @AccessLog(module = AdminModule.APP, operation = "分页查询APP版本")
    @MultiDataSource(name = "second")
    public MessageResult get(PageModel pageModel, AppRevisionScreen screen) {
        return success(service.findAllScreen(screen, pageModel));
    }
}
