package cn.ztuo.bitrade.controller.businessAuth;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.dto.BusinessDTO;
import cn.ztuo.bitrade.dto.OtcOrderCount;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.model.screen.BusinessAuthScreen;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.BusinessAuthApplyService;
import cn.ztuo.bitrade.service.BusinessAuthDepositService;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.OrderService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.entity.QMember.member;

/**
 * 商家认证可用保证金类型
 *
 * @author zhang yingxin
 * @date 2018/5/5
 */
@RestController
@RequestMapping("business-auth")
@Slf4j
@Api(tags = "商家认证管理")
public class BusinessAuthController extends BaseAdminController {
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;

    @Autowired
    private OrderService otcOrderService;

    @RequiresPermissions("business-auth:apply:page-query")
    @GetMapping("page")
    @ApiOperation(value = "分页查询商家认证")
    @MultiDataSource(name = "second")
    public MessageResult getAll(PageModel pageModel, CommonStatus status) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QBusinessAuthDeposit businessAuthDeposit = QBusinessAuthDeposit.businessAuthDeposit;
        if (status != null) {
            booleanExpressions.add(businessAuthDeposit.status.eq(status));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<BusinessAuthDeposit> depositPage = businessAuthDepositService.findAll(predicate, pageModel);
        MessageResult result = MessageResult.success();
        result.setData(depositPage);
        return result;
    }

