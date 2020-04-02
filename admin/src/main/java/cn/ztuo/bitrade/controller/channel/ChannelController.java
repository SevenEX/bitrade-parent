package cn.ztuo.bitrade.controller.channel;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.ChannelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("channel")
@Slf4j
@Api(tags = "渠道管理")
public class ChannelController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private LocaleMessageSourceService msService;

    @RequiresPermissions("channel:list")
    @PostMapping("list")
    //@AccessLog(module = AdminModule.CHANNEL, operation = "查询当前渠道列表")
    @ApiOperation(value = "查询当前渠道列表")
    @MultiDataSource(name = "second")
    public MessageResult listChannel(PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QMember member=QMember.member;
        booleanExpressions.add(member.isChannel.eq(BooleanEnum.IS_TRUE));
        Predicate predicate= PredicateUtils.getPredicate(booleanExpressions);
        Page<Member> channelPage= memberService.findAll(predicate,pageModel);
        List<Member> channelList=channelPage.getContent();
        if(channelList!=null&&channelList.size()>0){
            List<Long> memberIds=new ArrayList<>();
            for(Member m:channelList){
                memberIds.add(m.getId());
                m.setChannelVO(new ChannelVO());
            }
            List<ChannelVO> channelVOList=memberService.getChannelCount(memberIds);
            if(channelVOList!=null&&channelVOList.size()>0){
                for(Member m:channelList){
                    for(ChannelVO vo:channelVOList){
                        if(vo.getMemberId().equals(m.getId())){
                            m.setChannelVO(vo);
                        }
                    }
                }
            }
        }
        MessageResult result=MessageResult.success();
        result.setData(channelPage);
        return result;
    }

    @RequiresPermissions("channel:set")
    @PostMapping("setUp")
    @AccessLog(module = AdminModule.CHANNEL, operation = "设置渠道")
    @ApiOperation(value = "设置渠道")
    public MessageResult setChannel(@Param("mobilePhone")String mobilePhone){
        if(mobilePhone==null||mobilePhone.trim().equalsIgnoreCase("")){
            return MessageResult.error(msService.getMessage("Incorrect_Parameters"));
        }
        Member member=memberService.findByPhone(mobilePhone);
        if(member==null){
            return MessageResult.error(msService.getMessage("UNBOUND_PHONE"));
        }
        if(member.getIsChannel().equals(BooleanEnum.IS_TRUE)){
            return MessageResult.error(msService.getMessage("ALREADY_CHANNEL"));
        }
        member.setIsChannel(BooleanEnum.IS_TRUE);
        memberService.save(member);
        return MessageResult.success();
    }

    @RequiresPermissions("channel:revoke")
    @PostMapping("revoke")
    @AccessLog(module = AdminModule.CHANNEL, operation = "撤销渠道")
    @ApiOperation(value = "撤销渠道")
    public MessageResult updateChannel(@Param("memberId")Long memberId){
        if(memberId==null){
            return MessageResult.error(msService.getMessage("Incorrect_Parameters"));
        }
        Member member=memberService.findOne(memberId);
        if(member==null){
            return MessageResult.error(msService.getMessage("ID_IS_NOT_FOUND"));
        }
        if(member.getIsChannel().equals(BooleanEnum.IS_FALSE)){
            return MessageResult.error(msService.getMessage("ALREADY_REVOKE"));
        }
        member.setIsChannel(BooleanEnum.IS_FALSE);
        memberService.save(member);
        return MessageResult.success();
    }

    @RequiresPermissions("channel:list")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.CHANNEL, operation = "查询当前渠道列表")
    @ApiOperation(value = "查询当前渠道列表")
    @MultiDataSource(name = "second")
    public MessageResult channelRewardDetail(@Param("memberId")Long memberId,PageModel pageModel){
        Page<MemberTransaction> memberTransactionPage=memberTransactionService.queryByMember(memberId,
                pageModel.getPageNo()-1,pageModel.getPageSize(),TransactionType.CHANNEL_AWARD,null);
        MessageResult result=MessageResult.success();
        result.setData(memberTransactionPage);
        return result;
    }
}
