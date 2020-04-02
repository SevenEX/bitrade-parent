package cn.ztuo.bitrade.vo;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class ImportXmlVO {
    private Long memberId;
    private String memberName;
    private String phone;
    private String coinName;
    private String coinUnit;
    private Double amount;
}
