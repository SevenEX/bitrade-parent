package cn.ztuo.bitrade.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Data
public class ChannelVO {
    @Column(name = "memberId")
    private Long memberId;
    /**
     * 渠道累计奖励
     */
    @Column(name = "channelReward")
    private BigDecimal channelReward=BigDecimal.ZERO;
    /**
     * 推广人数
     */
    @Column(name = "channelCount")
    private Integer channelCount=0;

    public ChannelVO(Long memberId,Integer channelCount,BigDecimal channelReward){
        this.memberId=memberId;
        this.channelCount=channelCount;
        this.channelReward=channelReward;
    }

    public ChannelVO(){
        super();
    }
}
