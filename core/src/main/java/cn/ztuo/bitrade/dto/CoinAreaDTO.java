package cn.ztuo.bitrade.dto;

import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:14
 */
@Data
@AllArgsConstructor
@Builder
public class CoinAreaDTO {
    private Long id;

    private String cnName;

    private int sort;

    private CommonStatus status;

    private String arName;
    private String enName;
    private String jaName;
    private String koName;
}
