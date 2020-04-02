package cn.ztuo.bitrade.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * 返佣榜单ID
 * @author Seven
 * @date 2019年03月08日
 */
public class RewardStatisticsId implements Serializable {

    private Member orderMember;
    private String createTime;

}
