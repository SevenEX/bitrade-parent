package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.CoinChainRelationDao;
import cn.ztuo.bitrade.dao.CoinDao;
import cn.ztuo.bitrade.dao.OtcCoinDao;
import cn.ztuo.bitrade.dao.SettlementCoinDao;
import cn.ztuo.bitrade.dto.CoinDTO;
import cn.ztuo.bitrade.dto.CoinInfo;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.BooleanEnum.IS_FALSE;
import static cn.ztuo.bitrade.constant.BooleanEnum.IS_TRUE;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:50
 */
@Service
public class CoinService extends BaseService {
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private CoinChainRelationDao coinChainRelationDao;
    @Autowired
    private SettlementCoinDao settlementCoinDao;
    @Autowired
    private OtcCoinDao otcCoinDao;
    @Autowired
    private LocalizationExtendService localizationExtendService;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Coin> query(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<Coin> list;
        JPAQuery<Coin> jpaQuery = queryFactory.selectFrom(QCoin.coin);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());//添加总条数
    }

    public Coin findOne(String name) {
        return coinDao.findById(name).orElse(null);
    }

    public Coin findByUnit(String unit) {
        return coinDao.findByUnit(unit);
    }

    public Coin save(Coin coin) {
        return coinDao.save(coin);
    }

    public List<Coin> findAll() {
        return coinDao.findAllOrderBySort();
    }

    public List<Coin> findAllCoinByOtc() {
        List<String> supportUnits = otcCoinDao.findAll().stream().map(x -> x.getUnit()).collect(Collectors.toList());
        if (supportUnits.size() > 0) {
            return coinDao.findAllByOtc(supportUnits);
        } else {
            return null;
        }
    }

    public Page<Coin> pageQuery(Integer pageNo, Integer pageSize) {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("sort");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, orders);
        //查询条件
        Criteria<Coin> specification = new Criteria<Coin>();
        return coinDao.findAll(specification, pageRequest);
    }

    public List<Coin> findAllCanWithDraw() {
        return coinDao.findAllByCanWithdrawAndStatusAndHasLegal(IS_TRUE, CommonStatus.NORMAL, false);
    }

    public void deleteOne(String name) {
        coinDao.deleteById(name);
    }

    /**
     * 设置平台币
     *
     * @param coin
     */
    @Transactional(rollbackFor = Exception.class)
    public void setPlatformCoin(Coin coin) {
        List<Coin> list = coinDao.findAll();
        list.stream().filter(x ->
                !x.getName().equals(coin.getName())
        ).forEach(x -> {
            x.setIsPlatformCoin(BooleanEnum.IS_FALSE);
            coinDao.save(x);
        });
        coin.setIsPlatformCoin(IS_TRUE);
        coinDao.saveAndFlush(coin);
        OtcCoin otcCoin = otcCoinDao.findOtcCoinByUnit(coin.getUnit());
        if (otcCoin != null) {
            otcCoin.setIsPlatformCoin(IS_TRUE);
            otcCoinDao.saveAndFlush(otcCoin);
        }
        List<OtcCoin> list1 = otcCoinDao.findAll();
        list1.stream().filter(x ->
                !x.getUnit().equals(coin.getUnit())
        ).forEach(x -> {
            x.setIsPlatformCoin(BooleanEnum.IS_FALSE);
            otcCoinDao.save(x);
        });
    }

    public Coin queryPlatformCoin() {
        return coinDao.findCoinByIsPlatformCoin(IS_TRUE);
    }

    /**
     * @Description: 查询所有合法币种
     * @author MrGao
     */
    public List<Coin> findLegalAll() {
        return (List<Coin>) coinDao.findAll(QCoin.coin.hasLegal.eq(true));
    }

    public Page<Coin> findAll(Predicate predicate, Pageable pageable) {
        return coinDao.findAll(predicate, pageable);
    }

    public Page findLegalCoinPage(PageModel pageModel) {
        BooleanExpression eq = QCoin.coin.hasLegal.eq(true);
        return coinDao.findAll(eq, pageModel.getPageable());
    }

    public List<String> getAllCoinName() {
        List<String> list = coinDao.findAllName();
        return list;
    }

    public List<CoinDTO> getAllCoinNameAndUnit() {
        List<CoinDTO> allNameAndUnit = coinDao.findAllNameAndUnit();
        return allNameAndUnit;
    }

    public List<String> getAllCoinNameLegal() {
        return coinDao.findAllCoinNameLegal();
    }

    public List<String> findAllRpcUnit() {
        return coinDao.findAllRpcUnit();
    }

    /**
     * 设置平台币
     *
     * @param coin 需要设置的币种(持久态)
     */
    @Transactional(rollbackFor = Exception.class)
    public void setPlatform(Coin coin) {
        //取消其他平台币
        List<Coin> coins = coinDao.findAllByIsPlatformCoin(IS_TRUE);
        coins.forEach(x -> x.setIsPlatformCoin(IS_FALSE));
        //设置传入币为平台币
        coin.setIsPlatformCoin(IS_TRUE);
    }

    public List<Coin> findAllByStatus(CommonStatus status) {
        return coinDao.findAllByStatus(status);
    }

    public List<Coin> findAllByStatusAndIsSettlement(CommonStatus status) {
        return coinDao.findAllByStatusAndIsSettlement(status,true);
    }

    public List<Coin> findAllByStatusAndIsSettlementNot(CommonStatus status) {
        return coinDao.findAllByStatusAndIsSettlementNot(status,true);
    }

    /**
     * 查询指定币种总额
     *
     * @param coin
     * @return
     */
    public BigDecimal sumBalance(Coin coin) {
        return coinDao.sumBalance(coin);
    }

    /**
     * 查询用户的某个币种余额
     *
     * @param coin
     * @param memberId
     * @return
     */
    public BigDecimal getBalanceByMemberIdAndCoinId(Coin coin, Long memberId) {
        return coinDao.getBalanceByMemberIdAndCoinId(coin, memberId);
    }

    public List<Coin> findByStatus(CommonStatus status) {
        return coinDao.findByStatus(status);
    }

    public void saveLocale(CoinInfo coinInfo) {
        localizationExtendService.updateLocaleInfo("Coin", coinInfo.getLocale(), coinInfo.getName(), "name", coinInfo.getCnName());
        localizationExtendService.updateLocaleInfo("Coin", coinInfo.getLocale(), coinInfo.getName(), "description", coinInfo.getDescription());
        localizationExtendService.updateLocaleInfo("Coin", coinInfo.getLocale(), coinInfo.getName(), "depositMessage", coinInfo.getDepositMessage());
        localizationExtendService.updateLocaleInfo("Coin", coinInfo.getLocale(), coinInfo.getName(), "withdrawMessage", coinInfo.getWithdrawMessage());
    }

    public List<LocalizationExtend> findCoinInfo(String name) {
        return localizationExtendService.getLocaleInfo("Coin",name);
    }

    public CoinChainRelation findRelationByKey(String coinKey) {
        return coinChainRelationDao.findByCoinKey(coinKey);
    }
}
