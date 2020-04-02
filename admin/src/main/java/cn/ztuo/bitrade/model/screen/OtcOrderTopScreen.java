package cn.ztuo.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.OrderStatus;
import lombok.Data;

import java.util.Date;

@Data
public class OtcOrderTopScreen extends AccountScreen{

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
    private OrderStatus status;
    private String unit;
}
