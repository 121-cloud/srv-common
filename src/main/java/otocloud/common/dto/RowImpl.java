package otocloud.common.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
class RowImpl implements Row {
    private FieldTable fieldTable;
    private List<Cell> cells;

    private Object instance;
    private Field field;

    public RowImpl(FieldTable fieldTable) {
        this.fieldTable = fieldTable;
        this.cells = new ArrayList<>();
    }

    void add(Cell cell) {
        this.cells.add(cell);
    }

    @Override
    public FieldTable table() {
        return this.fieldTable;
    }

    @Override
    public List<Cell> cells() {
        return this.cells;
    }

    @Override
    public Cell cell(int index) {
        return this.cells.get(index);
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public Object instance() {
        return this.instance;
    }

    @Override
    public Field field() {
        return this.field;
    }
}
