package otocloud.common.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * 存储一个字段表对应的列。
 * <p>
 * zhangyef@yonyou.com on 2015-11-03.
 */
class ColumnImpl<T> implements Column<T> {
    private FieldTable fieldTable;

    private List<Cell<T>> cells;

    public ColumnImpl(FieldTable fieldTable) {
        this.fieldTable = fieldTable;
        this.cells = new LinkedList<>();
    }

    void add(Cell<T> cell) {
        this.cells.add(cell);
    }

    @Override
    public FieldTable table() {
        return this.fieldTable;
    }

    @Override
    public List<Cell<T>> cells() {
        return this.cells;
    }
}
