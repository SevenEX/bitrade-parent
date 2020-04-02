package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.CoinChainRelation;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MrGao
 * @description 货币操作
 * @date 2017/12/29 14:41
 */
@Repository
public interface CoinChainRelationDao extends BaseDao<CoinChainRelation> {

    /**
     * @param coinKey 唯一键
     * @return 币种多链关系
     */
    CoinChainRelation findByCoinKey(String coinKey);
}
