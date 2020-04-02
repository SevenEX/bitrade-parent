package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.service.LocalizationService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("locale")
@Api(tags = "国际化配置信息")
public class LocalizationController extends BaseController {
    @Autowired
    private LocalizationService localizationService;

    @GetMapping("all")
    @ApiOperation(value = "国际化配置信息拉取")
    @MultiDataSource(name = "second")
    public MessageResult all(){
        return success(localizationService.getAllMessage());
    }
}
