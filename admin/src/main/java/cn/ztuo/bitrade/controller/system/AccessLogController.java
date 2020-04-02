package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.AdminAccessLog;
import cn.ztuo.bitrade.entity.QAdmin;
import cn.ztuo.bitrade.entity.QAdminAccessLog;
import cn.ztuo.bitrade.service.AdminAccessLogService;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 日志管理
 * @date 2017/12/22 17:27
 */
@Slf4j
@RestController
@RequestMapping("/system/access-log")
@Transactional(readOnly = true)
@Api(tags = "日志管理")
public class AccessLogController extends BaseAdminController {

    @Autowired
    private AdminAccessLogService adminAccessLogService;

    @RequiresPermissions("system:access-log:page-query")
    @GetMapping("/all")
    //@AccessLog(module = AdminModule.SYSTEM, operation = "所有操作/访问日志AdminAccessLog")
    @ApiOperation(value = "后台所有操作/访问日志")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<AdminAccessLog> adminAccessLogList = adminAccessLogService.queryAll();
        return success(adminAccessLogList);
    }

    @RequiresPermissions("system:access-log:page-query")
    @GetMapping("/{id}")
    // @AccessLog(module = AdminModule.SYSTEM, operation = "操作/访问日志AdminAccessLog 详情")
    @ApiOperation(value = "操作/访问日志详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "操作id", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult detail(@PathVariable("id") Long id) {
        AdminAccessLog adminAccessLog = adminAccessLogService.queryById(id);
        notNull(adminAccessLog, "validate id!");
        return success(adminAccessLog);
    }

    @RequiresPermissions("system:access-log:page-query")
    @GetMapping("/page-query")
    // @AccessLog(module = AdminModule.SYSTEM, operation = "分页查找操作/访问日志AdminAccessLog")
    @ApiOperation(value = "分页查找操作/访问日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer", defaultValue = "10"),
            @ApiImplicitParam(name = "adminId", value = "管理员id", dataType = "Long"),
            @ApiImplicitParam(name = "module", value = "操作id", dataType = "Integer"),
            @ApiImplicitParam(name = "accessIp", value = "IP地址", dataType = "String"),
            @ApiImplicitParam(name = "adminName", value = "账号", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间(yyyy-MM-dd)", dataType = "Date"),
            @ApiImplicitParam(name = "endTime", value = "结束时间(yyyy-MM-dd)", dataType = "Date"),
    })
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(
            PageModel pageModel,
            @RequestParam(value = "adminId", required = false) Long adminId,
            @RequestParam(value = "accessIp", required = false) String accessIp,
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "adminName", required = false) String adminName,
            @RequestParam(value = "module", required = false) AdminModule module) {

        List<BooleanExpression> list = new ArrayList<>();
        list.add(QAdmin.admin.id.eq(QAdminAccessLog.adminAccessLog.adminId));
        if (null != adminId) {
            list.add(QAdmin.admin.id.eq(adminId));
        }
        if (!StringUtils.isEmpty(adminName)) {
            list.add(QAdmin.admin.username.like("%" + adminName + "%"));
        }
        if (module != null) {
            list.add(QAdminAccessLog.adminAccessLog.module.eq(module));
        }
        if (!StringUtils.isEmpty(accessIp)) {
            list.add(QAdminAccessLog.adminAccessLog.accessIp.like("%" + accessIp + "%"));
        }
        if (startTime != null) {
            list.add(QAdminAccessLog.adminAccessLog.accessTime.after(startTime));
        }
        if (endTime != null) {
            list.add(QAdminAccessLog.adminAccessLog.accessTime.before(DateUtils.addDays(endTime,1)));
        }
        Page<AdminAccessLog> all = adminAccessLogService.page(list, pageModel);
        return success(all);
    }


}
