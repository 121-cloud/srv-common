package otocloud.common.dto;

import java.lang.reflect.Field;
import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public interface Row {
    FieldTable table();

    List<Cell> cells();

    /**
     * @param index 从0开始的索引.
     * @return
     */
    Cell cell(int index);

    Object instance();

    Field field();
}
