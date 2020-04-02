package cn.ztuo.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MemberDepositVO {

    private Long id ;

    private Long memberId ;

    private String username ;

    private String unit ;

    private String address ;

    private BigDecimal amount ;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;

    private String txid;
}
