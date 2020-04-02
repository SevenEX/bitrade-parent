package cn.ztuo.bitrade.service.Base;

import cn.ztuo.bitrade.util.RsaKeyUtil;
import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cn.ztuo.bitrade.pagination.PageListMapResult;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.pagination.QueryDslContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/**
 * @author MrGao
 * @description
 * @date 2018/1/18 10:29
 */
@Component
@Slf4j
public class BaseService<T> extends TopBaseService {
    //JPA查询工厂
    @Autowired
    protected JPAQueryFactory queryFactory;

    /**
     * 与钱包通讯的私钥
     */
    @Value("${wallet.private.key}")
    private String walletPrivateKey;
    @Autowired
    protected LocaleMessageSourceService msService;


    /**
     * 查询列表
     *
     * @param pageNo             分页参数
     * @param pageSize           分页大小
     * @param predicateList      查询条件
     * @param entityPathBase     查询表
     * @param orderSpecifierList 排序条件
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<T> queryDsl(Integer pageNo, Integer pageSize, List<Predicate> predicateList, EntityPathBase<T> entityPathBase, List<OrderSpecifier> orderSpecifierList) {
        List<T> list;
        //查询表
        JPAQuery<T> jpaQuery = queryFactory.selectFrom(entityPathBase);
        //查询条件
        if (predicateList != null && predicateList.size() > 0)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        //排序方式
        if (orderSpecifierList != null && orderSpecifierList.size() > 0)
            jpaQuery.orderBy(orderSpecifierList.toArray(new OrderSpecifier[orderSpecifierList.size()]));
        //分页查询
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, pageNo, pageSize, jpaQuery.fetchCount());
    }

    /**
     * RSA私钥签名
     * @Title: sign
     * @return 设定文件
     * @return byte[]    返回类型
     * @lastModify 2019年4月22日
     */
    public String sign(TreeMap<String, String> map) {
        map.put("nonce", UUID.randomUUID().toString());
        String timeStamp = String.valueOf(System.currentTimeMillis());
        map.put("timestamp",timeStamp);
        map.put("sysId","app");
        String data= JSON.toJSONString(map);
        byte[] privateKey = RsaKeyUtil.decryptBASE64(walletPrivateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey2);
            signature.update(data.getBytes("utf-8"));
            String signatureStr = URLEncoder.encode(RsaKeyUtil.encryptBASE64(signature.sign()));
            StringBuilder sb = new StringBuilder();
            sb.append("?sign=").append(signatureStr);
            map.forEach((k,v) ->{
                sb.append("&").append(k).append("=").append(v);
            });
            log.info(data + "访问参数：" + sb);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询单个
     *
     * @param predicate      查询条件
     * @param entityPathBase 查询表
     * @return
     */
    @Transactional(readOnly = true)
    public T queryOneDsl(Predicate predicate, EntityPathBase<T> entityPathBase) {
        return queryFactory.selectFrom(entityPathBase).where(predicate).fetchFirst();
    }

    //多表联合查询

    /**
     * @param expressions        查询列表
     * @param entityPaths        查询表
     * @param predicates         条件
     * @param orderSpecifierList 排序
     * @param pageNo             页码
     * @param pageSize           页面大小
     */
    @Transactional(readOnly = true)
    public PageListMapResult queryDslForPageListResult(
            List<Expression> expressions,
            List<EntityPath> entityPaths,
            List<Predicate> predicates,
            List<OrderSpecifier> orderSpecifierList,
            Integer pageNo,
            Integer pageSize) {
        JPAQuery<Tuple> jpaQuery = queryFactory.select(expressions.toArray(new Expression[expressions.size()]))
                .from(entityPaths.toArray(new EntityPath[entityPaths.size()]))
                .where(predicates.toArray(new Predicate[predicates.size()]));
        List<Tuple> tuples = jpaQuery.orderBy(orderSpecifierList.toArray(new OrderSpecifier[orderSpecifierList.size()]))
                .offset((pageNo - 1) * pageSize).limit(pageSize)
                .fetch();
        List<Map<String, Object>> list = new LinkedList<>();//返回结果
        //封装结果
        for (int i = 0; i < tuples.size(); i++) {
            //遍历tuples
            Map<String, Object> map = new LinkedHashMap<>();//一条信息
            for (Expression expression : expressions) {
                map.put(expression.toString().split(" as ")[1],//别名作为Key
                        tuples.get(i).get(expression));//获取结果
            }
            list.add(map);
        }
        PageListMapResult pageListMapResult = new PageListMapResult(list, pageNo, pageSize, jpaQuery.fetchCount());//分页封装
        return pageListMapResult;
    }

    @Transactional(readOnly = true)
    public PageListMapResult queryDslForPageListResult(QueryDslContext qdc, Integer pageNo, Integer pageSize) {
        JPAQuery<Tuple> jpaQuery = queryFactory.select(qdc.expressionToArray())
                .from(qdc.entityPathToArray())
                .where(qdc.predicatesToArray());
        List<Tuple> tuples = jpaQuery.orderBy(qdc.orderSpecifiersToArray())
                .offset((pageNo - 1) * pageSize).limit(pageSize)
                .fetch();
        List<Map<String, Object>> list = new LinkedList<>();//返回结果
        //封装结果
        for (int i = 0; i < tuples.size(); i++) {
            //遍历tuples
            Map<String, Object> map = new LinkedHashMap<>();//一条信息
            for (Expression expression : qdc.getExpressions()) {
                map.put(expression.toString().split(" as ")[1],//别名作为Key
                        tuples.get(i).get(expression));//获取结果
            }
            list.add(map);
        }
        PageListMapResult pageListMapResult = new PageListMapResult(list, pageNo, pageSize, jpaQuery.fetchCount());//分页封装
        return pageListMapResult;
    }
}
