package cn.ztuo.bitrade.controller.finance;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.QMemberTransaction;
import cn.ztuo.bitrade.model.screen.MemberTransactionScreen;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.MemberTransactionVO;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 交易记录
 * @date 2018/1/17 17:07
 */
@RestController
@RequestMapping("/finance/member-transaction")
@Api(tags = "交易记录")
public class MemberTransactionController extends BaseAdminController {
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @RequiresPermissions("finance:member-transaction:page-query")
    @PostMapping("/all")
    //@AccessLog(module = AdminModule.FINANCE, operation = "所有交易记录MemberTransaction")
    @ApiOperation(value = "所有交易记录")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<MemberTransaction> memberTransactionList = memberTransactionService.findAll();
        if (memberTransactionList != null && memberTransactionList.size() > 0)
            return success(memberTransactionList);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("finance:member-transaction:page-query")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.FINANCE, operation = "交易记录MemberTransaction 详情")
    @ApiOperation(value = "交易记录详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        MemberTransaction memberTransaction = memberTransactionService.findOne(id);
        notNull(memberTransaction, "validate id!");
        return success(memberTransaction);
    }

    @RequiresPermissions(value = {"finance:member-transaction:page-query", "finance:member-transaction:page-query:recharge",
            "finance:member-transaction:page-query:check", "finance:member-transaction:page-query:fee"}, logical = Logical.OR)
    @PostMapping("page-query")
    //@AccessLog(module = AdminModule.FINANCE, operation = "分页查找交易记录MemberTransaction")
    @ApiOperation(value = "分页查找交易记录")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(
            PageModel pageModel,
            MemberTransactionScreen screen) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(QMemberTransaction.memberTransaction.memberId.eq(QMember.member.id));
        if(screen.getMemberId()!=null)
            predicates.add((QMember.member.id.eq(screen.getMemberId())));
        if (!StringUtils.isEmpty(screen.getAccount()))
            predicates.add(QMember.member.username.like("%"+screen.getAccount()+"%")
                        .or(QMember.member.realName.like("%"+screen.getAccount()+"%")));
        if (screen.getStartTime() != null)
            predicates.add(QMemberTransaction.memberTransaction.createTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null){
            predicates.add(QMemberTransaction.memberTransaction.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        if (screen.getType() != null)
            predicates.add(QMemberTransaction.memberTransaction.type.eq(screen.getType()));

        if(screen.getMinMoney()!=null)
            predicates.add(QMemberTransaction.memberTransaction.amount.goe(screen.getMinMoney()));

        if(screen.getMaxMoney()!=null)
            predicates.add(QMemberTransaction.memberTransaction.amount.loe(screen.getMaxMoney()));

        if(screen.getMinFee()!=null)
            predicates.add(QMemberTransaction.memberTransaction.fee.goe(screen.getMinFee()));

        if(screen.getMaxFee()!=null)
            predicates.add(QMemberTransaction.memberTransaction.fee.loe(screen.getMaxFee()));

        if(screen.getSymbol()!=null&&!screen.getSymbol().equalsIgnoreCase("")){
            predicates.add(QMemberTransaction.memberTransaction.symbol.eq(screen.getSymbol()));
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            predicates.add(QMember.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(QMember.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(QMember.member.email.like("%" + screen.getKeyWords() + "%")));
        }else if(!StringUtils.isEmpty(screen.getKeyWords())){
            predicates.add(QMember.member.email.like("%" + screen.getKeyWords() + "%"));
        }
        Page<MemberTransactionVO> results = memberTransactionService.joinFind(predicates, pageModel);

        return success(results);
    }

    @RequiresPermissions(value = {"finance:member-transaction:page-query", "finance:member-transaction:page-query:recharge",
            "finance:member-transaction:page-query:check", "finance:member-transaction:page-query:fee"}, logical = Logical.OR)
    @PostMapping("transaction/page-query")
    //@AccessLog(module = AdminModule.FINANCE, operation = "分页查找交易记录MemberTransaction")
    @ApiOperation(value = "分页查找划转记录")
    @MultiDataSource(name = "second")
    public MessageResult transactionPageQuery(
            PageModel pageModel,
            MemberTransactionScreen screen) {
        List<Predicate> predicates = new ArrayList<>();
        if (screen.getStartTime() != null)
            predicates.add(QMemberTransaction.memberTransaction.createTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null){
            predicates.add(QMemberTransaction.memberTransaction.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        predicates.add(QMemberTransaction.memberTransaction.type.in(TransactionType.TRANSFER_ACCOUNTS,TransactionType.COIN_TWO_OTC,TransactionType.OTC_TWO_COIN));

        if(screen.getSymbol()!=null&&!screen.getSymbol().equalsIgnoreCase("")){
            predicates.add(QMemberTransaction.memberTransaction.symbol.eq(screen.getSymbol()));
        }
        if(!StringUtils.isEmpty(screen.getKeyWords())){
            if(screen.getMemberId()!=null)
                predicates.add((QMember.member.id.eq(screen.getMemberId())));
            Pattern pattern = Pattern.compile("[0-9]*");
            if (pattern.matcher(screen.getKeyWords()).matches()) {
                predicates.add(QMember.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                        .or(QMember.member.id.eq(Long.valueOf(screen.getKeyWords())))
                        .or(QMember.member.email.like("%" + screen.getKeyWords() + "%")));
            }else{
                predicates.add(QMember.member.email.like("%" + screen.getKeyWords() + "%"));
            }
            return success(memberTransactionService.joinFind(predicates, pageModel));
        }
        return success(memberTransactionService.queryWhereOrPage(predicates,pageModel));
    }
}
