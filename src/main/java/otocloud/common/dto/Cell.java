package otocloud.common.dto;

/**
 * 查找表的单元格。
 * zhangyef@yonyou.com on 2015-11-03.
 *
 * @see FieldTable
 */
interface Cell<T> {
    FieldTable table();

    Row row();

    Column column();

    T data();
}
