package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;

/**
 * 返回第一个元素。
 * 如果找不到该字段，则抛出异常。
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class FirstFetcher extends AbstractFetcher {
    @Override
    public Row fetch(List<Row> rows) throws NoSuchFieldException {
        if (rows == null || rows.size() < 1) {
            throw new NoSuchFieldException("字段集合中没有元素。");
        }

        return rows.get(0);
    }
}
