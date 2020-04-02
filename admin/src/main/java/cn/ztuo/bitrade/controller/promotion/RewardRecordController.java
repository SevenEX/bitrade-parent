package cn.ztuo.bitrade.controller.promotion;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.RewardRecordScreen;
import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.RewardRecord;
import cn.ztuo.bitrade.model.screen.RewardRecordScreen;
import cn.ztuo.bitrade.service.RewardRecordService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.RewardRecordScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("promotion/reward-record")
@Api(tags = "邀请奖励记录")
public class RewardRecordController extends BaseAdminController {

    @Autowired
    private RewardRecordService rewardRecordService ;

    @PostMapping("page-query")
    @RequiresPermissions("promotion:reward-record:page-query")
    @ApiOperation(value = "分页邀请奖励记录")
    @MultiDataSource(name = "second")
    public MessageResult page(PageModel pageModel, RewardRecordScreen screen){
        Predicate predicate = screen.getPredicate();
        Page<RewardRecord> page = rewardRecordService.findAll(predicate, pageModel);
        return success(page);
    }
}
