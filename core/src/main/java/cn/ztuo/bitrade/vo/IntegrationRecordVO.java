package cn.ztuo.bitrade.vo;

import cn.ztuo.bitrade.constant.IntegrationRecordType;
import lombok.Data;

/**
 * @description: IntegrationRecordVO
 * @author: MrGao
 * @create: 2019/04/25 19:31
 */
@Data
public class IntegrationRecordVO extends BaseQueryVO {


    private Long userId;

    private IntegrationRecordType type;

    private String createStartTime ;

    private String createEndTime ;


}
