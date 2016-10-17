package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;

/**
 * 取出第一个不为null的字段。
 * 如果找不到该字段，抛出异常。
 * <p>
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class FirstNotNullFetcher extends AbstractFetcher {
    @Override
    public Row fetch(List<Row> rows) throws NoSuchFieldException {
        Row row = null;
        if (rows == null || rows.size() < 1) {
            throw new NoSuchFieldException("字段集合中没有元素。");
        }

        for (Row item : rows) {
            if (get(item.instance(), item.field()) != null) {
                row = item;
                break;
            }
        }

        if (row == null) throw new NoSuchFieldException("所有字段都为null。");

        return row;
    }
}
