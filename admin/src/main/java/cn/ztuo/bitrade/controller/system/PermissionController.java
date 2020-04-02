package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.QSysPermission;
import cn.ztuo.bitrade.entity.SysPermission;
import cn.ztuo.bitrade.service.SysPermissionService;
import cn.ztuo.bitrade.util.MessageResult;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping("/system/permission")
@Api(tags = "权限管理")
public class PermissionController extends BaseController {

    @Autowired
    private SysPermissionService sysPermissionService;

   // @RequiresPermissions("system:permission:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建/修改权限")
    @ApiOperation(value = "创建/修改权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", required = false, dataType = "Long"),
            @ApiImplicitParam(name = "title", value = "权限名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "description", value = "描述", required = false, dataType = "String"),
            @ApiImplicitParam(name = "parentId", value = "上级id（0表示是菜单）", required = false, dataType = "Long"),
            @ApiImplicitParam(name = "name", value = "权限代码", required = false, dataType = "String"),
    })
    public MessageResult merge(@Valid SysPermission permission) {
        if(permission.getId()==null) {
            SysPermission data = sysPermissionService.findByPermissionName(permission.getName());
            if (data != null) {
                return error("权限名重复");
            }
        }else {
            SysPermission data = sysPermissionService.findOne(permission.getId());
            if(!data.getName().equalsIgnoreCase(permission.getName())){
                SysPermission s =  sysPermissionService.findByPermissionName(permission.getName());
                if(s!=null){
                    return error("权限名重复");
                }
            }
        }
        permission = sysPermissionService.save(permission);
        MessageResult result = success("保存权限成功");
        result.setData(permission);
        return result;
    }


    // @RequiresPermissions("system:permission:merge")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "分页查询权限")
    @ApiOperation(value = "分页查询权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer",defaultValue = "10"),
            @ApiImplicitParam(name = "parentId", value = "上级id", required = false, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(PageModel pageModel,
                                   @RequestParam(value = "parentId", required = false) Long parentId) {
        BooleanExpression predicate = QSysPermission.sysPermission.id.isNotNull();
        if (parentId != null) {
            predicate = QSysPermission.sysPermission.parentId.eq(parentId);
            if ((parentId+"").equals("0")){
                pageModel.setPageSize(100);
            }
        }
        Page<SysPermission> all = sysPermissionService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    // @RequiresPermissions("system:permission:merge")
    @PostMapping("/detail")
    @AccessLog(module = AdminModule.SYSTEM, operation = "权限详情")
    @ApiOperation(value = "权限详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", required = true, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        SysPermission sysPermission = sysPermissionService.findOne(id);
        Assert.notNull(sysPermission, "该权限不存在");
        return MessageResult.getSuccessInstance("查询权限成功", sysPermission);
    }

    // @RequiresPermissions("system:permission:merge")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "批量删除权限")
    @ApiOperation(value = "删除权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "权限ids", required = true, dataType = "Long[]"),
    })
    public MessageResult deletes(@RequestParam(value = "ids") Long[] ids) {
        sysPermissionService.deletes(ids);
        return MessageResult.success("批量删除权限成功");
    }

}
