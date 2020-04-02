package cn.ztuo.bitrade.dto;

import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberWallet;
import lombok.Data;

import java.util.List;

@Data
public class MemberDTO {

    private Member member ;

    private List<MemberWallet> list ;

}
