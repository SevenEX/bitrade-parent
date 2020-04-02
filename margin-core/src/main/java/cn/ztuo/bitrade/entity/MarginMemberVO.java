package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class MarginMemberVO {
    private Long memberId;
    private Long leverCoinId;
    private InspectBean inspectBean;

    public MarginMemberVO(Long memberId,Long leverCoinId){
        this.memberId=memberId;
        this.leverCoinId=leverCoinId;
    }

    public MarginMemberVO(){
        super();
    }
}

