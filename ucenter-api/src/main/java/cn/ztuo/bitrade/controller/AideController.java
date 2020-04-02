package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.constant.Locale;
import cn.ztuo.bitrade.entity.*;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Seven
 * @date 2019年02月05日
 */
@RestController
@RequestMapping("/ancillary")
@Slf4j
@Api(tags = "系统管理")
public class AideController extends BaseController {
    @Autowired
    private WebsiteInformationService websiteInformationService;

    @Autowired
    private SysAdvertiseService sysAdvertiseService;

    @Autowired
    private SysHelpService sysHelpService;
    @Autowired
    private AppRevisionService appRevisionService;

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private RedisUtil redisUtil ;

    @Autowired
    private LocalizationExtendService localizationExtendService;

    @Autowired
    private NavigationService navigationService;


    @Autowired
    private DataDictionaryService dataDictionaryService;

    /**
     * 站点信息
     *
     * @return
     */
    @ApiOperation(value = "站点信息")
    @RequestMapping(value = "/website/info", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult keyWords() {
        WebsiteInformation websiteInformation = websiteInformationService.fetchOne();
        MessageResult result = MessageResult.success();
        result.setData(websiteInformation);
        return result;
    }

    /**
     * 系统广告
     *
     * @return
     */
    @ApiOperation(value = "获取系统广告")
    @RequestMapping(value = "/system/advertise", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult sysAdvertise(@RequestParam(value = "sysAdvertiseLocation", required = false) SysAdvertiseLocation sysAdvertiseLocation) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        List<SysAdvertise> list = sysAdvertiseService.findAllNormal(sysAdvertiseLocation,locale);
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }


    /**
     * 系统帮助
     *
     * @return
     */
    @ApiOperation(value = "系统帮助")
    @RequestMapping(value = "/system/help", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult sysHelp(@RequestParam(value = "sysHelpClassification", required = false) SysHelpClassification sysHelpClassification) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        List<SysHelp> list = null;
        if (sysHelpClassification == null) {
            list = sysHelpService.findAllByStatusNotAndSortAndLocale(locale);
        } else {
            list = sysHelpService.findBySysHelpClassification(sysHelpClassification,locale);
        }
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }

    /**
     * 系统帮助详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "系统帮助详情")
    @RequestMapping(value = "/system/help/{id}", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult sysHelp(@PathVariable(value = "id") long id) {
        //List<SysHelp> list = sysHelpService.findBySysHelpClassification(sysHelpClassification);
        SysHelp sysHelp = sysHelpService.findOne(id);
        MessageResult result = MessageResult.success();
        result.setData(sysHelp);
        return result;
    }

    /**
     * 移动版本号
     *
     * @param platform 0:安卓 1:苹果
     * @return
     */
    @ApiOperation(value = "移动版本号")
    @RequestMapping(value = "/system/app/version/{id}", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult sysHelp(@PathVariable(value = "id") Platform platform) {

        AppRevision revision = appRevisionService.findRecentVersion(platform);
        if(revision != null){
            MessageResult result = MessageResult.success();
            result.setData(revision);
            return result;
        }
        else{
            return MessageResult.error(msService.getMessage("NO_UPDATE"));
        }
    }

    /**
     * 查询帮助中心首页数据
     * @param total
     * @return
     */
    @ApiOperation(value = "查询帮助中心首页数据")
    @RequestMapping(value = "more/help",method = RequestMethod.POST)
    @MultiDataSource(name = "second")
    public MessageResult sysAllHelp(@RequestParam(value = "total",defaultValue = "6")int total){
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        List<JSONObject> result = (List<JSONObject>) redisUtil.get(SysConstant.SYS_HELP.concat(locale));
        if (result != null){
            return success(result);
        } else {
            //HELP("新手指南"),
            List<JSONObject> jsonResult = new ArrayList<>();
            Page<SysHelp> sysHelpPage = sysHelpService.findByCondition(1,total,SysHelpClassification.HELP,locale);
            JSONObject jsonSysHelp = new JSONObject();
            jsonSysHelp.put("content",sysHelpPage.getContent());
            jsonSysHelp.put("title","新手指南");
            jsonSysHelp.put("cate","0");
            jsonResult.add(jsonSysHelp);

            //FAQ("常见问题"),
            Page<SysHelp> sysFaqPage = sysHelpService.findByCondition(1,total,SysHelpClassification.FAQ,locale);
            JSONObject jsonSysFaq = new JSONObject();
            jsonSysFaq.put("content",sysFaqPage.getContent());
            jsonSysFaq.put("title","常见问题");
            jsonSysFaq.put("cate","1");
            jsonResult.add(jsonSysFaq);
            redisUtil.set(SysConstant.SYS_HELP.concat(locale),jsonResult,SysConstant.SYS_HELP_EXPIRE_TIME, TimeUnit.SECONDS);
            return success(jsonResult);
        }
        //RECHARGE("充值指南"),
        //Page<SysHelp> sysRechangePage = sysHelpService.findByCondition(1,total,SysHelpClassification.HELP);

        //TRANSACTION("交易指南"),
        //Page<SysHelp> sysTransactonPage = sysHelpService.findByCondition(1,total,SysHelpClassification.HELP);
    }

    /**
     * 获取该分类（二级页面）
     * @param pageNo
     * @param pageSize
     * @param cate
     * @return
     */
    @ApiOperation(value = "获取该分类")
    @RequestMapping(value = "more/help/page",method = RequestMethod.POST)
    @MultiDataSource(name = "second")
    public MessageResult sysHelpCate(@RequestParam(value = "pageNo",defaultValue = "1")int pageNo,
                                     @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                     @RequestParam(value = "cate")SysHelpClassification cate){
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        JSONObject result = (JSONObject) redisUtil.get(SysConstant.SYS_HELP_CATE.concat(locale)+cate);
        if (result != null){
            return success(result);
        }else {
            JSONObject jsonObject = new JSONObject();
            Page<SysHelp> sysHelpPage = sysHelpService.findByCondition(pageNo,pageSize,cate,locale);
            jsonObject.put("content",sysHelpPage.getContent());
            jsonObject.put("totalPage",sysHelpPage.getTotalPages());
            jsonObject.put("totalElements",sysHelpPage.getTotalElements());
            redisUtil.set(SysConstant.SYS_HELP_CATE.concat(locale)+cate,jsonObject,SysConstant.SYS_HELP_CATE_EXPIRE_TIME, TimeUnit.SECONDS);
            return success(jsonObject);

        }

    }

    /**
     * 获取该分类的置顶文章
     * @param cate
     * @return
     */
    @ApiOperation(value = "获取该分类的置顶帮助")
    @RequestMapping(value = "more/help/page/top", method = RequestMethod.POST)
    @MultiDataSource(name = "second")
    public MessageResult sysHelpTop(@RequestParam(value = "cate")String cate){
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        List<SysHelp> result = (List<SysHelp>) redisUtil.get(SysConstant.SYS_HELP_TOP.concat(locale)+cate);
        if ( result != null && !result.isEmpty()){
            return success(result);
        }else {
            List<SysHelp> sysHelps = sysHelpService.getgetCateTops(cate,locale);
            redisUtil.set(SysConstant.SYS_HELP_TOP.concat(locale)+cate,sysHelps,SysConstant.SYS_HELP_TOP_EXPIRE_TIME,TimeUnit.SECONDS);
            return success(sysHelps);
        }
    }

    /**
     * 系统帮助详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "系统帮助详情")
    @RequestMapping(value = "more/help/detail",method = RequestMethod.POST)
    @MultiDataSource(name = "second")
    public MessageResult sysHelpDetail(@RequestParam(value = "id") long id) {
        SysHelp result = (SysHelp) redisUtil.get(SysConstant.SYS_HELP_DETAIL+id);
        if (result != null){
            return success(result);
        }else {
            SysHelp sysHelp = sysHelpService.findOne(id);
            redisUtil.set(SysConstant.SYS_HELP_DETAIL+id,sysHelp,SysConstant.SYS_HELP_DETAIL_EXPIRE_TIME,TimeUnit.SECONDS);
            return success(sysHelp);
        }

    }


    @ApiOperation(value = "新手教程/帮助中心/隐私政策/广告服务协议")
    @RequestMapping(value = "/info", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult help() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        Map map = new HashMap();
        map.put("tutorial",localizationExtendService.getLocaleInfo("ENUM",locale,Configuration.class.getName().replace("cn.ztuo.bitrade.",""),Configuration.TUTORIAL.name()));
        map.put("help",localizationExtendService.getLocaleInfo("ENUM",locale,Configuration.class.getName().replace("cn.ztuo.bitrade.",""),Configuration.HELP.name()));
        map.put("privacy",localizationExtendService.getLocaleInfo("ENUM",locale,Configuration.class.getName().replace("cn.ztuo.bitrade.",""),Configuration.PRIVACY.name()));
        map.put("advertis",localizationExtendService.getLocaleInfo("ENUM",locale,Configuration.class.getName().replace("cn.ztuo.bitrade.",""),Configuration.ADVERTIS.name()));
        MessageResult result = MessageResult.success();
        result.setData(map);
        return result;
    }


    @ApiOperation(value = "固定位置外链")
    @RequestMapping(value = "/externalLinks/info", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult externalLinks() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        JSONObject jsonObject = (JSONObject) redisUtil.get(SysConstant.EXTERNAL_LINKS+locale);
        if (jsonObject != null){
            return success(jsonObject);
        }else {
            Map map = new HashMap();
            jsonObject = new JSONObject();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.ORGAN_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.ORGAN.name()));
            jsonObject.put("organ", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.BUSINESS_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.BUSINESS.name()));
            jsonObject.put("business", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.CUSTOMER_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.CUSTOMER.name()));
            jsonObject.put("customer", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.APPLY_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.APPLY.name()));
            jsonObject.put("apply", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.ABOUTUS_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.ABOUTUS.name()));
            jsonObject.put("aboutus", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.SERVICE_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.SERVICE.name()));
            jsonObject.put("service", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.PRIVACY_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.PRIVACY.name()));
            jsonObject.put("privacy", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.RATEEXPLAIN_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.RATEEXPLAIN.name()));
            jsonObject.put("rateExplain", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.CONTACTUS_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.CONTACTUS.name()));
            jsonObject.put("contactus", map);
            map = new HashMap();
            map.put("name",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.INTRODUCE_NAME.name()));
            map.put("url",localizationExtendService.getLocaleInfo("ENUM",locale,ExternalLinks.class.getName().replace("cn.ztuo.bitrade.",""),ExternalLinks.INTRODUCE.name()));
            jsonObject.put("introduce", map);
            AppRevision revision = appRevisionService.findRecentVersion(Platform.ANDROID);
            if(revision != null) {
                jsonObject.put("android", revision.getDownloadUrl());
            }
            revision = appRevisionService.findRecentVersion(Platform.IOS);
            if(revision != null) {
                jsonObject.put("ios", revision.getDownloadUrl());
            }
            redisUtil.set(SysConstant.EXTERNAL_LINKS+locale,jsonObject,SysConstant.EXTERNAL_LINKS_EXPIRE_TIME,TimeUnit.SECONDS);
            return success(jsonObject);
        }
    }

    @ApiOperation(value = "非固定位置外链")
    @RequestMapping(value = "/getNavigation", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult getNavigation() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        Map map = (Map) redisUtil.get(SysConstant.NAVIGATION+locale);
        if (map != null){
            return success(map);
        }else {
            map = new HashMap();
            map.put("top", navigationService.findAllByStatusNotAndTypeAndLocale("0", locale));
            map.put("bottom", navigationService.findAllByStatusNotAndTypeAndLocale("1", locale));
            redisUtil.set(SysConstant.NAVIGATION+locale,map,SysConstant.NAVIGATION_EXPIRE_TIME,TimeUnit.SECONDS);
            return success(map);
        }
    }

    @ApiOperation(value = "获取说明")
    @RequestMapping(value = "/getMassage", method = {RequestMethod.POST,RequestMethod.GET})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "areaName", value = "TUTORIAL(\"新手教程\"),TRADINGRULES(\"交易规则\"), HELP(\"帮助中心\"), PRIVACY(\"隐私政策\"),\n" +
                    "    ADVERTIS(\"广告服务协议\"), INVITATION(\"邀请返佣奖励细则\"),\n" +
                    "    SE_WEB(\"SE抵扣说明(WEB端)\"), SE_MOBILE(\"SE抵扣说明(移动端)\"), GRADE(\"等级费率说明\")", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult getMassage(String areaName) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        MessageResult result = MessageResult.success();
        String massage = localizationExtendService.getLocaleInfo("ENUM",locale,Configuration.class.getName().replace("cn.ztuo.bitrade.",""),areaName);
        result.setData(massage==null?"":massage);
        return result;
    }

    @ApiOperation(value = "获取联系方式")
    @RequestMapping(value = "/getContact", method = {RequestMethod.POST,RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult get() {
        return success(dataDictionaryService.findByComment("contact"));
    }
}
