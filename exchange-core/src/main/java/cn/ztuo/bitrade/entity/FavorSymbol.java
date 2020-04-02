package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="exchange_favor_symbol")
public class FavorSymbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private Long memberId;
    private String addTime;
}
