package otocloud.common.dto;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 字段查找表。
 * 表头结构为 “Field名称(0)、Field类型(1)、Field对象(2)、来源Class(3)、来源类实例(4)”。
 * 一个字段对应一个行，因此，本类中称一个“行(hang)”为一个字段行。
 * <p>
 * zhangyef@yonyou.com on 2015-11-03.
 */
interface FieldTable {

    /**
     * 插入一个字段到字段查找表中。
     *
     * @param field
     * @param instance
     * @return
     */
    Row insert(Field field, Object instance);

    /**
     * 处理多个类当中具有“同类型、同名字段”的情况。
     * <p>
     * 在字段查找表中，根据字段名称、字段类型，查找符合的全部字段行。
     *
     * @param fieldName 字段名称.
     * @param fieldType 字段类型.
     * @return 字段所在的行的集合.
     */
    List<Row> find(String fieldName, Class fieldType);

    /**
     * 处理某个类的多个实例具有“同类型、同名字段”的情况。
     * <p>
     * 在字段查找表中，根据字段名称、字段类型、来源类的类型，查找符合的全部字段行。
     *
     * @param fieldName 字段名称.
     * @param fieldType 字段类型.
     * @param clazz     来源类的类型.
     * @return 字段所在的行的集合.
     */
    List<Row> find(String fieldName, Class fieldType, Class clazz);


    /**
     * 得到字段表中的所有行。
     *
     * @return 字段行的集合.
     */
    List<Row> rows();

    /**
     * 得到字段表的所有列。
     *
     * @return 字段表的所有列集合。
     */
    List<Column> columns();
}
