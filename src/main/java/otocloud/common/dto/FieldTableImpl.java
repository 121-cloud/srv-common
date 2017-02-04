/*package otocloud.common.dto;

import com.google.common.collect.ComparisonChain;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

*//**
 * Field名称(0)、Field类型(1)、Field对象(2)、来源Class(3)、来源类实例(4)。
 * String, Class, Field, Class, Object
 * <p>
 * zhangyef@yonyou.com on 2015-11-03.
 *//*
class FieldTableImpl implements FieldTable {

    private int columnNum;

    private ColumnImpl[] columns;
    private List<Row> rows;

    public FieldTableImpl() {
        initColumns(5);

        this.rows = new LinkedList<>();
    }

    private void initColumns(int columnNum) {
        this.columnNum = columnNum;
        this.columns = new ColumnImpl[this.columnNum];

        this.columns[0] = new ColumnImpl<String>(this);
        this.columns[1] = new ColumnImpl<Class>(this);
        this.columns[2] = new ColumnImpl<Field>(this);
        this.columns[3] = new ColumnImpl<Class>(this);
        this.columns[4] = new ColumnImpl<>(this);
    }

    *//**
     * 插入行数据。
     * 每插入一行数据，都会新建一个{@linkplain Row}，但不会新建{@linkplain Column}。
     *
     * @param field    字段.
     * @param instance 字段所在实例.
     * @return 新插入的行.
     *//*
    @Override
    public Row insert(Field field, Object instance) {
        String fieldName = field.getName(); //column-0
        Class<?> fieldType = field.getType(); //column-1
        Field fromField = field; //column-2
        Class<?> fromClass = field.getDeclaringClass(); //column-3
        Object fromInstance = instance; //column-4

        RowImpl row = new RowImpl(this);

        new CellImpl<String>(row, columns[0]).setData(fieldName);
        new CellImpl<Class>(row, columns[1]).setData(fieldType);
        new CellImpl<Field>(row, columns[2]).setData(fromField);
        new CellImpl<Class>(row, columns[3]).setData(fromClass);
        new CellImpl<>(row, columns[4]).setData(fromInstance);

        row.setField(field);
        row.setInstance(instance);

        this.rows.add(row);

        return row;
    }

    *//**
     * {@inheritDoc}
     * <p>
     * 查找fieldType所指定的类型或其子类型的字段。
     *
     * @param fieldName 字段名称.
     * @param fieldType 字段类型.
     * @return
     *//*
    @Override
    public List<Row> find(String fieldName, Class fieldType) {
        return this.rows.stream().filter(row -> {
            Field field = (Field) row.cell(2).data();
            if (!field.getName().equals(fieldName)) {
                return false;
            }

            if (fieldType.isAssignableFrom(field.getType())) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    *//**
     * {@inheritDoc}
     *
     * @param name      字段名.
     * @param fieldType 字段类型.
     * @param clazz     来源类的类型.
     * @return
     *//*
    @Override
    public List<Row> find(String name, Class fieldType, Class clazz) {

        return this.rows.stream().filter(row -> {

            Field field = (Field) row.cell(2).data();
            Class fromClazz = (Class) row.cell(3).data();

            //左侧类是右侧类的子类，或等于右侧类。
            Comparator<Class> comparator = (Class left, Class right) -> {
                if (right.isAssignableFrom(left)) {
                    return 0;
                }
                return 1;
            };

            return ComparisonChain.start()
                    .compare(field.getName(), name)
                    .compare(field.getType(), fieldType, comparator) //比较类的名称
                    .compare(fromClazz, clazz, comparator) //比较类的名称
                    .result() == 0;

        }).collect(Collectors.toList());
    }


    @Override
    public List<Row> rows() {
        return this.rows;
    }

    @Override
    public List<Column> columns() {
        return Arrays.asList(this.columns);
    }
}
*/