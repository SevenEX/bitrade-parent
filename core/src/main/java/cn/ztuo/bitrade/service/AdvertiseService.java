package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.core.DB;
import cn.ztuo.bitrade.core.DataException;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.dao.AdvertiseDao;
import cn.ztuo.bitrade.dao.OtcCoinDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.*;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.BooleanEnum.IS_FALSE;
import static cn.ztuo.bitrade.constant.BooleanEnum.IS_TRUE;
import static cn.ztuo.bitrade.entity.QAdvertise.advertise;
import static cn.ztuo.bitrade.util.BigDecimalUtils.mulRound;
import static cn.ztuo.bitrade.util.BigDecimalUtils.rate;

/**
 * @author Seven
 * @date 2019年12月07日
 */
@Service
@Slf4j
public class AdvertiseService extends BaseService {

    @Autowired
    private AdvertiseDao advertiseDao;
    @Autowired
    private OtcCoinDao otcCoinDao;
    @Autowired
    private OtcWalletService otcWalletService;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Advertise> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<Advertise> list;
        JPAQuery<Advertise> jpaQuery = queryFactory.selectFrom(advertise);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    public Advertise findOne(Long id) {
        return advertiseDao.findById(id).orElse(null);
    }

    public MemberAdvertiseDetail findOne(Long id, Long memberId) {
        Advertise advertise = advertiseDao.findAdvertiseByIdAndMemberIdAndStatusNot(id, memberId, AdvertiseControlStatus.TURNOFF);
        if (advertise != null) {
            return MemberAdvertiseDetail.toMemberAdvertiseDetail(advertise);
        } else {
            return null;
        }
    }

    public Advertise find(long id, long memberId) {
        return advertiseDao.findByIdAndMemberId(id, memberId);
    }

    public Advertise saveAdvertise(Advertise advertise) {
        return advertiseDao.save(advertise);
    }

