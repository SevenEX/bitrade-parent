package cn.ztuo.bitrade.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 4:32 PM
 */
@Data
public class IeoEmptionVO extends BaseQueryVO{


    private String startTime;

    private String endTime;

    /**
     * 1-预热中，2-进行中，3-已结束
     */
    private String status;

    private Long id;


}
