package otocloud.common.dto;

import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
interface Column<T> {
    FieldTable table();

    List<Cell<T>> cells();
}
