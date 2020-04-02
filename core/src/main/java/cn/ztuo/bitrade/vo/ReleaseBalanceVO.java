package cn.ztuo.bitrade.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ReleaseBalanceVO {

    // 用户id
    private List<Long> id;

    // 会员名称
    private String memberName;

    // 手机号
    private String phone;

    // 查询开始时间
    private Date startTime;

    // 查询结束时间
    private Date endTime;

    //审核状态
    private String releaseState;

    private Integer pageNo;

    private Integer pageSize;
}
