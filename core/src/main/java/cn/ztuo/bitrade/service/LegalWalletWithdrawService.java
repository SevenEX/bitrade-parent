package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import cn.ztuo.bitrade.dao.LegalWalletWithdrawDao;
import cn.ztuo.bitrade.entity.LegalWalletWithdraw;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QLegalWalletWithdraw;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class LegalWalletWithdrawService extends TopBaseService<LegalWalletWithdraw, LegalWalletWithdrawDao> {
    @Autowired
    private LegalWalletWithdrawDao legalWalletWithdrawDao;
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    protected LocaleMessageSourceService msService;

    @Autowired
    public void setDao(LegalWalletWithdrawDao legalWalletWithdrawDao) {
        super.setDao(super.dao = legalWalletWithdrawDao);
    }

    public LegalWalletWithdraw findOne(Long id) {
        return legalWalletWithdrawDao.findById(id).orElse(null);
    }

    //审核通过
    public void pass(LegalWalletWithdraw legalWalletWithdraw) {
        legalWalletWithdraw.setStatus(WithdrawStatus.WAITING);
        legalWalletWithdraw.setDealTime(new Date());//处理时间
        legalWalletWithdrawDao.save(legalWalletWithdraw);
    }

    public LegalWalletWithdraw findDetailWeb(Long id, Long memberId) {
        BooleanExpression and = QLegalWalletWithdraw.legalWalletWithdraw.id.eq(id)
                .and(QLegalWalletWithdraw.legalWalletWithdraw.member.id.eq(memberId));
        return legalWalletWithdrawDao.findOne(and).orElse(null);
    }

    //提现
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(MemberWallet wallet, LegalWalletWithdraw legalWalletWithdraw) {
        int count = memberWalletDao.freezeBalance(wallet.getId(), legalWalletWithdraw.getAmount());
        if(count == 0) {
            throw new RuntimeException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        legalWalletWithdrawDao.save(legalWalletWithdraw);
    }

    //提现不通过
    @Transactional(rollbackFor = Exception.class)
    public void noPass(MemberWallet wallet, LegalWalletWithdraw withdraw) {
        int count = memberWalletDao.thawBalance(wallet.getId(), withdraw.getAmount());
        if(count == 0) {
            throw new RuntimeException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        withdraw.setStatus(WithdrawStatus.FAIL);//标记失败
        withdraw.setDealTime(new Date());//处理时间
    }

    //打款
    @Transactional(rollbackFor = Exception.class)
    public void remit(String paymentInstrument, LegalWalletWithdraw withdraw, MemberWallet wallet) {
        withdraw.setPaymentInstrument(paymentInstrument);//支付凭证
        withdraw.setStatus(WithdrawStatus.SUCCESS);//标记成功
        withdraw.setRemitTime(new Date());//打款时间
        wallet.setFrozenBalance(BigDecimalUtils.sub(wallet.getFrozenBalance(), withdraw.getAmount()));//钱包冻结金额减少
    }
}
