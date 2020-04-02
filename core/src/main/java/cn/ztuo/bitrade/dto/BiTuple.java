package cn.ztuo.bitrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BiTuple<T1, T2> {
    private T1 first;
    private T2 second;
}
