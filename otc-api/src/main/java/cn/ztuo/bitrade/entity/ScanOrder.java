package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.AdvertiseType;
import cn.ztuo.bitrade.constant.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Seven
 * @date 2019年01月19日
 */
@Builder
@Data
public class ScanOrder {
    private String orderSn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String unit;
    private AdvertiseType type;
    private String name;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal commission;
    private BigDecimal amount;
    private OrderStatus status;
    private Long memberId;
    private String avatar;
    private String realName;

    public static ScanOrder toScanOrder(Order order, Long id) {
        ScanOrder scanOrder = ScanOrder.builder().orderSn(order.getOrderSn())
                .createTime(order.getCreateTime())
                .unit(order.getCoin().getUnit())
                .price(order.getPrice())
                .amount(order.getNumber())
                .money(order.getMoney())
                .status(order.getStatus())
                .commission(id.equals(order.getMemberId())?order.getCommission():BigDecimal.ZERO)
                .name(order.getCustomerId().equals(id) ? order.getMemberName() : order.getCustomerName())
                .memberId(order.getCustomerId().equals(id) ? order.getMemberId():order.getCustomerId())
                .type(judgeType(order.getAdvertiseType(), order, id))
                .realName(order.getCustomerId().equals(id) ? order.getMemberRealName() : order.getCustomerRealName())
                .build();
        if (id.equals(order.getMemberId())){
            scanOrder.setCommission(order.getCommission());
        }else {
            scanOrder.setCommission(BigDecimal.ZERO);
        }
//        if (order.getAdvertiseType().equals(AdvertiseType.SELL)){
//            if (id.equals(order.getMemberId())){
//                scanOrder.setCommission(order.getCommission());
//            }else {
//                scanOrder.setCommission(BigDecimal.ZERO);
//
//            }
//        }else {
//            if (id.equals(order.getCustomerId())){
//                scanOrder.setCommission(order.getCommission());
//            }else {
//                scanOrder.setCommission(BigDecimal.ZERO);
//
//            }
//        }
        return scanOrder;
    }


    public static AdvertiseType judgeType(AdvertiseType type, Order order, Long id) {
        if (type.equals(AdvertiseType.BUY) && id.equals(order.getMemberId())) {
            return AdvertiseType.BUY;
        } else if (type.equals(AdvertiseType.BUY) && id.equals(order.getCustomerId())) {
            return AdvertiseType.SELL;
        } else if (type.equals(AdvertiseType.SELL) && id.equals(order.getCustomerId())) {
            return AdvertiseType.BUY;
        } else  {
            return AdvertiseType.SELL;
        }
    }
}
