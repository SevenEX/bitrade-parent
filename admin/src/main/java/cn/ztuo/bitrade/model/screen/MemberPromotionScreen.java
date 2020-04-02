package cn.ztuo.bitrade.model.screen;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberPromotionScreen extends AccountScreen{

    private int minPromotionNum = -1;

    private int maxPromotionNum = -1;
}
