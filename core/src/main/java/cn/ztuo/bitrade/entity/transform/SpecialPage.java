package cn.ztuo.bitrade.entity.transform;

import lombok.Data;

import java.util.List;

/**
 *
 * @author Seven
 * @date 2019年01月15日
 */
@Data
public class SpecialPage<E> {

    private List<E> context;
    private int currentPage;
    private int totalPage;
    private int pageNumber;
    private int totalElement;
}
