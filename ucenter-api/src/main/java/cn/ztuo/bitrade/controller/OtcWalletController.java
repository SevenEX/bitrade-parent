package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.MemberLevelEnum;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.OtcWalletVO;
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

import java.math.BigDecimal;
import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static cn.ztuo.bitrade.util.BigDecimalUtils.*;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/5/5 3:18 PM
 */
@RestController
@RequestMapping("otc/wallet")
@Slf4j
@Api(tags = "法币账户")
public class OtcWalletController extends BaseController {

    /**
     * 1.币币账户到法币账户互转
     * 2.查询法币账户
     */


    @Autowired
    private OtcWalletService otcWalletService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private OtcCoinService otcCoinService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    /**
     * 查询用户法币账户
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "get", method = RequestMethod.POST)
    @ApiOperation(value = "查询用户法币账户")
    @MultiDataSource(name = "second")
    public MessageResult getUserOtcWallet(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        log.info("---------查询用户法币账户:" + user.getId());
        List<OtcWallet> result = otcWalletService.findByMemberId(user.getId());
        result.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            if (rate != null) {
                wallet.getCoin().setUsdRate(rate.getUsdRate());
                wallet.getCoin().setCnyRate(rate.getCnyRate());
            } else {
                log.info("unit = {} , rate = null ", wallet.getCoin().getUnit());
            }
        });
        return success(result);
    }



    /**
     * 币币账户到法币账户互转
     *
     * @param user
     * @param otcWalletVO
     * @return
     */
    @RequestMapping(value = "transfer", method = RequestMethod.POST)
    @ApiOperation(value = "币币账户到法币账户互转")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult transferOtcWallet(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, OtcWalletVO
            otcWalletVO) throws Exception {
        log.info("---------币币账户到法币账户互转:userId=" + user.getId() + "," + JSONObject.toJSONString(otcWalletVO));
        if(redisUtil.get(SysConstant.WITHDRAW_LOCK+user.getId())!=null&&(Boolean)redisUtil.get(SysConstant.WITHDRAW_LOCK+user.getId())&&("0").equals(otcWalletVO.getDirection())){ return error(sourceService.getMessage("WITHCRAW_LOCK"));}
//        String jyPassword = otcWalletVO.getJyPassword();
//        hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
        OtcCoin coin = otcCoinService.findByUnit(otcWalletVO.getCoinName());
        if (coin == null){
            return error(msService.getMessage("COIN_NOT_SUPPORT"));
        }
        BigDecimal amount = otcWalletVO.getAmount().setScale(coin.getCoinScale(), BigDecimal.ROUND_DOWN);
        Member member = memberService.findOne(user.getId());
        isTrue(member.getMemberLevel() != MemberLevelEnum.GENERAL, msService.getMessage("NO_REAL_NAME"));
//        Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(member.getJyPassword()),
//                sourceService.getMessage("ERROR_JYPASSWORD"));
        isTrue(compare(amount, BigDecimal.ZERO), sourceService.getMessage("PARAM_ERROR"));


        Coin memberCoin = coinService.findByUnit(coin.getUnit());

        //查询用户币币账户
        memberWalletService.findWalletForUpdate(member.getId(), memberCoin);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(memberCoin, user.getId());
        isTrue(memberWallet.getIsLock() == BooleanEnum.IS_FALSE, msService.getMessage("WALLET_LOCKED"));


        //查询用户法币账户
        OtcWallet otcWallet = otcWalletService.findByOtcCoinAndMemberId(member.getId(), coin);
        if (otcWallet == null) {
            //如果法币账户不存在新建
            OtcWallet otcWalletNew = new OtcWallet();
            otcWalletNew.setCoin(memberCoin);
            otcWalletNew.setIsLock(0);
            otcWalletNew.setMemberId(member.getId());
            otcWalletNew.setBalance(BigDecimal.ZERO);
            otcWalletNew.setFrozenBalance(BigDecimal.ZERO);
            otcWalletNew.setReleaseBalance(BigDecimal.ZERO);
            otcWalletNew.setVersion(0);
            otcWallet = otcWalletService.save(otcWalletNew);
            if (otcWallet == null) {
                return error(msService.getMessage("OTC_WALLET_ERROR"));
            }
        }
        isTrue(otcWallet.getIsLock() == 0, msService.getMessage("WALLET_LOCKED"));
        if ("0".equals(otcWalletVO.getDirection())) {
            //币币转法币
            isTrue(compare(memberWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));
            int subResult = otcWalletService.coin2Otc(memberWallet,otcWallet,amount);
            if (subResult == 1){
                return success(msService.getMessage("SUCCESS"));
            }
            return error(msService.getMessage("FAIL"));
        } else if ("1".equals(otcWalletVO.getDirection())) {
            //法币转币币
            isTrue(compare(otcWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));
            int addResult = otcWalletService.otc2Coin(memberWallet,otcWallet,amount);
            if (addResult == 1){
                return success(msService.getMessage("SUCCESS"));
            }
            return error(msService.getMessage("FAIL"));

        } else {
            return error(msService.getMessage("PARAM_ERROR"));
        }

    }


}
