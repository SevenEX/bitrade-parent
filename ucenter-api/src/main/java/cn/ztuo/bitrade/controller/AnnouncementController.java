package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.Locale;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.entity.Announcement;
import cn.ztuo.bitrade.entity.QAnnouncement;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.AnnouncementService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


/**
 * @author MrGao
 * @description
 * @date 2018/3/5 15:25
 */
@RestController
@RequestMapping("announcement")
@Api(tags = "通知公告管理")
public class AnnouncementController extends BaseController {
    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("page")
    @ApiOperation(value = "分页获取通知公告")
    @MultiDataSource(name = "second")
    public MessageResult page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        //条件
        ArrayList<Predicate> predicates = new ArrayList<>();
        predicates.add(QAnnouncement.announcement.isShow.eq(true));
        //排序
        ArrayList<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(QAnnouncement.announcement.isTop.asc());
        orderSpecifiers.add(QAnnouncement.announcement.createTime.desc());
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        predicates.add(QAnnouncement.announcement.locale.eq(locale));
        //查
        PageResult<Announcement> pageResult = announcementService.queryDsl(pageNo, pageSize, predicates, QAnnouncement.announcement, orderSpecifiers);
        return success(pageResult);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "获取通知公告详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@PathVariable("id") Long id) {
        Announcement announcement = announcementService.findById(id);
        Assert.notNull(announcement, "validate id!");
        return success(announcement);
    }


}
