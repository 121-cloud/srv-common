package otocloud.common.dto;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
class CellImpl<T> implements Cell<T> {
    private FieldTable fieldTable;
    private RowImpl row;
    private ColumnImpl column;

    private T data;

    public CellImpl(RowImpl row, ColumnImpl column) {
        this.fieldTable = row.table();
        this.row = row;
        this.column = column;

        this.row.add(this);
        this.column.add(this);
    }

    @Override
    public FieldTable table() {
        return this.fieldTable;
    }

    @Override
    public Row row() {
        return this.row;
    }

    @Override
    public Column column() {
        return this.column;
    }

    void setData(T data) {
        this.data = data;
    }

    @Override
    public T data() {
        return this.data;
    }
}
