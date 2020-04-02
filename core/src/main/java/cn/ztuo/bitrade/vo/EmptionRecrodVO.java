package cn.ztuo.bitrade.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 4:32 PM
 */
@Data
public class EmptionRecrodVO extends BaseQueryVO{


    private Long userId;

    private String startTime;

    private String endTime;

    private String userName;

    private String userMobile;
    /**
     * 项目名称
     * This field corresponds to the database column emption_record.ieo_name
     *
     * @date 2019-04-26 15:22:41
     */
    private String ieoName;

    /**
     * 状态 (0-失败，1-成功)
     * This field corresponds to the database column emption_record.status
     *
     * @date 2019-04-26 15:22:41
     */
    private String status;


}
