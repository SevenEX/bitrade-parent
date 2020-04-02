package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @description: MemberApiKey
 * @author: MrGao
 * @create: 2019/05/07 10:36
 */
@Entity
@Data
public class MemberApiKey {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id ;

    private Long memberId ;

    private String apiKey ;

    private String secretKey ;
    /**
     * 绑定ip 多个以逗号分割
     */
    private String bindIp ;

    private String apiName ;

    private String remark ;

    /**
     * 权限（0、读取、1、提币、2、交易 多个以逗号分割）
     */
    private String powerLimit ;

    private Date expireTime;

    private Date createTime;

    private CommonStatus status ;

    public MemberApiKey() {
    }

    public MemberApiKey(Long memberId, String apiKey, String bindIp, String apiName, String remark,
                        Date expireTime, Long id,Date createTime,String powerLimit,CommonStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.apiKey = apiKey;
        this.bindIp = bindIp;
        this.apiName = apiName;
        this.remark = remark;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.powerLimit = powerLimit;
        //this.statusStr = CommonStatus.valueOf(status);
        this.status = status ;
    }

    public MemberApiKey(Long memberId, String apiKey, String secretKey, String bindIp, String apiName, String remark,
                        Date expireTime, Long id,Date createTime,String powerLimit) {
        this.id = id;
        this.memberId = memberId;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.bindIp = bindIp;
        this.apiName = apiName;
        this.remark = remark;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.powerLimit = powerLimit;
    }

    public MemberApiKey(Long memberId, String apiKey, String bindIp, String apiName, String remark, Date expireTime,
                        Long id,Date createTime,String powerLimit) {
        this.id = id;
        this.memberId = memberId;
        this.apiKey = apiKey;
        this.bindIp = bindIp;
        this.apiName = apiName;
        this.remark = remark;
        this.expireTime = expireTime;
        this.createTime = createTime;
        this.powerLimit = powerLimit;
    }

    public MemberApiKey(Long memberId, String bindIp, String apiName, String remark,Date createTime) {
        this.memberId = memberId;
        this.bindIp = bindIp;
        this.apiName = apiName;
        this.remark = remark;
        this.createTime = createTime;
    }
}
