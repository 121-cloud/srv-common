package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;
import java.util.ListIterator;

/**
 * 取出最后一个为空的字段。
 * 如果没有该字段，抛出异常。
 * zhangyef@yonyou.com on 2015-11-03.
 *
 * @see NoSuchFieldException
 */
public class LastNullFetcher extends AbstractFetcher {
    @Override
    public Row fetch(List<Row> rows) throws NoSuchFieldException {
        if (rows == null || rows.size() < 1) {
            throw new NoSuchFieldException("字段集合中没有元素。");
        }
        ListIterator<Row> itr = rows.listIterator(rows.size());
        while (itr.hasPrevious()) {
            Row row = itr.previous();
            if (get(row.instance(), row.field()) == null) {
                return row;
            }
        }

        throw new NoSuchFieldException("没有为null的字段。");
    }
}