    @RequiresPermissions("business-auth:apply:page-query")
    @PostMapping("create")
    @ApiOperation(value = "创建商家认证保证金记录")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "新增商家认证保证金")
    public MessageResult create(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
                                @RequestParam("amount") Double amount,
                                @RequestParam("coinUnit") String coinUnit) {
        Coin coin = coinService.findByUnit(coinUnit);
        if (coin == null) {
            return error("validate coinUnit");
        }
        BusinessAuthDeposit businessAuthDeposit = new BusinessAuthDeposit();
        businessAuthDeposit.setAmount(new BigDecimal(amount));
        businessAuthDeposit.setCoin(coin);
        businessAuthDeposit.setCreateTime(new Date());
        businessAuthDeposit.setAdmin(admin);
        businessAuthDeposit.setStatus(CommonStatus.NORMAL);
        businessAuthDepositService.save(businessAuthDeposit);
        return success();
    }

    @RequiresPermissions({"business-auth:apply:page-query","otc:businessaudit:business"})
    @PostMapping("apply/detail")
    @ApiOperation(value = "查询详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam("id") Long id) {
        MessageResult result = businessAuthApplyService.detail(id);
        return result;
    }

    @RequiresPermissions("business-auth:apply:page-query")
    @PatchMapping("update")
    @ApiOperation(value = "更新保证金")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "修改保证金")
    public MessageResult update(
            @RequestParam("id") Long id,
            @RequestParam("amount") Double amount,
            @RequestParam("coinUnit") String coinUnit,
            @RequestParam("status") CommonStatus status) {
        BusinessAuthDeposit oldData = businessAuthDepositService.findById(id);
        Coin coin = coinService.findByUnit(coinUnit);
        if (coin == null) {
            return error("validate coinUnit");
        }
        if(!oldData.getCoin().getUnit().equalsIgnoreCase(coin.getUnit())){
            oldData.setCoin(coin);
            oldData.setCreateTime(DateUtil.getCurrentDate());
        }
        if (amount != null) {
            /*if(businessAuthDeposit.getAmount().compareTo(oldData.getAmount())>0){
                //如果上调了保证金，所有使用当前类型保证金的已认证商家的认证状态都改为保证金不足
                ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
                booleanExpressions.add(QDepositRecord.depositRecord.coin.eq(oldData.getCoin()));
                booleanExpressions.add(QDepositRecord.depositRecord.status.eq(DepositStatusEnum.PAY));
                Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
                List<DepositRecord> depositRecordList=depositRecordService.findAll(predicate);
                if(depositRecordList!=null){
                    List<Long> idList=new ArrayList<>();
                    for(DepositRecord depositRecord:depositRecordList){
                        idList.add(depositRecord.getMember().getId());
                    }
                    memberService.updateCertifiedBusinessStatusByIdList(idList);
                }
            }*/
            oldData.setAmount(new BigDecimal(amount));
        }
        if (status != null) {
            oldData.setStatus(status);
        }
        businessAuthDepositService.save(oldData);
        return success();
    }

    @PostMapping("apply/page-query")
    @RequiresPermissions({"business-auth:apply:page-query","otc:businessaudit:business"})
    @ApiOperation(value = "分页查询商家认证申请")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel, BusinessAuthScreen screen) {
        List<BooleanExpression> lists = new ArrayList<>();
        lists.add(QBusinessAuthApply.businessAuthApply.member.certifiedBusinessStatus.ne(CertifiedBusinessStatus.NOT_CERTIFIED));
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            lists.add(QBusinessAuthApply.businessAuthApply.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(QBusinessAuthApply.businessAuthApply.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(QBusinessAuthApply.businessAuthApply.member.email.like("%" + screen.getKeyWords() + "%")));
        }else if(!StringUtils.isEmpty(screen.getKeyWords())){
            lists.add(QBusinessAuthApply.businessAuthApply.member.email.like("%" + screen.getKeyWords() + "%"));
        }
        if (screen.getStartTime() != null)
            lists.add(QBusinessAuthApply.businessAuthApply.createTime.goe(screen.getStartTime()));//大于等于开始时间
        if (screen.getEndTime() != null) {
            lists.add(QBusinessAuthApply.businessAuthApply.createTime.loe(DateUtils.addDays(screen.getEndTime(),1)));//小于等于结束时间
        }
        if (screen.getStatus() != null) {
            lists.add(QBusinessAuthApply.businessAuthApply.certifiedBusinessStatus.eq(screen.getStatus()));
        }
        Sort sort = Criteria.sortStatic("updateTime.desc","createTime.desc");
        Pageable pageable = PageRequest.of(pageModel.getPageNo() - 1, pageModel.getPageSize(), sort);
        Page<BusinessAuthApply> page = businessAuthApplyService.page(PredicateUtils.getPredicate(lists), pageable);
        List<BusinessDTO> businessDTOS = new ArrayList<>();
        for (BusinessAuthApply b : page.getContent()) {
            BusinessDTO businessDTO = new BusinessDTO();
            businessDTO.setId(b.getId());
            businessDTO.setAmount(b.getAmount());
            businessDTO.setAuditingTime(b.getAuditingTime());
            businessDTO.setAuthInfo(b.getAuthInfo());
            businessDTO.setBusinessAuthDeposit(b.getBusinessAuthDeposit());
            businessDTO.setCertifiedBusinessStatus(b.getCertifiedBusinessStatus());
            businessDTO.setCreateTime(b.getCreateTime());
            businessDTO.setDepositRecordId(b.getDepositRecordId());
            businessDTO.setDetail(b.getDetail());
            businessDTO.setInfo(b.getInfo());
            businessDTO.setMember(b.getMember());
            businessDTO.setUpdateTime(b.getUpdateTime());
            businessDTO.setVerifyLevel(b.getVerifyLevel());
            OtcOrderCount otcOrderCount =  otcOrderService.countOtcOrder(b.getMember().getId());
            if(otcOrderCount !=null && otcOrderCount.getCount30() != 0){
                businessDTO.setSuccessCount30(otcOrderCount.getSuccessCount30());
                businessDTO.setSuccessRate30(new BigDecimal(otcOrderCount.getSuccessCount30())
                        .divide(new BigDecimal(otcOrderCount.getCount30()),4, RoundingMode.HALF_DOWN));
            }
            businessDTOS.add(businessDTO);
        }

        PageResult<BusinessDTO> pageResult = new PageResult<>(businessDTOS.stream().map(b ->
                BusinessDTO.builder().id(b.getId())
                        .amount(b.getAmount())
                        .auditingTime(b.getAuditingTime())
                        .authInfo(b.getAuthInfo())
                        .businessAuthDeposit(b.getBusinessAuthDeposit())
                        .certifiedBusinessStatus(b.getCertifiedBusinessStatus())
                        .createTime(b.getCreateTime())
                        .depositRecordId(b.getDepositRecordId())
                        .detail(b.getDetail())
                        .info(b.getInfo())
                        .member(b.getMember())
                        .updateTime(b.getUpdateTime())
                        .verifyLevel(b.getVerifyLevel())
                        .successCount30(b.getSuccessCount30())
                        .successRate30(b.getSuccessRate30())
                        .build()
        ).collect(Collectors.toList()),pageModel.getPageNo()+1,page.getSize(),page.getTotalElements());

        return success(pageResult);

    }

    @PostMapping("get-search-status")
    @ApiOperation(value = "获取认证状态列表")
    @MultiDataSource(name = "second")
    public MessageResult getSearchStatus() {
        CertifiedBusinessStatus[] statuses = CertifiedBusinessStatus.values();
        List<Map> list = new ArrayList<>();
        for (CertifiedBusinessStatus status : statuses) {
            if (status == CertifiedBusinessStatus.NOT_CERTIFIED
                    || status.getOrdinal() >= CertifiedBusinessStatus.DEPOSIT_LESS.getOrdinal()) {
                continue;
            }
            Map map = new HashMap();
            map.put("name", status.getCnName());
            map.put("value", status.getOrdinal());
            list.add(map);
        }
        return success(list);
    }

    @RequiresPermissions("otc:businessaudit:business")
    @PatchMapping("updateV")
    @ApiOperation(value = "加V/去V")
    @AccessLog(module = AdminModule.BUSINESSAUTH, operation = "加V/去V")
    public MessageResult updateV(
            @RequestParam("id") Long id,
            @RequestParam("verifyLevel") VerifyLevel verifyLevel) {
        BusinessAuthApply oldData = businessAuthApplyService.findOne(id);
        if (verifyLevel != null) {
            oldData.setVerifyLevel(verifyLevel);
        }
        businessAuthApplyService.save(oldData);
        return success();
    }
}
