package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SmsCodePrefixEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.core.Menu;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.SysPermission;
import cn.ztuo.bitrade.entity.SysRole;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.SysPermissionService;
import cn.ztuo.bitrade.service.SysRoleService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Seven
 * @date 2019年12月20日
 */
@RestController
@RequestMapping(value = "system/role")
@Api(tags = "角色管理")
public class RoleController extends BaseAdminController {

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * 创建或修改角色
     *
     * @param sysRole
     * @param bindingResult
     * @return
     */

    @RequiresPermissions("system:role:merge")
    @RequestMapping("merge")
    @Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建或修改角色SysRole")
    @ApiOperation(value = "创建或修改角色")
    public MessageResult mergeRole(@Valid SysRole sysRole, String code,
                                   @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin,
                                   BindingResult bindingResult) {
        if (sysRole.getId() == null) {
            checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ROLE_ADD_PHONE_PREFIX);
        } else {
            checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ROLE_UPDATE_PHONE_PREFIX);
        }
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        sysRole = sysRoleService.save(sysRole);
        if (sysRole != null) {
            result = success(messageSource.getMessage("SUCCESS"));
            result.setData(sysRole);
            return result;
        } else {
            return MessageResult.error(500, messageSource.getMessage("FAIL"));
        }

    }

    /**
     * 全部权限树
     *
     * @return
     */
    @RequiresPermissions("system:role:merge")
    // @AccessLog(module = AdminModule.SYSTEM, operation = "全部权限树Menu")
    @ApiOperation(value = "权限树")
    @RequestMapping(value = "permission/all", method = RequestMethod.POST)
    @MultiDataSource(name = "second")
    public MessageResult allMenu() {
        List<Menu> list = sysRoleService.toMenus(sysRoleService.getAllPermission(), 0L);
        MessageResult result = success(messageSource.getMessage("SUCCESS"));
        result.setData(list);
        return result;
    }

    /**
     * 角色拥有的权限
     *
     * @param roleId
     * @return
     */
    @RequiresPermissions("system:role:merge")
    @RequestMapping(value = "permission", method = RequestMethod.POST)
    //   @AccessLog(module = AdminModule.SYSTEM, operation = "角色拥有的权限Menu")
    @ApiOperation(value = "角色拥有的权限Menu")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", required = true, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult roleAllPermission(Long roleId) {
        List<Menu> content = sysRoleService.toMenus(sysRoleService.findOne(roleId).getPermissions(), 0L);
        MessageResult result = success();
        result.setData(content);
        return result;
    }

    /**
     * 更改角色权限
     *
     * @param roleId
     * @param permissionId
     * @return
     */
    @RequiresPermissions("system:role:merge")
    @RequestMapping("permission/update")
    @Transactional(rollbackFor = Exception.class)
    @AccessLog(module = AdminModule.SYSTEM, operation = "更改角色拥有的权限Menu")
    public MessageResult updateRolePermission(Long roleId, Long[] permissionId) {
        SysRole sysRole = sysRoleService.findOne(roleId);
        if (permissionId != null) {
            List<SysPermission> list = Arrays.stream(permissionId)
                    .map(x -> sysPermissionService.findOne(x))
                    .collect(Collectors.toList());
            sysRole.setPermissions(list);
        } else {
            sysRole.setPermissions(null);
        }
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * 全部角色
     *
     * @return
     */
    @RequiresPermissions("system:role:merge")
    @RequestMapping(value = "all", method = RequestMethod.POST)
    // @AccessLog(module = AdminModule.SYSTEM, operation = "分页获取所有角色SysRole")
    @ApiOperation(value = "分页获取所有角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer", defaultValue = "10"),

    })
    @MultiDataSource(name = "second")
    public MessageResult getAllRole(PageModel pageModel) {
        Page<SysRole> all = sysRoleService.findAll(new BooleanBuilder(), pageModel.getPageable());
        return success(all);
    }

    /**
     * 删除角色
     *
     * @return
     */
    @RequiresPermissions("system:role:merge")
    @RequestMapping(value = "deletes", method = RequestMethod.POST)
    @AccessLog(module = AdminModule.SYSTEM, operation = "删除角色SysRole")
    @ApiOperation(value = "删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "角色id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "code", value = "短信验证码", required = true, dataType = "String")
    })
    public MessageResult deletes(Long id, String code,
                                 @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ROLE_DEL_PHONE_PREFIX);
        return sysRoleService.deletes(id);
    }


}
