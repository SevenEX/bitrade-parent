package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Zane
 * @description
 * @date 2017/12/29 14:14
 */
@Entity
@Data
@Table(name = "coin_chain_relation")
public class CoinChainRelation {
    /**
     * 币种唯一标识
     */
    @Id
    private String coinKey;
    /**
     * 链ID
     */
    private String chainId;

    /**
     * 币种名称
     */
    private String coinName;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "coin_id")
    private Coin coin;
}
