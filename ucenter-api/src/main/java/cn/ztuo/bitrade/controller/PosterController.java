package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.Locale;
import cn.ztuo.bitrade.entity.Poster;
import cn.ztuo.bitrade.service.PosterService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author dz
 * @date 2020-2-20
 */
@RestController
@RequestMapping("/poster")
@Slf4j
@Api(tags = "海报配置")
public class PosterController extends BaseController {

    private final PosterService posterService;

    public PosterController(PosterService posterService) {
        this.posterService = posterService;
    }

    /**
     * 海报列表
     */
    @ApiOperation(value = "海报列表")
    @RequestMapping(value = "/list", method = {RequestMethod.POST, RequestMethod.GET})
    @MultiDataSource(name = "second")
    public MessageResult list() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if (!Locale.locales.contains(locale)) {
            locale = Locale.ZH_CN;
        }
        List<Poster> list = posterService.findAllByLocale(locale);
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }
}
