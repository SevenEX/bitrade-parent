package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Department;
import cn.ztuo.bitrade.service.DepartmentService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Seven
 * @date 2019年12月20日
 */
@RestController
@RequestMapping(value = "/system/department")
@Api(tags = "部门管理（暂弃）")
public class DepartmentController extends BaseAdminController {

    @Autowired
    private DepartmentService departmentService;


    /**
     * 创建或更新部门
     *
     * @param department
     * @return
     */
    @RequiresPermissions("system:department:merge")
    @RequestMapping("merge")
    @AccessLog(module = AdminModule.SYSTEM, operation = "创建或更新部门Department")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult save(@Valid Department department, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        if (department.getId() != null) {
            department.setCreateTime(departmentService.findOne(department.getId()).getCreateTime());
        }
        departmentService.save(department);
        return success();
    }

    /**
     * 部门详情
     *
     * @param departmentId
     * @return
     */
    @RequiresPermissions("system:department:detail")
    @RequestMapping("detail")
    //@AccessLog(module = AdminModule.SYSTEM, operation = "部门Department详情")
    public MessageResult detail(Long departmentId) {
        Department department = departmentService.getDepartmentDetail(departmentId);
        return success(department);
    }

    /**
     * 全部部门
     *
     * @return
     */
    @RequiresPermissions("system:department:all")
    @RequestMapping("all")
   // @AccessLog(module = AdminModule.SYSTEM, operation = "所有部门Department")
    public MessageResult allDepartment(PageModel pageModel) {
        Page<Department> all = departmentService.findAll(new BooleanBuilder(), pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("system:department:deletes")
    @RequestMapping("deletes")
    @AccessLog(module = AdminModule.SYSTEM, operation = "批量删除部门")
    public MessageResult deletes(@RequestParam(value = "id") Long id) {
        MessageResult result = departmentService.deletes(id);
        return result;
    }

}
