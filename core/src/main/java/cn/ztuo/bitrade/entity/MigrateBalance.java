package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.math.BigDecimal;

@Entity
@Data
@IdClass(MigrateBalance.MigrateBalanceKey.class)
public class MigrateBalance implements java.io.Serializable {
    @Id
    private Long foreignId;
    @Id
    private String tokenId;
    private String tokenName;
    private BigDecimal total;
    private BigDecimal free;
    private BigDecimal locked;
    private BigDecimal position;

    @Data
    public static class MigrateBalanceKey implements java.io.Serializable {
        private Long foreignId;
        private String tokenId;
    }
}
