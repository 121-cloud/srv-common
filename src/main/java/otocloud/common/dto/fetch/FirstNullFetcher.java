package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;

/**
 * 获取第一个为null的字段。
 * 如果找不到该字段，则抛出异常。
 * <p>
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class FirstNullFetcher extends AbstractFetcher {

    @Override
    public Row fetch(List<Row> rows) throws NoSuchFieldException{
        Row row = null;
        if (rows == null || rows.size() < 1) {
            throw new NoSuchFieldException("字段集合中没有元素。");
        }

        for (Row item : rows) {
            if (get(item.instance(), item.field()) == null) {
                row = item;
                break;
            }
        }

        if(row == null) throw new NoSuchFieldException("没有为null的字段。");

        return row;
    }
}
