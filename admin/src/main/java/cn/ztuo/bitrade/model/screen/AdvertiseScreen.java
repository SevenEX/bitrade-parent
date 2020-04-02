package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.AdvertiseControlStatus;
import cn.ztuo.bitrade.constant.AdvertiseType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdvertiseScreen extends AccountScreen{

    AdvertiseType advertiseType;

    String payMode ;

    String symbol ;

    /**
     * 广告状态 (012  上架/下架/关闭)
     */
    AdvertiseControlStatus status ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

}