    @Transactional(rollbackFor = Exception.class)
    public int turnOffBatch(AdvertiseControlStatus status, Long[] ids) {
        for(Long advertiseId:ids){
            Advertise advertise = this.findOne(advertiseId);
            if(advertise == null){
                return 0;
            }
            if(!advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES)){
                return 0;
            }
            if(advertise.getDealAmount()!=null&&advertise.getDealAmount().compareTo(BigDecimal.ZERO)>0){
                return -100;
            }
            OtcCoin otcCoin = advertise.getCoin();
            if (advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
                OtcWallet memberWallet = otcWalletService.findByOtcCoinAndMemberId(advertise.getMember().getId(),
                        otcCoin);
                MessageResult result = otcWalletService.thawBalance(memberWallet, advertise.getRemainAmount());
                if (result.getCode() != 0) {
                    return 0;
                }
            }
            int ret = this.putOffShelves(advertise.getId(), advertise.getRemainAmount());
            if (!(ret > 0)) {
                return 0;
            }
        }
        return 1;
    }

    public Advertise modifyAdvertise(Advertise advertise, Advertise old) {
        if (advertise.getPriceType() == PriceType.MUTATIVE) {
            //变化的
            old.setPriceType(PriceType.MUTATIVE);
            old.setPremiseRate(advertise.getPremiseRate());
        } else {
            //固定的
            old.setPriceType(PriceType.REGULAR);
            old.setPrice(advertise.getPrice());
        }
        if (advertise.getAuto().isIs()) {
            old.setAuto(BooleanEnum.IS_TRUE);
            old.setAutoword(advertise.getAutoword());
        } else {
            old.setAuto(BooleanEnum.IS_FALSE);
        }
        old.setMinLimit(advertise.getMinLimit());
        old.setMaxLimit(advertise.getMaxLimit());
        old.setTimeLimit(advertise.getTimeLimit());
        old.setRemark(advertise.getRemark());
        old.setPayMode(advertise.getPayMode());
        old.setNumber(advertise.getNumber());
        old.setRemainAmount(advertise.getNumber());
        //变更为下架状态
        old.setStatus(AdvertiseControlStatus.PUT_OFF_SHELVES);
        return advertiseDao.save(old);

    }

    public List<MemberAdvertise> getAllAdvertiseByMemberId(Long memberId, Sort sort) {
        List<Advertise> list = advertiseDao.findAllByMemberIdAndStatusNot(memberId, AdvertiseControlStatus.TURNOFF, sort);
        return list.stream().map(x ->
                MemberAdvertise.toMemberAdvertise(x)
        ).collect(Collectors.toList());
    }

    public Advertise getAllAdvertiseForEasyBuy(Long memberId, Long coinId, BigDecimal money, String payMode) {
        return advertiseDao.findAdvertiseForEasyBuy(memberId, coinId, money, payMode);
    }

    public Advertise getAllAdvertiseForEasyBuyByAmount(Long memberId, Long coinId, BigDecimal amount, String payMode) {
        return advertiseDao.findAdvertiseForEasyBuyByAmount(memberId, coinId, amount, payMode);
    }

    public Advertise getAllAdvertiseForEasySell(Long memberId, Long coinId, BigDecimal money, String payMode, String payMode2, String payMode3) {
        return advertiseDao.findAdvertiseForEasySell(memberId, coinId, money, payMode, payMode2, payMode3);
    }

    public Advertise getAllAdvertiseForEasySellByAmount(Long memberId, Long coinId, BigDecimal amount, String payMode, String payMode2, String payMode3) {
        return advertiseDao.findAdvertiseForEasySellByAmount(memberId, coinId, amount, payMode, payMode2, payMode3);
    }

    public List<ScanAdvertise> getAllExcellentAdvertise(AdvertiseType type, List<Map<String, String>> list) throws SQLException, DataException {
        List<ScanAdvertise> excellents = new ArrayList<>();
        String sql = "SELECT\n" +
                "\td.*\n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tc.coin_id,\n" +
                "\t\t\t(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN c.price_type = 0\n" +
                "\t\t\t\tAND c.price = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tWHEN c.price_type = 1\n" +
                "\t\t\t\tAND round(((c.premise_rate + 100) / 100 * ?),2) = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) advertise_id,\n" +
                "\t\t\tb.minPrice\n" +
                "\t\tFROM\n" +
                "\t\t\tadvertise c\n" +
                "\t\tJOIN (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\ta.coin_id,\n" +
                (type.equals(AdvertiseType.SELL) ? "\t\t\t\tmin(\n" : "\t\t\t\tmax(\n") +
                "\t\t\t\t\tCASE a.price_type\n" +
                "\t\t\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\t\t\ta.price\n" +
                "\t\t\t\t\tELSE\n" +
                "\t\t\t\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\t\t\t\tEND\n" +
                "\t\t\t\t) minPrice,\n" +
                "\t\t\t\ta.advertise_type,\n" +
                "\t\t\t\ta.`status`\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tadvertise a\n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\ta. STATUS = 0\n" +
                "\t\t\tAND a.advertise_type = ?\n" +
                "\t\t\tGROUP BY\n" +
                "\t\t\t\ta.coin_id\n" +
                "\t\t) b ON c.coin_id = b.coin_id\n" +
                "\t\tAND c.advertise_type = b.advertise_type\n" +
                "\t\tAND c.`status` = b. STATUS\n" +
                "\t\tAND c.coin_id = ?\n" +
                "\t) d\n" +
                "WHERE\n" +
                "\td.advertise_id IS NOT NULL\n" +
                "GROUP BY\n" +
                "\td.coin_id";
        list.parallelStream()
                .forEachOrdered((Map<String, String> x) -> {
                    OtcCoin otcCoin = otcCoinDao.findOtcCoinByUnitAndStatus(x.get("name"), CommonStatus.NORMAL);
                    if (otcCoin != null) {
                        try {
                            List<Map<String, String>> mapList = DB.query(sql, x.get("price"), x.get("price"), type.ordinal(), otcCoin.getId());
                            if (mapList.size() > 0) {
                                Advertise advertise = advertiseDao.findById(Long.valueOf(mapList.get(0).get("advertise_id"))).orElse(null);
                                Member member = advertise.getMember();
                                excellents.add(ScanAdvertise
                                        .builder()
                                        .advertiseId(advertise.getId())
                                        .coinId(otcCoin.getId())
                                        .coinName(otcCoin.getName())
                                        .coinNameCn(otcCoin.getNameCn())
                                        .createTime(advertise.getCreateTime())
                                        .maxLimit(advertise.getMaxLimit())
                                        .minLimit(advertise.getMinLimit())
                                        .memberName(member.getUsername())
                                        .avatar(member.getAvatar())
                                        .level(member.getMemberLevel().ordinal())
                                        .payMode(advertise.getPayMode())
                                        .unit(otcCoin.getUnit())
                                        .remainAmount(advertise.getRemainAmount())
                                        .transactions(member.getTransactions())
                                        .price(BigDecimalUtils.round(Double.valueOf(mapList.get(0).get("minPrice")), 2))
                                        .build()
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        return excellents;
    }

    public List<ScanAdvertise> getAllExcellentAdvertise(AdvertiseType type, List<Map<String, String>> list, Country country) throws SQLException, DataException {
        List<ScanAdvertise> excellents = new ArrayList<>();
        String sql = "SELECT\n" +
                "\td.*\n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tc.coin_id,\n" +
                "\t\t\t(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN c.price_type = 0\n" +
                "\t\t\t\tAND c.price = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tWHEN c.price_type = 1\n" +
                "\t\t\t\tAND round(((c.premise_rate + 100) / 100 * ?),2) = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) advertise_id,\n" +
                "\t\t\tb.minPrice\n" +
                "\t\tFROM\n" +
                "\t\t\tadvertise c\n" +
                "\t\tJOIN (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\ta.coin_id,\n" +
                (type.equals(AdvertiseType.SELL) ? "\t\t\t\tmin(\n" : "\t\t\t\tmax(\n") +
                "\t\t\t\t\tCASE a.price_type\n" +
                "\t\t\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\t\t\ta.price\n" +
                "\t\t\t\t\tELSE\n" +
                "\t\t\t\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\t\t\t\tEND\n" +
                "\t\t\t\t) minPrice,\n" +
                "\t\t\t\ta.advertise_type,\n" +
                "\t\t\t\ta.`status`\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tadvertise a\n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\ta. STATUS = 0\n" +
                "\t\t\tAND a.advertise_type = ?\n" +
                "\t\t\tGROUP BY\n" +
                "\t\t\t\ta.coin_id\n" +
                "\t\t) b ON c.coin_id = b.coin_id\n" +
                "\t\tAND c.advertise_type = b.advertise_type\n" +
                "\t\tAND c.`status` = b. STATUS\n" +
                "\t\tAND c.country = ?\n" +
                "\t\tAND c.coin_id = ?\n" +
                "\t) d\n" +
                "WHERE\n" +
                "\td.advertise_id IS NOT NULL\n" +
                "GROUP BY\n" +
                "\td.coin_id";
        list.parallelStream()
                .forEachOrdered((Map<String, String> x) -> {
                    OtcCoin otcCoin = otcCoinDao.findOtcCoinByUnitAndStatus(x.get("name"), CommonStatus.NORMAL);
                    if (otcCoin != null) {
                        try {
                            List<Map<String, String>> mapList = DB.query(sql, x.get("price"), x.get("price"), type.ordinal(),country.getZhName(), otcCoin.getId());
                            if (mapList.size() > 0) {
                                Advertise advertise = advertiseDao.findById(Long.valueOf(mapList.get(0).get("advertise_id"))).orElse(null);
                                Member member = advertise.getMember();
                                excellents.add(ScanAdvertise
                                        .builder()
                                        .advertiseId(advertise.getId())
                                        .coinId(otcCoin.getId())
                                        .coinName(otcCoin.getName())
                                        .coinNameCn(otcCoin.getNameCn())
                                        .createTime(advertise.getCreateTime())
                                        .maxLimit(advertise.getMaxLimit())
                                        .minLimit(advertise.getMinLimit())
                                        .memberName(member.getUsername())
                                        .avatar(member.getAvatar())
                                        .level(member.getMemberLevel().ordinal())
                                        .payMode(advertise.getPayMode())
                                        .unit(otcCoin.getUnit())
                                        .remainAmount(advertise.getRemainAmount())
                                        .transactions(member.getTransactions())
                                        .price(BigDecimalUtils.round(Double.valueOf(mapList.get(0).get("minPrice")), 2))
                                        .build()
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        return excellents;
    }

    public SpecialPage<ScanAdvertise> paginationAdvertise(int pageNo, int pageSize, OtcCoin otcCoin, AdvertiseType advertiseType, double marketPrice, int isCertified) throws SQLException, DataException {
        SpecialPage<ScanAdvertise> specialPage = new SpecialPage<>();
        String sql = "SELECT\n" +
                "\ta.*, (\n" +
                "\t\tCASE a.price_type\n" +
                "\t\tWHEN 0 THEN\n" +
                "\t\t\ta.price\n" +
                "\t\tELSE\n" +
                "\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\tEND\n" +
                "\t) finalPrice,\n" +
                "\tb.avatar,\n" +
                "\tb.username,\n" +
                "\tb.member_level,\n" +
                "\tb.transactions\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "JOIN member b ON a.member_id = b.id\n" +
                (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                "AND a.coin_id = ?\n" +
                "AND a.advertise_type = ?\n" +
                "AND a.`status` = 0\n" +
                "ORDER BY\n" +
                (advertiseType.equals(AdvertiseType.SELL) ? "\tfinalPrice,\n" : "\tfinalPrice desc,\n") +
                "\ta.id\n" +
                "LIMIT ?,\n" +
                " ?";
        List<Map<String, String>> list = DB.query(sql, marketPrice, otcCoin.getId(), advertiseType.ordinal(), (pageNo - 1) * pageSize, pageSize);
        if (list!=null&&list.size() > 0) {
            String sql1 = "SELECT\n" +
                    "\tCOUNT(a.id) total\n" +
                    "FROM\n" +
                    "\tadvertise a\n" +
                    "JOIN member b ON a.member_id = b.id\n" +
                    (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                    "AND a.coin_id = ?\n" +
                    "AND a.advertise_type = ?\n" +
                    "AND a.`status` = 0";
            List<Map<String, String>> list1 = DB.query(sql1, otcCoin.getId(), advertiseType.ordinal());
            Map<String, String> map = list1.get(0);
            int total = Integer.valueOf(map.get("total"));
            specialPage.setTotalElement(total);
            specialPage.setTotalPage(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
            specialPage.setContext(
                    list.stream().map((Map<String, String> x) ->
                            ScanAdvertise.builder()
                                    .price(BigDecimalUtils.round(Double.valueOf(x.get("finalPrice")), 2))
                                    .transactions(Integer.parseInt(x.get("transactions")))
                                    .remainAmount(BigDecimal.valueOf(Double.valueOf(x.get("remain_amount"))))
                                    .unit(otcCoin.getUnit())
                                    .payMode(x.get("pay_mode"))
                                    .memberName(x.get("username"))
                                    .avatar(x.get("avatar"))
                                    .minLimit(BigDecimal.valueOf(Double.valueOf(x.get("min_limit"))))
                                    .maxLimit(BigDecimal.valueOf(Double.valueOf(x.get("max_limit"))))
                                    .coinNameCn(otcCoin.getNameCn())
                                    .level(Integer.parseInt(x.get("member_level")))
                                    .coinId(otcCoin.getId())
                                    .coinName(otcCoin.getName())
                                    .advertiseId(Long.valueOf(x.get("id")))
                                    .createTime(DateUtil.strToDate(x.get("create_time")))
                                    .advertiseType(advertiseType)
                                    .build()
                    ).collect(Collectors.toList()));
        } else {
            specialPage.setTotalPage(1);
            specialPage.setTotalElement(0);
        }
        specialPage.setCurrentPage(pageNo);
        specialPage.setPageNumber(pageSize);
        return specialPage;
    }
    public SpecialPage<ScanAdvertise> paginationAdvertise(int pageNo, int pageSize, OtcCoin otcCoin, AdvertiseType advertiseType, double marketPrice, int isCertified,String[] paymodes,String limit) throws SQLException, DataException {
        SpecialPage<ScanAdvertise> specialPage = new SpecialPage<>();
        StringBuffer payConditon=new StringBuffer(" ");
        if (paymodes!=null){
            payConditon.append("AND ( ");
            for (String paymode:paymodes){
                payConditon.append(" FIND_IN_SET('" + paymode + "',a.pay_mode) OR ");
            }
            payConditon.delete(payConditon.length() - 3, payConditon.length());
            payConditon.append(" ) ");
            //payConditon.append("AND a.pay_mode in ("+Arrays.toString(paymodes)+") ");
        }
        if(!StringUtils.isEmpty(limit)){
            payConditon.append(" \tAND "+limit+" BETWEEN min_limit AND max_limit \n");
        }
        String sql = "SELECT\n" +
                "\ta.*, (\n" +
                "\t\tCASE a.price_type\n" +
                "\t\tWHEN 0 THEN\n" +
                "\t\t\ta.price\n" +
                "\t\tELSE\n" +
                "\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\tEND\n" +
                "\t) finalPrice,\n" +
                "\tb.avatar,\n" +
                "\tb.username,\n" +
                "\tb.member_level,\n" +
                "\tb.transactions,\n" +
                "\tbaa.verify_level,\n" +
                "\ta.member_id,\n" +
                "\ta.top\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "JOIN member b ON a.member_id = b.id\n" +
                (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                "JOIN business_auth_apply baa on baa.member_id = b.id\n"+
                "AND a.coin_id = ?\n" +
                "AND a.advertise_type = ?\n" +
               // "AND a.country = ?\n" +
                payConditon.toString()+
                "AND a.`status` = 0\n" +
                "ORDER BY a.top DESC,\n" +
                (advertiseType.equals(AdvertiseType.SELL) ? "\tfinalPrice,\n" : "\tfinalPrice desc,\n") +
                "\ta.id\n" +
                "LIMIT ?,\n" +
                " ?";
        List<Map<String, String>> list = DB.query(sql, marketPrice, otcCoin.getId(), advertiseType.ordinal(), (pageNo - 1) * pageSize, pageSize);
        if (list!=null && list.size() > 0) {
            String sql1 = "SELECT\n" +
                    "\tCOUNT(a.id) total\n" +
                    "FROM\n" +
                    "\tadvertise a\n" +
                    "JOIN member b ON a.member_id = b.id\n" +
                    (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                    "AND a.coin_id = ?\n" +
                    payConditon.toString()+
                    "AND a.advertise_type = ?\n" +
                    //"AND a.country = ?\n" +
                    "AND a.`status` = 0";
            List<Map<String, String>> list1 = DB.query(sql1, otcCoin.getId(), advertiseType.ordinal());
            Map<String, String> map = list1.get(0);
            int total = Integer.valueOf(map.get("total"));
            specialPage.setTotalElement(total);
            specialPage.setTotalPage(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
            specialPage.setContext(
                    list.stream().map((Map<String, String> x) ->
                            ScanAdvertise.builder()
                                    .price(BigDecimalUtils.round(Double.valueOf(x.get("finalPrice")), 2))
                                    .transactions(Integer.parseInt(x.get("transactions")))
                                    .remainAmount(BigDecimal.valueOf(Double.valueOf(x.get("remain_amount"))))
                                    .unit(otcCoin.getUnit())
                                    .payMode(x.get("pay_mode"))
                                    .memberName(x.get("username"))
                                    .memberId(Long.valueOf(x.get("member_id")))
                                    .avatar(x.get("avatar"))
                                    .minLimit(BigDecimal.valueOf(Double.valueOf(x.get("min_limit"))))
                                    .maxLimit(BigDecimal.valueOf(Double.valueOf(x.get("max_limit"))))
                                    .coinNameCn(otcCoin.getNameCn())
                                    .level(Integer.parseInt(x.get("member_level")))
                                    .coinId(otcCoin.getId())
                                    .coinName(otcCoin.getName())
                                    .advertiseId(Long.valueOf(x.get("id")))
                                    .createTime(DateUtil.strToDate(x.get("create_time")))
                                    .advertiseType(advertiseType)
                                    .verifyLevel(x.get("verify_level"))
                                    .top(x.get("top"))
                                    .build()
                    ).collect(Collectors.toList()));
        } else {
            specialPage.setTotalPage(1);
            specialPage.setTotalElement(0);
        }
        specialPage.setCurrentPage(pageNo);
        specialPage.setPageNumber(pageSize);
        return specialPage;
    }

    public Page<ScanAdvertise> paginationQuery(int pageNo, int pageSize, String country, String payMode, AdvertiseType advertiseType,OtcCoin coin,BigDecimal marketPrice) {
        Sort.Order order1 = new Sort.Order(Sort.Direction.ASC, "price");
        Sort.Order order2 = new Sort.Order(Sort.Direction.DESC, "id");
        Sort sort = Sort.by(order1, order2);
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, sort);
        Specification<Advertise> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> country1 = root.get("country");
            Path<String> payMode1 = root.get("payMode");
            Path<AdvertiseType> advertiseType1 = root.get("advertiseType");
            Path<Long> currency1 = root.get("coin").get("id");
            Path<CommonStatus> status1 = root.get("status");
            Predicate predicate1 = criteriaBuilder.like(payMode1, "%" + payMode + "%");
            Predicate predicate2 = criteriaBuilder.equal(country1, country);
            Predicate predicate3 = criteriaBuilder.equal(advertiseType1, advertiseType);
            Predicate predicate4 = criteriaBuilder.equal(currency1, coin.getId());
            Predicate predicate5 = criteriaBuilder.equal(status1, CommonStatus.NORMAL);
            if (country == null && payMode == null) {
                return criteriaBuilder.and(predicate3, predicate4, predicate5);
            } else if (country != null && payMode == null) {
                return criteriaBuilder.and(predicate2, predicate3, predicate4, predicate5);
            } else if (country == null && payMode != null) {
                return criteriaBuilder.and(predicate1, predicate3, predicate4, predicate5);
            } else {
                return criteriaBuilder.and(predicate1, predicate2, predicate3, predicate4, predicate5);
            }
        };
        Page<Advertise> page = advertiseDao.findAll(specification, pageRequest);
        //todo:得到市场价
        //BigDecimal markerprice = BigDecimal.TEN;
        Page<ScanAdvertise> page1 = page.map((Advertise advertise) -> {
            Member member = advertise.getMember();
            return ScanAdvertise.builder()
                    .advertiseId(advertise.getId())
                    .coinId(advertise.getCoin().getId())
                    .coinName(advertise.getCoin().getName())
                    .coinNameCn(advertise.getCoin().getNameCn())
                    .createTime(advertise.getCreateTime())
                    .maxLimit(advertise.getMaxLimit())
                    .minLimit(advertise.getMinLimit())
                    .memberName(member.getUsername())
                    .payMode(advertise.getPayMode())
                    .unit(advertise.getCoin().getUnit())
                    .remainAmount(advertise.getRemainAmount())
                    .transactions(member.getTransactions())
                    .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                            advertise.getPrice() :
                            marketPrice.multiply(advertise.getPremiseRate().divide(new BigDecimal(100)).add(BigDecimal.ONE)))
                    .build();

        });
        return page1;
    }

    public MemberAdvertiseInfo getMemberAdvertise(Member member, HashMap<String, BigDecimal> map) {
        List<Advertise> buy = advertiseDao.findAllByMemberIdAndStatusAndAdvertiseType(member.getId(), AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseType.BUY);
        List<Advertise> sell = advertiseDao.findAllByMemberIdAndStatusAndAdvertiseType(member.getId(), AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseType.SELL);
        return MemberAdvertiseInfo.builder()
                .createTime(member.getRegistrationTime())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE : IS_TRUE)
                .transactions(member.getTransactions())
                .username(member.getUsername())
                .avatar(member.getAvatar())
                .buy(buy.stream().map(advertise -> {
                    BigDecimal markerPrice = map.get(advertise.getCoin().getUnit());
                    Member member1 = advertise.getMember();
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .advertiseType(advertise.getAdvertiseType())
                            .coinId(advertise.getCoin().getId())
                            .coinName(advertise.getCoin().getName())
                            .coinNameCn(advertise.getCoin().getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(advertise.getCoin().getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .level(member1.getMemberLevel().getOrdinal())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                                    advertise.getPrice() :
                                    mulRound(markerPrice, rate(advertise.getPremiseRate()), 2))
                            .build();
                }).collect(Collectors.toList()))
                .sell(sell.stream().map(advertise -> {
                    BigDecimal markerPrice = map.get(advertise.getCoin().getUnit());
                    Member member1 = advertise.getMember();
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .advertiseType(advertise.getAdvertiseType())
                            .coinId(advertise.getCoin().getId())
                            .coinName(advertise.getCoin().getName())
                            .coinNameCn(advertise.getCoin().getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(advertise.getCoin().getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .level(member1.getMemberLevel().getOrdinal())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                                    advertise.getPrice() : mulRound(markerPrice, rate(advertise.getPremiseRate()), 2)
                            )
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    public boolean updateAdvertiseAmountForBuy(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseAmount(AdvertiseControlStatus.PUT_ON_SHELVES, advertiseId, amount);
        return ret > 0 ? true : false;
    }

    public boolean updateAdvertiseAmountForCancel(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    public boolean updateAdvertiseAmountForRelease(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    /**
     * 得到出售类型自动下架的广告
     *
     * @param coinId
     * @param marketPrice
     * @return
     */
    public List<Map<String, String>> selectSellAutoOffShelves(long coinId, BigDecimal marketPrice, BigDecimal jyRate) throws SQLException, DataException {
        String sql = "SELECT b.* FROM (SELECT\n" +
                "\ta.*, CAST(\n" +
                "\t\ta.min_limit / (\n" +
                "\t\t\tCASE a.price_type\n" +
                "\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\ta.price\n" +
                "\t\t\tELSE\n" +
                "\t\t\t\tround(\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t(a.premise_rate + 100) / 100 * ?\n" +
                "\t\t\t\t\t),\n" +
                "\t\t\t\t\t2\n" +
                "\t\t\t\t)\n" +
                "\t\t\tEND\n" +
                "\t\t) AS DECIMAL (18, 8)\n" +
                "\t) minWithdrawAmount\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "WHERE\n" +
                "\ta.`status` = 0\n" +
                "AND a.advertise_type = 1\n" +
                "AND a.coin_id = ?) b WHERE b.remain_amount<ROUND(((? + 100) / 100 * b.minWithdrawAmount),8)";
        List<Map<String, String>> list = DB.query(sql, marketPrice, coinId, jyRate);
        return list;
    }

    /**
     * 得到购买类型自动下架的广告
     *
     * @param coinId
     * @param marketPrice
     * @return
     */
    public List<Map<String, String>> selectBuyAutoOffShelves(long coinId, BigDecimal marketPrice) throws SQLException, DataException {
        String sql = "SELECT b.* FROM (SELECT\n" +
                "\ta.*, CAST(\n" +
                "\t\ta.min_limit / (\n" +
                "\t\t\tCASE a.price_type\n" +
                "\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\ta.price\n" +
                "\t\t\tELSE\n" +
                "\t\t\t\tround(\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t(a.premise_rate + 100) / 100 * ?\n" +
                "\t\t\t\t\t),\n" +
                "\t\t\t\t\t2\n" +
                "\t\t\t\t)\n" +
                "\t\t\tEND\n" +
                "\t\t) AS DECIMAL (18, 8)\n" +
                "\t) minWithdrawAmount\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "WHERE\n" +
                "\ta.`status` = 0\n" +
                "AND a.advertise_type = 0\n" +
                "AND a.coin_id = ?) b WHERE b.remain_amount<b.minWithdrawAmount";
        List<Map<String, String>> list = DB.query(sql, marketPrice, coinId);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoPutOffShelves(Map<String, String> map, OtcCoin otcCoin) throws InformationExpiredException {
        if (map.get("advertise_type").equals(String.valueOf(AdvertiseType.SELL.ordinal()))) {
            OtcWallet memberWallet = otcWalletService.findByOtcCoinAndMemberId(Long.valueOf(map.get("member_id")),
                    otcCoin);
            MessageResult result = otcWalletService.thawBalance(memberWallet, new BigDecimal(map.get("remain_amount")));
            if (result.getCode() != 0) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }
        int is = putOffShelves(Long.valueOf(map.get("id")), new BigDecimal(map.get("remain_amount")));
        if (!(is > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }


    public int putOffShelves(long id, BigDecimal amount) {
        return advertiseDao.putOffAdvertise(id, amount);
    }

    public List<Advertise> getAllPutOnAdvertis(Long memberId) {
        return advertiseDao.findAllByMemberIdAndStatus(memberId, AdvertiseControlStatus.PUT_ON_SHELVES);
    }

    public Page<Advertise> findAll(com.querydsl.core.types.Predicate predicate, Pageable pageable) {
        return advertiseDao.findAll(predicate, pageable);
    }

    public Special<ScanAdvertise> getLatestAdvertise() throws SQLException, DataException {
        Special<ScanAdvertise> special = new Special<>();
        String sql = "SELECT\n" +
                "\ta.*, \n" +
//                "\ta.*, (\n" +
//                "\t\tCASE a.price_type\n" +
//                "\t\tWHEN 0 THEN\n" +
//                "\t\t\ta.price\n" +
//                "\t\tELSE\n" +
//                "\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
//                "\t\tEND\n" +
//                "\t) finalPrice,\n" +
                "\tb.avatar,\n" +
                "\tb.username,\n" +
                "\tb.member_level,\n" +
                "\tb.transactions\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "JOIN member b ON a.member_id = b.id\n" +
                "AND a.`status` = 0\n" +    //TODO
                "ORDER BY\n" +
                "\ta.id desc\n" +
                "LIMIT 0,\n" +
                " 10";
        List<Map<String, String>> list = DB.query(sql);
        if (null != list && list.size() > 0) {
            special.setContext(
                    list.stream().map((Map<String, String> x) ->
                                    ScanAdvertise.builder()
                                            .premiseRate(Integer.parseInt(x.get("price_type")) == 0? null:BigDecimal.valueOf(Double.valueOf(x.get("premise_rate"))))
                                            .price(BigDecimalUtils.round(Double.valueOf(x.get("price")), 2))
                                            .transactions(Integer.parseInt(x.get("transactions")))
                                            .remainAmount(BigDecimal.valueOf(Double.valueOf(x.get("remain_amount"))))
                                            .unit(x.get("coin_unit"))
//                                    .unit(otcCoin.getUnit())
                                            .payMode(x.get("pay_mode"))
                                            .memberName(x.get("username"))
                                            .avatar(x.get("avatar"))
                                            .minLimit(BigDecimal.valueOf(Double.valueOf(x.get("min_limit"))))
                                            .maxLimit(BigDecimal.valueOf(Double.valueOf(x.get("max_limit"))))
//                                    .coinNameCn(otcCoin.getNameCn())
                                            .level(Integer.parseInt(x.get("member_level")))
//                                    .coinId(otcCoin.getId())
                                            .coinId(Integer.parseInt(x.get("coin_id")))
//                                    .coinName(otcCoin.getName())
                                            .advertiseId(Long.valueOf(x.get("id")))
                                            .createTime(DateUtil.strToDate(x.get("create_time")))
                                            .advType(Integer.parseInt(x.get("advertise_type")))
                                            .build()
                    ).collect(Collectors.toList()));
        }
        return special;
    }

    public int countByMemberAndStatus(Member member,AdvertiseControlStatus status){
        return advertiseDao.countAllByMemberAndStatus(member,status);
    }

    public int alterTopBatch(Integer top, Long id) {
        return advertiseDao.alterTopBatch(top,DateUtil.getCurrentDate(),id);
    }
}
