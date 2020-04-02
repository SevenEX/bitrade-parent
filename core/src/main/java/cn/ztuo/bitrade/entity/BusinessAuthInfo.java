package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BusinessAuthInfo {

    private String name;
    private String telno;
    private String wechat;
    private String qq;
    private String mail;
    private String coinSymbol;
    private BigDecimal amount;
    private String assetData;
    private String tradeData;
    private String houseBook;//户口本
    private String houseCertificate;//房产证/租房合同/水电费清单(三选一)
    private String video;
    private String contactPerson;//紧急联系人
    private String contactPhone;//紧急联系人电话
    private String contactRelation;//紧急联系人与本人关系
    private String contactAddress;//紧急联系人与本人关系

}
