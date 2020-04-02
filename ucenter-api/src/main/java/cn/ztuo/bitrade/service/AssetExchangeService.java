package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.AssetExchangeDao;
import cn.ztuo.bitrade.entity.AssetExchangeCoin;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetExchangeService extends BaseService {
    @Autowired
    private AssetExchangeDao assetExchangeDao;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    public List<AssetExchangeCoin> findAllByFromCoin(String toUnit){
        return assetExchangeDao.findAllByToUnit(toUnit);
    }

    public AssetExchangeCoin findOne(String fromUnit,String toUnit){
        return assetExchangeDao.findByFromUnitAndToUnit(fromUnit,toUnit);
    }

    @Transactional
    public MessageResult exchange(Long memberId, AssetExchangeCoin coin, BigDecimal amount){
        MemberWallet fromWallet = memberWalletService.findByCoinUnitAndMemberId(coin.getFromUnit(),memberId);
        MemberWallet toWallet = memberWalletService.findByCoinUnitAndMemberId(coin.getToUnit(),memberId);
        if(fromWallet == null || toWallet == null){
            return new MessageResult(500,msService.getMessage("WALLET_NOT_EXTIS"));
        }
        BigDecimal toAmount = amount.multiply(coin.getExchangeRate());
        if(memberWalletService.deductBalance(fromWallet,amount) > 0){
            //增加余额
            memberWalletService.increaseBalance(toWallet.getId(),toAmount);
            //增加入资金记录
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(toAmount);
            transaction.setSymbol(coin.getToUnit());
            transaction.setAddress("");
            transaction.setMemberId(toWallet.getMemberId());
            transaction.setType(TransactionType.ASSET_EXCHANGE);
            transaction.setFee(BigDecimal.ZERO);
            transactionService.save(transaction);

            //增加出资金记录
            MemberTransaction transaction2 = new MemberTransaction();
            transaction2.setAmount(amount.negate());
            transaction2.setSymbol(coin.getFromUnit());
            transaction2.setAddress("");
            transaction2.setMemberId(fromWallet.getMemberId());
            transaction2.setType(TransactionType.ASSET_EXCHANGE);
            transaction2.setFee(BigDecimal.ZERO);
            transactionService.save(transaction2);

            return new MessageResult(0,messageSource.getMessage("SUCCESS"));
        }
        else return new MessageResult(500,msService.getMessage("INSUFFICIENT_BALANCE"));
    }
}
