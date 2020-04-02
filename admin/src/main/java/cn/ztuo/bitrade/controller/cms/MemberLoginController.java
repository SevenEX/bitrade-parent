package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.ExternalLinks;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.model.update.DataDictionaryUpdate;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 系统广告
 * @date 2018/1/6 15:03
 */
@Slf4j
@RestController
@RequestMapping("/memberLogin")
@Api(tags = "登录日志/安全日志")
public class MemberLoginController extends BaseAdminController {
    @Autowired
    private MemberLoginRecordService memberLoginRecordService;

    @Autowired
    private MemberVerifyRecordService memberVerifyRecordService;

    @PostMapping("/loginRecord")
    @MultiDataSource(name = "second")
    @ApiOperation(value = "分页查询")
    public MessageResult pageQuery(PageModel pageModel,Long memberId) {
        PageResult<MemberLoginRecord> result = memberLoginRecordService.query(memberId,pageModel.getPageNo(),pageModel.getPageSize());
        return success(result);
    }

    @RequestMapping(value = "/verifyRecord",method = RequestMethod.POST)
    @ApiOperation(value = "安全记录")
    public MessageResult getVerifyRecord(PageModel pageModel,Long memberId) {
        PageResult<MemberVerifyRecord> result = memberVerifyRecordService.query(memberId,pageModel.getPageNo(),pageModel.getPageSize());
        return success(result);
    }

}
