package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.Configuration;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.dto.CoinInfo;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LocalizationExtend;
import cn.ztuo.bitrade.entity.SysHelp;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.service.SysHelpService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 后台帮助web
 * @date 2018/1/9 10:11
 */
@RestController
@RequestMapping("/cms/system-help")
@Api(tags = "帮助中心")
public class HelpController extends BaseAdminController {

    @Autowired
    private SysHelpService sysHelpService;

    @Autowired
    private LocalizationExtendService localizationExtendService;

    @RequiresPermissions("cms:system-help:create")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.HELP, operation = "创建系统帮助")
    @ApiOperation(value = "创建系统帮助")
    public MessageResult create(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        sysHelp.setCreateTime(DateUtil.getCurrentDate());
        sysHelp = sysHelpService.save(sysHelp);
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:all")
    @PostMapping("/all")
  //  @AccessLog(module = AdminModule.HELP, operation = "查找所有系统帮助")
    @ApiOperation(value = "查找所有系统帮助")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<SysHelp> sysHelps = sysHelpService.findAll();
        if (sysHelps != null && sysHelps.size() > 0)
            return success(sysHelps);
        return error("data null");
    }

    @RequiresPermissions("cms:system-help:top")
    @PostMapping("top")
    @AccessLog(module = AdminModule.HELP, operation = "系统帮助置顶")
    @ApiOperation(value = "系统帮助置顶")
    public MessageResult toTop(@RequestParam("id")long id){
        SysHelp help = sysHelpService.findOne(id);
        int a = sysHelpService.getMaxSort();
        help.setSort(a+1);
        help.setIsTop("0");
        sysHelpService.save(help);
        return success("置顶成功！");
    }

    /**
     * 系统帮助取消置顶
     * @param id
     * @return
     */
    @RequiresPermissions("cms:system-help:down")
    @PostMapping("down")
    @AccessLog(module = AdminModule.HELP, operation = "系统帮助取消置顶")
    @ApiOperation(value = "系统帮助取消置顶")
    public MessageResult toDown(@RequestParam("id")long id){
        SysHelp help = sysHelpService.findOne(id);
        help.setIsTop("1");
        sysHelpService.save(help);
        return success();
    }


    @RequiresPermissions("cms:system-help:detail")
    @PostMapping("/detail")
   // @AccessLog(module = AdminModule.HELP, operation = "系统帮助详情")
    @ApiOperation(value = "系统帮助详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        SysHelp sysHelp = sysHelpService.findOne(id);
        notNull(sysHelp, "validate id!");
        return success(sysHelp);
    }

    @RequiresPermissions("cms:system-help:update")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.HELP, operation = "更新系统帮助")
    @ApiOperation(value = "更新系统帮助")
    public MessageResult update(@Valid SysHelp sysHelp, BindingResult bindingResult) {
        notNull(sysHelp.getId(), "validate id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        SysHelp one = sysHelpService.findOne(sysHelp.getId());
        notNull(one, "validate id!");
        sysHelpService.save(sysHelp);
        return success();
    }

    @RequiresPermissions("cms:system-help:deletes")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.HELP, operation = "删除系统帮助")
    @ApiOperation(value = "删除系统帮助")
    public MessageResult deleteOne(@RequestParam("ids") Long[] ids) {
        sysHelpService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("cms:system-help:page-query")
    @PostMapping("/page-query")
    //@AccessLog(module = AdminModule.HELP, operation = "分页查询系统帮助")
    @ApiOperation(value = "分页查询系统帮助")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(PageModel pageModel) {
        List<Sort.Direction> directions = new ArrayList<>();
        List<String> sorts = Arrays.asList("sort","isTop");
        directions.add(Sort.Direction.DESC);
        directions.add(Sort.Direction.ASC);
        pageModel.setProperty(sorts);
        pageModel.setDirection(directions);
        Page<SysHelp> all = sysHelpService.findAll(new BooleanBuilder(), pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("cms:system-help:out-excel")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.HELP, operation = "导出系统帮助Excel")
    @ApiOperation(value = "导出系统帮助Excel")
    @MultiDataSource(name = "second")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = sysHelpService.findAll();
        return new FileUtil().exportExcel(request, response, all, "sysHelp");
    }

    @PostMapping("update-locale")
    @AccessLog(module = AdminModule.SYSTEM, operation = "更新基础配置")
    @ApiOperation(value = "更新基础配置")
    public MessageResult updateLocale(@RequestParam("locale")String locale,
                                      @RequestParam("columnName")String columnName,
                                      @RequestParam("content")String content) {
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                Configuration.class.getName().replace("cn.ztuo.bitrade.",""),
                columnName, content);
        return success();
    }

    @PostMapping("detail-locale")
    @ApiOperation(value = "基础配置详情")
    @MultiDataSource(name = "second")
    public MessageResult detail() {
        List<LocalizationExtend> coinInfo = localizationExtendService.getLocaleInfo("ENUM",Configuration.class.getName().replace("cn.ztuo.bitrade.",""));;
        return success(coinInfo);
    }
}
