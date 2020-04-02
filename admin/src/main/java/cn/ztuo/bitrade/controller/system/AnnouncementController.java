package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.Announcement;
import cn.ztuo.bitrade.entity.QAnnouncement;
import cn.ztuo.bitrade.service.AnnouncementService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author MrGao
 * @description 公告
 * @date 2018/3/5 15:25
 */
@RestController
@RequestMapping("system/announcement")
@Api(tags = "通知公告")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("cms:notice")
    @PostMapping("create")
    @ApiOperation(value = "创建通知公告")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "创建通知公告")
    public MessageResult create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String locale,
            @RequestParam("isShow") Boolean isShow,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setIsShow(isShow);
        announcement.setImgUrl(imgUrl);
        announcement.setLocale(locale);
        announcementService.save(announcement);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("cms:notice")
    @PostMapping("top")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "公告置顶")
    @ApiOperation(value = "公告置顶")
    public MessageResult toTop(@RequestParam("id")long id){
        Announcement announcement = announcementService.findById(id);
        //int a = announcementService.getMaxSort();
        //announcement.setSort(a+1);
        announcement.setIsTop("0");
        announcementService.save(announcement);
        return success(messageSource.getMessage("SUCCESS"));
    }


    /**
     * 取消公告置顶
     * @param id
     * @return
     */
    @RequiresPermissions("cms:notice")
    @PostMapping("down")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "公告取消置顶")
    @ApiOperation(value = "公告取消置顶")
    public MessageResult toDown(@RequestParam("id")long id){
        Announcement announcement = announcementService.findById(id);
        announcement.setIsTop("1");
        announcementService.save(announcement);
        return success();
    }

    @RequiresPermissions("cms:notice")
    @GetMapping("page-query")
    @ApiOperation(value = "分页查询通知公告")
    //@AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "分页查询通知公告")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            @RequestParam(required = false) Boolean isShow,
            @RequestParam(required = false) String title) {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (isShow != null) {
            booleanExpressions.add(QAnnouncement.announcement.isShow.eq(isShow));
        }
        if (title != null) {
            booleanExpressions.add(QAnnouncement.announcement.title.like("%"+title+"%"));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<Sort.Direction> directions = new ArrayList<>();
        List<String> sorts = Arrays.asList("createTime");
        directions.add(Sort.Direction.DESC);
        pageModel.setProperty(sorts);
        pageModel.setDirection(directions);
        Page<Announcement> all = announcementService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    @RequiresPermissions("cms:notice")
    @PatchMapping("deletes")
    @ApiOperation(value = "删除通知公告")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "删除通知公告")
    public MessageResult deleteOne(@RequestParam Long[] ids) {
        announcementService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("cms:notice")
    @GetMapping("{id}/detail")
    @ApiOperation(value = "查询通知公告详情")
    @MultiDataSource(name = "second")
   // @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "查询通知公告详情")
    public MessageResult detail(
            @PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        return success(announcement);
    }


    @RequiresPermissions("cms:notice")
    @PutMapping("{id}/update")
    @ApiOperation(value = "更新通知公告")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "更新通知公告")
    public MessageResult update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String locale,
            @RequestParam String isTop,
            @RequestParam Boolean isShow,
            @RequestParam(value = "imgUrl", required = false) String imgUrl) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setIsShow(isShow);
        announcement.setImgUrl(imgUrl);
        announcement.setLocale(locale);
        announcement.setIsTop(isTop);
        announcementService.save(announcement);
        return success();
    }

    @RequiresPermissions("cms:notice")
    @PatchMapping("{id}/turn-off")
    @ApiOperation(value = "设置不显示")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "设置不显示")
    public MessageResult turnOff(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        announcement.setIsShow(false);
        announcementService.save(announcement);
        return success();
    }

    @RequiresPermissions("cms:notice")
    @PatchMapping("{id}/turn-on")
    @ApiOperation(value = "设置显示")
    @AccessLog(module = AdminModule.ANNOUNCEMENT, operation = "设置显示")
    public MessageResult turnOn(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        announcement.setIsShow(true);
        announcementService.save(announcement);
        return success();
    }

}
