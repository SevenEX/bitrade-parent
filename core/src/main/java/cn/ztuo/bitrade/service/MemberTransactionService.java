package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.util.DateUtil;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.MemberTransactionDao;
import cn.ztuo.bitrade.entity.MemberTransaction;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.QMemberTransaction;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.vo.MemberTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MemberTransactionService extends BaseService {
    @Autowired
    private MemberTransactionDao transactionDao;
    @Autowired
    private MemberWalletService walletService;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicates
     * @param pageModel
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<MemberTransaction> queryWhereOrPage(List<Predicate> predicates, PageModel pageModel) {
        List<MemberTransaction> list;
        JPAQuery<MemberTransaction> jpaQuery = queryFactory.selectFrom(QMemberTransaction.memberTransaction);
        jpaQuery.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        if (predicates != null)
            jpaQuery.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        jpaQuery.orderBy(QMemberTransaction.memberTransaction.createTime.desc());
        if (pageModel.getPageNo() != null && pageModel.getPageSize() != null) {
            list = jpaQuery.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 保存交易记录
     *
     * @param transaction
     * @return
     */
    public MemberTransaction save(MemberTransaction transaction) {
        return transactionDao.saveAndFlush(transaction);
    }

    public List<MemberTransaction> findAll() {
        return transactionDao.findAll();
    }


    public MemberTransaction findOne(Long id) {
        return transactionDao.findById(id).orElse(null);
    }


    public List findAllByWhere(Date startTime, Date endTime, TransactionType type, Long memberId) {
        QMemberTransaction qMemberTransaction = QMemberTransaction.memberTransaction;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        if (startTime != null)
            booleanExpressionList.add(qMemberTransaction.createTime.gt(startTime));
        if (endTime != null)
            booleanExpressionList.add(qMemberTransaction.createTime.lt(endTime));
        if (type != null)
            booleanExpressionList.add(qMemberTransaction.type.eq(type));
        if (memberId != null)
            booleanExpressionList.add(qMemberTransaction.memberId.eq(memberId));
        return queryFactory.selectFrom(qMemberTransaction).
                where(booleanExpressionList.toArray(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()])))
                .fetch();
    }

    public Page<MemberTransaction> queryByMember(Long uid, Integer pageNo, Integer pageSize, TransactionType type,String unit) {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize, orders);
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("type", type, false));
        if(unit!=null){
            specification.add(Restrictions.eq("symbol",unit,false));
        }
        return transactionDao.findAll(specification, pageRequest);
    }

    public Page<MemberTransaction> queryByMember(Long uid, Integer pageNo, Integer pageSize, TransactionType type, String startDate, String endDate, String symbol) throws ParseException {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize, orders);
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        if (type != null) {
            specification.add(Restrictions.eq("type", type, false));
        }
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            specification.add(Restrictions.gte("createTime", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(startDate ), false));
            specification.add(Restrictions.lte("createTime", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(endDate), false));
        }
        if (StringUtils.isNotBlank(symbol))
            specification.add(Restrictions.eq("symbol", symbol, false));
        return transactionDao.findAll(specification, pageRequest);
    }

    public Page<MemberTransaction> queryByMember(Long uid, Integer pageNo, Integer pageSize, List<TransactionType> type, String startDate, String endDate, String symbol) throws ParseException {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize, orders);
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        if (type != null && type.size() > 0) {
            specification.add(Restrictions.in("type", type, false));
        }
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            specification.add(Restrictions.gte("createTime", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(startDate ), false));
            specification.add(Restrictions.lte("createTime", DateUtil.YYYY_MM_DD_MM_HH_SS.parse(endDate), false));
        }
        if (StringUtils.isNotBlank(symbol))
            specification.add(Restrictions.eq("symbol", symbol, false));
        return transactionDao.findAll(specification, pageRequest);
    }

    public List<Map<String, Object>> findTransactionSum(Long uid,TransactionType type) {
        List<TransactionType> types = new ArrayList<>();
        types.add(type);
        List<Map<String, Object>> results = transactionDao.findTransactionSum(uid, types);
        return results;
    }

    public List<MemberTransaction> findMatchTransaction(Long uid, String symbol) {
        Sort orders = Criteria.sortStatic("createTime.asc");
        //查询条件
        Criteria<MemberTransaction> specification = new Criteria<MemberTransaction>();
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("flag", 0, false));
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.gt("amount", 0, false));
        List<TransactionType> types = new ArrayList<>();
        types.add(TransactionType.RECHARGE);
        types.add(TransactionType.EXCHANGE);
        types.add(TransactionType.ADMIN_RECHARGE);
        specification.add(Restrictions.in("type", types, false));
        List<MemberTransaction> transactions = transactionDao.findAll(specification, orders);
        return transactions;
    }

    @Transactional
    public void matchWallet(Long uid, String symbol, BigDecimal amount) {
        List<MemberTransaction> transactions = findMatchTransaction(uid, symbol);
        BigDecimal deltaAmount = BigDecimal.ZERO;
        MemberWallet gccWallet = walletService.findByCoinUnitAndMemberId("GCC", uid);
        MemberWallet gcxWallet = walletService.findByCoinUnitAndMemberId("GCX", uid);

        for (MemberTransaction transaction : transactions) {
            if (amount.compareTo(deltaAmount) > 0) {
                BigDecimal amt = amount.subtract(deltaAmount).compareTo(transaction.getAmount()) > 0 ? transaction.getAmount() : amount.subtract(deltaAmount);
                deltaAmount = deltaAmount.add(amt);
                transaction.setFlag(1);
            } else break;
        }

        gccWallet.setBalance(gccWallet.getBalance().subtract(deltaAmount));
        gcxWallet.setBalance(gcxWallet.getBalance().add(deltaAmount));

        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(deltaAmount);
        transaction.setSymbol(gcxWallet.getCoin().getUnit());
        transaction.setAddress(gcxWallet.getAddress());
        transaction.setMemberId(gcxWallet.getMemberId());
        transaction.setType(TransactionType.MATCH);
        transaction.setFee(BigDecimal.ZERO);
        //保存配对记录
        save(transaction);
        if (gccWallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            gccWallet.setBalance(BigDecimal.ZERO);
        }
    }

    public boolean isOverMatchLimit(String day, double limit) throws Exception {
        BigDecimal totalAmount;
        Date date1 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day + " 00:00:00");
        Date date2 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day + " 23:59:59");
        Map<String, Object> result = transactionDao.findMatchTransactionSum("GCX", TransactionType.MATCH, date1, date2);
        if (result != null && result.containsKey("amount")) {
            totalAmount = new BigDecimal(result.get("amount").toString());
        } else {
            totalAmount = BigDecimal.ZERO;
        }
        log.info("totalAmount:" + totalAmount);
        return totalAmount.doubleValue() >= limit;
    }

    public BigDecimal findMemberDailyMatch(Long uid, String day) throws Exception {
        Date date1 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day + " 00:00:00");
        Date date2 = DateUtil.YYYY_MM_DD_MM_HH_SS.parse(day + " 23:59:59");
        Map<String, Object> result = transactionDao.findMatchTransactionSum(uid, "GCX", TransactionType.MATCH, date1, date2);
        if (result != null && result.containsKey("amount")) {
            return new BigDecimal(result.get("amount").toString());
        } else return BigDecimal.ZERO;
    }

    public Page<MemberTransactionVO> joinFind(List<Predicate> predicates, PageModel pageModel) {
        JPAQuery<MemberTransactionVO> query = queryFactory.select(Projections.fields(MemberTransactionVO.class,
                QMemberTransaction.memberTransaction.address,
                QMemberTransaction.memberTransaction.amount,
                QMemberTransaction.memberTransaction.createTime.as("createTime"),
                QMemberTransaction.memberTransaction.fee,
                QMemberTransaction.memberTransaction.flag,
                QMemberTransaction.memberTransaction.id.as("id"),
                QMemberTransaction.memberTransaction.symbol,
                QMemberTransaction.memberTransaction.type,
                QMember.member.username.as("memberUsername"),
                QMember.member.mobilePhone.as("phone"),
                QMember.member.email,
                QMember.member.realName.as("memberRealName"),
                QMember.member.id.as("memberId")))
                .from(QMemberTransaction.memberTransaction, QMember.member);
        predicates.add(QMemberTransaction.memberTransaction.memberId.eq(QMember.member.id));
        query.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(QMemberTransaction.memberTransaction.createTime.desc());
        List<MemberTransactionVO> list = query.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(list, pageModel.getPageable(), total);
    }

    public void save(List<MemberTransaction> list) {
        transactionDao.saveAll(list);
    }
    /**
     * 查询币币交易分红记录
     * @param beginDate
     * @param endDate
     * @return
     */
    public  List<MemberTransaction> findAllByCreateTime(String beginDate,String endDate){
        return transactionDao.findAllByCreateTime(beginDate,endDate);
    }
}
