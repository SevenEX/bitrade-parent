package cn.ztuo.bitrade.controller.member;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.IntegrationRecord;
import cn.ztuo.bitrade.service.IntegrationRecordService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.IntegrationRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @description: IntegrationRecordController
 * @author: MrGao
 * @create: 2019/04/25 19:24
 */
@Slf4j
@RestController
@RequestMapping("integration/record")
@Api(tags = "会员积分记录")
public class IntegrationRecordController extends BaseController {

    @Autowired
    private IntegrationRecordService recordService ;

    //@AccessLog(module = AdminModule.MEMBER, operation = "会员积分记录查询")
    @RequiresPermissions("member:integration_query_page:all")
    @RequestMapping(value = "query_page",method = RequestMethod.POST)
    @ApiOperation(value = "会员积分记录查询")
    @MultiDataSource(name = "second")
    public MessageResult findAllIntegrationPage(@RequestBody IntegrationRecordVO queryVo){
        Page<IntegrationRecord> page =  recordService.findRecord4Page(queryVo);
        return success(page);
    }


}
