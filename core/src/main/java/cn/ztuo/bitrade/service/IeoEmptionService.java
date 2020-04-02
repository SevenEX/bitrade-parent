package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.IeoEmptionDao;
import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.IeoEmption;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QIeoEmption;
import cn.ztuo.bitrade.service.Base.BaseService;

import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.IeoEmptionVO;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 3:59 PM
 */
@Service
public class IeoEmptionService extends BaseService {

    @Autowired
    private IeoEmptionDao ieoEmptionDao;

    @Autowired
    private MemberWalletDao memberWalletDao;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberService memberService;

    public Page<IeoEmption> getByPage(IeoEmptionVO ieoEmptionVO) throws Exception {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotEmpty(ieoEmptionVO.getStartTime())) {
            booleanExpressions.add(QIeoEmption.ieoEmption.createTime.goe(DateUtil.stringToDate(ieoEmptionVO
                    .getStartTime())));
        }
        if (StringUtils.isNotEmpty(ieoEmptionVO.getEndTime())) {
            booleanExpressions.add(QIeoEmption.ieoEmption.createTime.loe(DateUtil.stringToDate(ieoEmptionVO
                    .getEndTime())));
        }
        if (StringUtils.isNotEmpty(ieoEmptionVO.getStatus())) {
            Date date = new Date();
            String status = ieoEmptionVO.getStatus();
            if ("1".equals(status)) {
                booleanExpressions.add(QIeoEmption.ieoEmption.startTime.gt(date));
            } else if ("2".equals(status)) {
                booleanExpressions.add(QIeoEmption.ieoEmption.startTime.lt(date));
                booleanExpressions.add(QIeoEmption.ieoEmption.endTime.goe(date));
            } else if ("3".equals(status)) {
                booleanExpressions.add(QIeoEmption.ieoEmption.endTime.lt(date));
            }
        }
        if (ieoEmptionVO.getId() != null){
            booleanExpressions.add(QIeoEmption.ieoEmption.id.eq(ieoEmptionVO.getId()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(ieoEmptionVO.getPageNum() - 1, ieoEmptionVO.getPageSize(), sort);
        return ieoEmptionDao.findAll(predicate, pageable);

    }

    public IeoEmption save(IeoEmption ieoEmption) {
        return ieoEmptionDao.save(ieoEmption);
    }

    public IeoEmption findById(Long id) {
        return ieoEmptionDao.findById(id).orElse(null);
    }

    public void del(Long id){
        ieoEmptionDao.deleteById(id);
    }
    /**
     * 查询正在进行中的活动
     *
     * @param id
     * @param time
     * @return
     */
    public IeoEmption findbyCondition(Long id, String time) {
        return ieoEmptionDao.findbyCondition(id, time);
    }

    /**
     * 更新库存及更新用户余额
     *
     * @param userId
     * @param amount
     * @param ieoEmption
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int subAmount(BigDecimal amount, IeoEmption ieoEmption, Long userId) throws Exception {

        BigDecimal receAmount = amount.multiply(ieoEmption.getRatio()).setScale(4, BigDecimal.ROUND_DOWN);
        //减少库存
        int subResult = ieoEmptionDao.subAmount(ieoEmption.getId(), receAmount);
        if (subResult == 1) {
            Coin raiseCoin = coinService.findByUnit(ieoEmption.getRaiseCoin());
            //扣减用户募集币种
            MemberWallet raiseWallet = memberWalletDao.findByCoinAndMemberId(raiseCoin, userId);
            int subWallet = memberWalletDao.decreaseBalance(raiseWallet.getId(), amount);
            if (subWallet == 1) {
                //增加用户认购币种
                Coin saleCoin = coinService.findByUnit(ieoEmption.getSaleCoin());
                MemberWallet saleWallet = memberWalletDao.findByCoinAndMemberId(saleCoin,userId);
                if (saleWallet == null){
                    int insertResult = memberService.saveWallet(ieoEmption.getSaleCoin(),userId,receAmount);
                    if (insertResult == 1){
                        return 1;
                    }else {
                        throw new Exception("增加用户余额异常："+userId+"，coin:"+ieoEmption.getSaleCoin());
                    }

                }else {
                    int addResult = memberWalletDao.increaseBalance(saleWallet.getId(),receAmount);
                    if (addResult == 1){
                        return 1;
                    }else {
                        throw new Exception("增加用户余额异常："+userId+"，coin:"+ieoEmption.getSaleCoin());
                    }
                }

            } else {
                throw new Exception("扣减用户余额异常：" + userId + "，coin:" + ieoEmption.getRaiseCoin());
            }
        } else {
            return 0;
        }
    }
}
