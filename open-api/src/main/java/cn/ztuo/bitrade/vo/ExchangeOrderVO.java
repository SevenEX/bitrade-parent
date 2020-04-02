package cn.ztuo.bitrade.vo;

import cn.ztuo.bitrade.entity.ExchangeOrderDirection;
import cn.ztuo.bitrade.entity.ExchangeOrderType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: ExchangeOrderVO
 * @author: MrGao
 * @create: 2019/05/07 15:41
 */
@Data
public class ExchangeOrderVO {

    private Long memberId ;

    private ExchangeOrderDirection direction ;

    private String symbol ;

    private BigDecimal price ;

    private BigDecimal amount ;

    private ExchangeOrderType type ;


}
