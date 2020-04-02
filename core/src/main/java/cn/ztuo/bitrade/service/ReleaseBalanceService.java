package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.MemberTransactionDao;
import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.dao.ReleaseBalanceDao;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.ReleaseBalance;
import cn.ztuo.bitrade.entity.RewardRecord;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.ReleaseBalanceVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class ReleaseBalanceService extends BaseService {

    @Autowired
    private ReleaseBalanceDao releaseBalanceDao;

    public ReleaseBalance save(ReleaseBalance releaseBalance) {
        return releaseBalanceDao.save(releaseBalance);
    }

    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private MemberTransactionDao memberTransactionDao;

    /**
     * 注册送币审核
     */
    public MessageResult updateReleaseBalance(ReleaseBalanceVO releaseBalanceVO) {
        List<Long> id = releaseBalanceVO.getId();
        ReleaseBalance releaseBalance;
        for (int i = 0; i < id.size(); i++) {
            releaseBalance = releaseBalanceDao.getOne(id.get(i));
            if (!"1".equals(releaseBalance.getReleaseState())) {
                String coinId = releaseBalance.getCoinUnit();
                Long memberId = releaseBalance.getMemberId();
                // 根据传过来的  用户id 去找钱包
                MemberWallet memberWallet = memberWalletDao.findCoinIdAndMemberId(coinId, memberId);
                // 获取释放余额.
                BigDecimal rbMoney = releaseBalance.getReleaseBalance();
                BigDecimal mwMoney = memberWallet.getReleaseBalance();
                if(mwMoney.compareTo(rbMoney) > -1){   //a大于等于b
                    // 将获取的释放余额  加 到钱包余额里面
                    int num= memberWalletDao.releaseReisterGiving(rbMoney,memberWallet.getId());
                    //更改审核状态 1 - 已审核
                    if(num ==1) {
                        releaseBalance.setReleaseState("1");
                        releaseBalance.setReleaseTime(new Date());
                        releaseBalanceDao.save(releaseBalance);
                        //会员交易记录，包括充值、提现、转账等
                        MemberTransaction memberTransaction = new MemberTransaction();
                        memberTransaction.setFee(BigDecimal.ZERO);
                        memberTransaction.setAmount(rbMoney);
                        memberTransaction.setSymbol(coinId);
                        memberTransaction.setType(TransactionType.ACTIVITY_AWARD);
                        memberTransaction.setMemberId(id.get(i));
                        memberTransactionDao.save(memberTransaction);
                    }
                }
            }
        }
        return MessageResult.success("审核已通过");
    }

    /**
     * 默认查询   余额状态为 0 是 未审核
     */
    public Page<ReleaseBalance> findByReleaseBalanceState(ReleaseBalanceVO releaseBalanceVO) {
        // 查询条件
        Criteria<ReleaseBalance> releaseBalance = new Criteria<>();
        // 条件查询
        releaseBalance.add(Restrictions.eq("releaseState", releaseBalanceVO.getReleaseState(), false));
        // 根据注册时间倒序
        Sort sorts = Criteria.sortStatic("registerTime.desc");
        // 分页
        PageRequest pageRequest = PageRequest.of(releaseBalanceVO.getPageNo() - 1, releaseBalanceVO.getPageSize(), sorts);
        // 返回
        return releaseBalanceDao.findAll(releaseBalance, pageRequest);
    }

    /**
     * 条件查询  用户名字  手机号  注册时间 审核状态
     */
    public Page<ReleaseBalance> conditionQueryAll(ReleaseBalanceVO releaseBalanceVO) {
        Criteria<ReleaseBalance> releaseBalance = new Criteria<>();
        // 条件查询
        if (StringUtils.isNotBlank(releaseBalanceVO.getMemberName())) {
            releaseBalance.add(Restrictions.eq("userName", releaseBalanceVO.getMemberName(), false));
        }
        if (StringUtils.isNotBlank(releaseBalanceVO.getPhone())) {
            releaseBalance.add(Restrictions.eq("phone", releaseBalanceVO.getPhone(), false));
        }
        if (StringUtils.isNotBlank(releaseBalanceVO.getReleaseState())) {
            releaseBalance.add(Restrictions.eq("releaseState", releaseBalanceVO.getReleaseState(), false));
        }
        if (releaseBalanceVO.getStartTime() != null) {
            releaseBalance.add(Restrictions.gte("registerTime", releaseBalanceVO.getStartTime(), false));
        }
        if (releaseBalanceVO.getEndTime() != null) {
            releaseBalance.add(Restrictions.lte("registerTime", releaseBalanceVO.getEndTime(), false));
        }
        // 根据注册时间倒序
        Sort sorts = Criteria.sortStatic("registerTime.desc");
        PageRequest pageRequest = PageRequest.of(releaseBalanceVO.getPageNo() - 1, releaseBalanceVO.getPageSize(), sorts);
        return releaseBalanceDao.findAll(releaseBalance, pageRequest);
    }
}


