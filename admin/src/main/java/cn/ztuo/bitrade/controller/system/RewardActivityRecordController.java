package cn.ztuo.bitrade.controller.system;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.BooleanEnum;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.ActivityRewardType;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.QRewardActivitySetting;
import cn.ztuo.bitrade.entity.RewardActivitySetting;
import cn.ztuo.bitrade.service.RewardActivitySettingService;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.dsl.BooleanOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/system/reward-activity-record")
public class RewardActivityRecordController extends BaseAdminController {

    @Autowired
    private RewardActivitySettingService rewardActivitySettingService;

    @RequiresPermissions("system:reward-activity-record:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.INVITE, operation = "创建修改邀请奖励设置")
    public MessageResult merge(@Valid RewardActivitySetting setting) {
        String info = setting.getInfo();
        JSONObject object = new JSONObject();
        object.put("amount",info);
        setting.setInfo(object.toJSONString());
        setting.setStatus(BooleanEnum.IS_TRUE);
        rewardActivitySettingService.save(setting);
        return MessageResult.success("保存成功");
    }

    /**
     * 查询所有未被禁用的（判断type条件）
     * 默认按照id降序
     *
     * @param type
     * @return
     */
    @RequiresPermissions("system:reward-activity-record:page-query")
    @PostMapping("page-query")
    //@AccessLog(module = AdminModule.INVITE, operation = "分页查询邀请奖励设置")
    public MessageResult pageQuery(PageModel pageModel,
                                   @RequestParam(value = "type", required = false) ActivityRewardType type) {
        BooleanExpression predicate = QRewardActivitySetting.rewardActivitySetting.id.isNotNull();
        if (type != null) {
            predicate = QRewardActivitySetting.rewardActivitySetting.type.eq(type);
        }
        Page<RewardActivitySetting> all = rewardActivitySettingService.findAll(predicate, pageModel);
        return success(all);
    }

    @RequiresPermissions("system:reward-activity-record:deletes")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.INVITE, operation = "批量删除邀请奖励设置")
    public MessageResult deletes(Long[] ids) {
        Assert.notNull(ids, "ids不能为null");
        rewardActivitySettingService.deletes(ids);
        return MessageResult.success("删除成功");
    }


}
