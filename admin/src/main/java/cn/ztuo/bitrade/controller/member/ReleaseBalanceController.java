package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.ReleaseBalance;
import cn.ztuo.bitrade.service.ReleaseBalanceService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.ReleaseBalanceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("release")
@Api(tags = "释放余额")
public class ReleaseBalanceController extends BaseController {

    @Autowired
    private ReleaseBalanceService releaseBalanceService;

    /**
     * 注册送币审核
     *
     * @param releaseBalanceVO
     * @throws Exception
     */
    @RequiresPermissions("member:page-query")
    @AccessLog(module = AdminModule.CMS, operation = "注册审核送币")
    @RequestMapping(value = "updateReleaseBalance", method = RequestMethod.POST)
    @ApiOperation(value = "注册审核送币")
    public MessageResult updateReleaseBalance(@RequestBody ReleaseBalanceVO releaseBalanceVO) {

        log.info("审核送币 updateReleaseBalance = {}", releaseBalanceVO);
        MessageResult messageResult = releaseBalanceService.updateReleaseBalance(releaseBalanceVO);
        return messageResult;

    }

    /**
     * 默认查询  释放余额状态为 未审核0 -
     */
    @RequiresPermissions("member:page-query")
    //@AccessLog(module = AdminModule.CMS, operation = "默认查询  释放余额状态为 未审核 ")
    @RequestMapping(value = "allReleaseBalanceState", method = RequestMethod.POST)
    @ApiOperation(value = "默认查询  释放余额状态为 未审核")
    public MessageResult allReleaseBalanceState(@RequestBody ReleaseBalanceVO releaseBalanceVO) {
        log.info("默认查询  释放余额状态为 0 是 未审核 allReleaseBalanceState ={}", releaseBalanceVO);
        // 查询注册送币审核表 状态为0的数据 就是未审核的
        Page<ReleaseBalance> page = releaseBalanceService.findByReleaseBalanceState(releaseBalanceVO);
//        List<ReleaseBalance> content = page.getContent();
//        return successDataAndTotal(content, page.getTotalElements());
        return success(page);
    }

    /**
     * 条件查询  用户名字  手机号  注册时间  审核状态
     */
   // @AccessLog(module = AdminModule.CMS, operation = "邀请管理条件查询")
    @RequestMapping(value = "conditionQueryAll", method = RequestMethod.POST)
    @ApiOperation(value = "邀请管理条件查询")
    @MultiDataSource(name = "second")
    public MessageResult conditionQueryAll(@RequestBody ReleaseBalanceVO releaseBalanceVO) {
        log.info("条件查询  用户名字  手机号  注册时间 审核状态 conditionQueryAll ={}", releaseBalanceVO);
        // 构建条件查询  根据查询条件查询
        Page<ReleaseBalance> page = releaseBalanceService.conditionQueryAll(releaseBalanceVO);
//        List<ReleaseBalance> content = page.getContent();
//        return successDataAndTotal(content, page.getTotalElements());
        return success(page);
    }
}
