package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.entity.Feedback;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.FeedbackService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author Seven
 * @date 2019年03月19日
 */
@RestController
@Slf4j
@Api(tags = "反馈")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     * 提交反馈意见
     *
     * @param user
     * @param remark
     * @return
     */
    @RequestMapping(value = "feedback",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "提交反馈意见")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult feedback(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String remark) {
        Feedback feedback = new Feedback();
        Member member = memberService.findOne(user.getId());
        feedback.setMember(member);
        feedback.setRemark(remark);
        Feedback feedback1 = feedbackService.save(feedback);
        if (feedback1 != null) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("SYSTEM_ERROR"));
        }
    }
}
