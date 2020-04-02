package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.SysAdvertiseScreen;
import cn.ztuo.bitrade.util.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SysAdvertiseLocation;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.QSysAdvertise;
import cn.ztuo.bitrade.entity.SysAdvertise;
import cn.ztuo.bitrade.model.screen.SysAdvertiseScreen;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.SysAdvertiseService;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.SysAdvertiseScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.ztuo.bitrade.entity.QSysAdvertise.sysAdvertise;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 系统广告
 * @date 2018/1/6 15:03
 */
@Slf4j
@RestController
@RequestMapping("/cms/system-advertise")
@Api(tags = "系统广告/轮播")
public class AdvertiseController extends BaseAdminController {
    @Autowired
    private SysAdvertiseService sysAdvertiseService;
    @Autowired
    private LocaleMessageSourceService msService;

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "创建轮播")
    @ApiOperation(value = "创建轮播")
    public MessageResult create(@Valid SysAdvertise sysAdvertise, BindingResult bindingResult) {
        if(StringUtils.isNotEmpty(sysAdvertise.getEndTime()) && StringUtils.isNotEmpty(sysAdvertise.getStartTime())){
            Date end = DateUtil.strToDate(sysAdvertise.getEndTime());
            Date start = DateUtil.strToDate(sysAdvertise.getStartTime());
            Assert.isTrue(end.after(start), msService.getMessage("START_END_TIME"));
        }
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        sysAdvertise.setSerialNumber(UUIDUtil.getUUID());
        sysAdvertise.setCreateTime(DateUtil.getCurrentDate());
        SysAdvertise one = sysAdvertiseService.save(sysAdvertise);
        return success(one);
    }

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/all")
    //@AccessLog(module = AdminModule.ADVERTISE, operation = "所有系统广告")
    @ApiOperation(value = "所有系统广告")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<SysAdvertise> all = sysAdvertiseService.findAll();
        if (all != null & all.size() > 0)
            return success(all);
        return error("data null");
    }

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/detail")
    //@AccessLog(module = AdminModule.ADVERTISE, operation = "系统广告详情")
    @ApiOperation(value = "系统广告详情")
    @MultiDataSource(name = "second")
    public MessageResult findOne(@RequestParam(value = "serialNumber") String serialNumber) {
        SysAdvertise sysAdvertise = sysAdvertiseService.findOne(serialNumber);
        notNull(sysAdvertise, "validate serialNumber!");
        return success(sysAdvertise);
    }

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "更新轮播")
    @ApiOperation(value = "更新轮播")
    public MessageResult update(@Valid SysAdvertise sysAdvertise, BindingResult bindingResult) {
        notNull(sysAdvertise.getSerialNumber(), "validate serialNumber(null)!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        SysAdvertise one = sysAdvertiseService.findOne(sysAdvertise.getSerialNumber());
        notNull(one, "validate serialNumber!");
        sysAdvertise.setCreateTime(one.getCreateTime());
        sysAdvertiseService.save(sysAdvertise);
        return success();
    }

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "批量删除轮播")
    @ApiOperation(value = "批量删除轮播")
    public MessageResult delete(@RequestParam(value = "ids") String[] ids) {
        sysAdvertiseService.deleteBatch(ids);
        return success();
    }


    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("/page-query")
    //@AccessLog(module = AdminModule.ADVERTISE, operation = "分页查询系统广告")
    @MultiDataSource(name = "second")
    @ApiOperation(value = "分页查询轮播")
    public MessageResult pageQuery(PageModel pageModel, SysAdvertiseScreen screen) {
        Predicate predicate = getPredicate(screen);
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("createTime");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<SysAdvertise> all = sysAdvertiseService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    private Predicate getPredicate(SysAdvertiseScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (screen.getStatus() != null) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.status.eq(screen.getStatus()));
        }
        if (screen.getSysAdvertiseLocation() != null) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.sysAdvertiseLocation.eq(screen.getSysAdvertiseLocation()));
        }
        if (StringUtils.isNotBlank(screen.getSerialNumber())) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.serialNumber.like("%" + screen.getSerialNumber() + "%"));
        }
        if (StringUtils.isNotBlank(screen.getName())) {
            booleanExpressions.add(QSysAdvertise.sysAdvertise.name.like("%" + screen.getName() + "%"));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions("cms:system-advertise:page-query")
    @PostMapping("top")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "置顶")
    public MessageResult toTop(@RequestParam("serialNum") String serialNum) {
        SysAdvertise advertise = sysAdvertiseService.findOne(serialNum);
        int a = sysAdvertiseService.getMaxSort();
        advertise.setSort(a + 1);
        sysAdvertiseService.save(advertise);
        return success();
    }


    @RequiresPermissions("cms:system-advertise:page-query")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "导出系统广告Excel")
    @MultiDataSource(name = "second")
    public MessageResult outExcel(
            @RequestParam(value = "serialNumber", required = false) String serialNumber,
            @RequestParam(value = "sysAdvertiseLocation", required = false) SysAdvertiseLocation sysAdvertiseLocation,
            @RequestParam(value = "status", required = false) CommonStatus status,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Predicate> predicateList = getPredicateList(serialNumber, sysAdvertiseLocation, status);
        List list = sysAdvertiseService.query(predicateList, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "sysAdvertise");
    }

    private List<Predicate> getPredicateList(String serialNumber, SysAdvertiseLocation sysAdvertiseLocation, CommonStatus status) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(serialNumber))
            predicates.add(sysAdvertise.serialNumber.eq(serialNumber));
        if (sysAdvertiseLocation != null)
            predicates.add(sysAdvertise.sysAdvertiseLocation.eq(sysAdvertiseLocation));
        if (status != null)
            predicates.add(sysAdvertise.status.eq(status));
        return predicates;
    }
}
