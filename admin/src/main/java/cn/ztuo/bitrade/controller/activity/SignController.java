package cn.ztuo.bitrade.controller.activity;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.create.SignCreate;
import cn.ztuo.bitrade.model.screen.SignScreen;
import cn.ztuo.bitrade.model.vo.SignVO;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SignStatus;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Sign;
import cn.ztuo.bitrade.model.create.SignCreate;
import cn.ztuo.bitrade.model.screen.SignScreen;
import cn.ztuo.bitrade.model.update.SignUpdate;
import cn.ztuo.bitrade.model.vo.SignVO;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.SignService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.create.SignCreate;
import cn.ztuo.bitrade.model.screen.SignScreen;
import cn.ztuo.bitrade.model.vo.SignVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/311:22
 */
@RestController
@RequestMapping("activity/sign")
@Api(tags = "活动签到（暂弃）")
public class SignController extends BaseAdminController {
    @Autowired
    private SignService service;
    @Autowired
    private CoinService coinService;

    //创建
    @RequiresPermissions("activity:sign:post")
    @PostMapping
    @ApiOperation(value = "签到")
    public MessageResult create(@Valid SignCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) return result;
        service.save(model.transformation(service, coinService));
        return success();
    }

    //更新
    @RequiresPermissions("activity:sign:put")
    @PutMapping("{id}")
    @ApiOperation(value = "更新")
    public MessageResult update(@Valid SignUpdate model, BindingResult bindingResult, @PathVariable("id") Long id) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) return result;
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        //转化
        sign = model.transformation(coinService, sign);
        //保存
        service.save(sign);
        return success();
    }

    //分页
    @RequiresPermissions("activity:sign:page-query")
    @GetMapping("page-query")
    @ApiOperation(value = "分页查询记录")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(SignScreen screen, PageModel pageModel) {
        Page<Sign> source = service.findAllScreen(screen, pageModel);
        Page<SignVO> page = source.map(x -> SignVO.getSignVO(x));
        return success(page);
    }

    //详情
    @RequiresPermissions("activity:sign:id:get")
    @GetMapping("{id}")
    @ApiOperation(value = "详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@PathVariable("id") Long id) {
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        SignVO signVO = SignVO.getSignVO(sign);
        return success(signVO);
    }

    // 提前关闭
    @RequiresPermissions("activity:sign:id:early-closing")
    @PatchMapping("{id}/early-closing")
    @ApiOperation(value = "提前关闭")
    public MessageResult earlyClosing(@PathVariable("id") Long id) {
        //校验id
        Sign sign = service.findById(id);
        Assert.notNull(sign, "validate id!");
        Assert.isTrue(sign.getStatus() == SignStatus.UNDERWAY, "validate status!");
        service.earlyClosing(sign);
        return success();
    }

    //判断是否存在惊醒中
    @GetMapping("has-underway")
    @ApiOperation(value = "判断签到活动状态是否存在")
    @MultiDataSource(name = "second")
    public MessageResult is() {
        Sign sign = service.fetchUnderway();
        if (sign != null) return success(true);
        else return success(false);
    }

}