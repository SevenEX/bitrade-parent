package cn.ztuo.bitrade.model.screen;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OtcOrderExcelScreen extends OtcOrderTopScreen{

    private Long memberId ;

    private Long customerId ;
}
