package cn.ztuo.bitrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtcOrderCount {
    private long count30;
    private long successCount30;
}
