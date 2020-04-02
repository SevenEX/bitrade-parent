package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.Configuration;
import cn.ztuo.bitrade.constant.ExternalLinks;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.DataDictionary;
import cn.ztuo.bitrade.entity.LocalizationExtend;
import cn.ztuo.bitrade.entity.Navigation;
import cn.ztuo.bitrade.entity.QNavigation;
import cn.ztuo.bitrade.entity.QSysAdvertise;
import cn.ztuo.bitrade.model.update.DataDictionaryUpdate;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.DataDictionaryService;
import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.service.NavigationService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 系统广告
 * @date 2018/1/6 15:03
 */
@Slf4j
@RestController
@RequestMapping("/cms/website")
@Api(tags = "顶部链接/友情链接")
public class NavigationController extends BaseAdminController {
    @Autowired
    private NavigationService navigationService;

    @Autowired
    private LocalizationExtendService localizationExtendService;

    @Autowired
    private DataDictionaryService dataDictionaryService;

    @RequiresPermissions("cms:website:setting:page-query")
    @PostMapping("/create")
   // @AccessLog(module = AdminModule.ADVERTISE, operation = "创建")
    @ApiOperation(value = "创建")
    public MessageResult create(@Valid Navigation navigation, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        navigation.setCreateTime(DateUtil.getCurrentDate());
        Navigation one = navigationService.save(navigation);
        return success(one);
    }


    @RequiresPermissions("cms:website:setting:page-query")
    @PostMapping("/update")
   // @AccessLog(module = AdminModule.ADVERTISE, operation = "更新")
    @ApiOperation(value = "更新")
    public MessageResult update(@Valid Navigation navigation, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        Navigation one = navigationService.findOne(navigation.getId());
        notNull(one, "validate id!");
        navigation.setCreateTime(one.getCreateTime());
        navigationService.save(navigation);
        return success();
    }

    @RequiresPermissions("cms:website:setting:page-query")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.ADVERTISE, operation = "批量删除")
    @ApiOperation(value = "批量删除")
    public MessageResult delete(@RequestParam(value = "ids") Long[] ids) {
        navigationService.deleteBatch(ids);
        return success();
    }


    @RequiresPermissions("cms:website:setting:page-query")
    @PostMapping("/page-query")
    //@AccessLog(module = AdminModule.ADVERTISE, operation = "分页查询系统广告")
    @MultiDataSource(name = "second")
    @ApiOperation(value = "分页查询")
    public MessageResult pageQuery(PageModel pageModel,String type) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(type)) {
            booleanExpressions.add(QNavigation.navigation.type.eq(type));
        }
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("createTime");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        PageResult<Navigation> all = navigationService.queryWhereOrPage(booleanExpressions,pageModel.getPageNo(),pageModel.getPageSize());
        return success(all);
    }

    @PostMapping("detail-locale")
    @ApiOperation(value = "首页链接详情")
    @MultiDataSource(name = "second")
    public MessageResult detail() {
        List<LocalizationExtend> coinInfo = localizationExtendService.getLocaleInfo("ENUM", ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""));;
        return success(coinInfo);
    }

    @PostMapping("update-locale")
    //@AccessLog(module = AdminModule.SYSTEM, operation = "更新首页链接")
    @ApiOperation(value = "更新首页链接")
    public MessageResult updateLocale(@RequestParam("locale")String locale,
                                      @RequestParam("aboutus")String aboutus,
                                      @RequestParam("apply")String apply,
                                      @RequestParam("contactus")String contactus,
                                      @RequestParam("service")String service,
                                      @RequestParam("privacy")String privacy,
                                      @RequestParam("rateExplain")String rateExplain,
                                      @RequestParam("introduce")String introduce) {
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.ABOUTUS.name(), aboutus);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.RATEEXPLAIN.name(), rateExplain);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.APPLY.name(), apply);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.CONTACTUS.name(), contactus);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.SERVICE.name(), service);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.PRIVACY.name(), privacy);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.INTRODUCE.name(), introduce);
        localizationExtendService.updateLocaleInfo("ENUM", locale,
                ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),
                ExternalLinks.ABOUTUS.name(), aboutus);

        return success();
    }

    @GetMapping("/link")
    @ApiOperation(value = "查询联系方式")
    public MessageResult get(@RequestParam("comment") String comment) {
        return success(dataDictionaryService.findByComment(comment));
    }


    @PutMapping("/link/{bond}")
    @ApiOperation(value = "更新联系方式")
    public MessageResult put(@PathVariable("bond") String bond, DataDictionaryUpdate model) {
        DataDictionary dataDictionary = dataDictionaryService.findByBond(bond);
        Assert.notNull(dataDictionary, "validate bond");
        dataDictionaryService.update(model, dataDictionary);
        return success();
    }


    @PutMapping("/update-link")
    @ApiOperation(value = "更新联系方式")
    public MessageResult put(@RequestParam("facebook")String facebook,
                             @RequestParam("twitter")String twitter,
                             @RequestParam("telegram")String telegram,
                             @RequestParam("reddit")String reddit,
                             @RequestParam("wechat")String wechat,
                             @RequestParam("weblog")String weblog,
                             @RequestParam("organ")String organ,
                             @RequestParam("business")String business,
                             @RequestParam("email")String email) {
        dataDictionaryService.updateByBond("facebook", facebook);
        dataDictionaryService.updateByBond("twitter", twitter);
        dataDictionaryService.updateByBond("telegram", telegram);
        dataDictionaryService.updateByBond("reddit", reddit);
        dataDictionaryService.updateByBond("wechat", wechat);
        dataDictionaryService.updateByBond("weblog", weblog);
        dataDictionaryService.updateByBond("organ", organ);
        dataDictionaryService.updateByBond("business", business);
        dataDictionaryService.updateByBond("email", email);

        return success();
    }
}
