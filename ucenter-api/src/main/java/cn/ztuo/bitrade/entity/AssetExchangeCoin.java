package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table
public class AssetExchangeCoin {
    @Id
    private Long id;
    private String fromUnit;
    private String toUnit;
    private BigDecimal exchangeRate;
}
