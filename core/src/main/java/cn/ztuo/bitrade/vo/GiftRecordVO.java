package cn.ztuo.bitrade.vo;

import lombok.Data;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/29 11:16 AM
 */
@Data
public class GiftRecordVO extends BaseQueryVO {

    private String startTime;

    private String endTime;

    private Long userId;

    private String userName;

    private String mobile;


}
