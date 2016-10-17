package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class LastFetcher extends AbstractFetcher {
    @Override
    public Row fetch(List<Row> rows) throws NoSuchFieldException {

        if(rows == null || rows.size() < 1){
            throw new NoSuchFieldException("字段集合中没有元素。");
        }

        return rows.get(rows.size());
    }
}
