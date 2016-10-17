package otocloud.common.dto.fetch;

import otocloud.common.dto.Row;

import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
@FunctionalInterface
public interface Fetcher {
    Row fetch(List<Row> rows) throws NoSuchFieldException;
}
