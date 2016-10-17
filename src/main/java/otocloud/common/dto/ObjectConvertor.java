package otocloud.common.dto;

import org.apache.commons.lang3.StringUtils;
import otocloud.common.dto.fetch.*;
import otocloud.common.dto.fetch.NoSuchFieldException;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * 提供不同类型实例间的相互转换。
 * 如，从Model到DTO的转换。
 * <p>
 * zhangyef@yonyou.com 2015-11-02.
 */
public class ObjectConvertor {

    /**
     * 根据指定的输出类型，将一个或多个类实例转换成输出类型。
     *
     * @param dto     输出类型的Class实例.
     * @param objects 类实例.
     * @param <T>     输出类型.
     * @return 输出类型的对象实例.
     */
    public static <T> T toDTO(Class<T> dto, Object... objects) {
        T obj = null;
        try {
            obj = dto.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            obj = null;
        }

        if (obj == null) {
            return null;
        }

        FieldTable fieldTable = makeFieldTable(objects);

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            fetchAndSet(obj, field, fieldTable);
        }

        return obj;
    }

    private static void fetchAndSet(Object obj, Field field, FieldTable fieldTable) {
        List<Row> rows = lookup(field, fieldTable);

        //没有找到类型匹配的属性字段
        if (rows == null) return;

        //选择一个确定属性
        Row row = chooseOne(field, rows);
        if (row == null) return;

        checkAndSet(obj, field, row.instance(), row.field());
    }

    private static void checkAndSet(Object toInstance, Field field,
                                    Object fromInstance, Field fromField) {

        //设置字段可以访问
        boolean isDataFieldAccessible = fromField.isAccessible();
        boolean isFieldAccess = field.isAccessible();

        field.setAccessible(true);
        fromField.setAccessible(true);

        setFieldValue(toInstance, field, fromInstance, fromField);

        //恢复字段的访问权限
        field.setAccessible(isFieldAccess);
        fromField.setAccessible(isDataFieldAccessible);
    }

    /**
     * 如果是继承关系的类，新建子类类型的实例。
     * 如果没有继承关系，新建目标类型的实例。
     *
     * @param toInstance
     * @param field
     * @param fromInstance
     * @param fromField
     * @return
     */
    private static boolean setNestedFieldValue(Object toInstance, Field field,
                                               Object fromInstance, Field fromField) {

        Class fromClazz = fromField.getType();
        Class toClazz = field.getType();
        //得到目标类类型。
        if (toClazz.isAssignableFrom(fromClazz)) {
            toClazz = fromClazz;
        }

        try {
            Object toData = toDTO(toClazz, fromField.get(fromInstance));
            if (toData != null) {
                field.set(toInstance, toData);
            }
            return true;
        } catch (IllegalAccessException ignore) {
            //不是Class类型的对象，或者没有
        }
        return false;
    }

    private static void setFieldValue(Object toInstance, Field field, Object fromInstance, Field fromField) {

        Object data;
        try {
            data = fromField.get(fromInstance);
        } catch (IllegalAccessException ignore) {
            return;
        }

        if (data == null) return;

        if (isBasicType(data)) {
            if (setBasicFieldValue(toInstance, field, data)) return;
        }

        //处理集合类
        if (isCollection(data)) {
            if (setCollectionFieldValue(toInstance, field, (Collection) data)) return;
        }

        //如果是对象类型(放在集合类型处理之后)
        setNestedFieldValue(toInstance, field, fromInstance, fromField);
    }

    private static boolean setBasicFieldValue(Object toInstance, Field field, Object toData) {
        if (toData == null) return false;
        try {
            field.set(toInstance, toData);
            return true;
        } catch (IllegalAccessException ignore) {
            //已经设置为“可以访问”。
        }

        return false;
    }

    private static boolean setCollectionFieldValue(Object toInstance, Field field, Collection data) {
        Object toData;
        Type type = field.getGenericType();
        if (type instanceof ParameterizedTypeImpl) {
            ParameterizedTypeImpl paraType = (ParameterizedTypeImpl) type;
            //如果是集合类
            if (Collection.class.isAssignableFrom(paraType.getRawType())) {
                //获得泛型实际参数(集合的元素类型)
                Type[] actualItemTypes = paraType.getActualTypeArguments();
                //集合类的泛型只有一个
                Class itemType = (Class) actualItemTypes[0];

                //迭代集合中的元素
                Collection collection = data;

                //根据来源集合类型创建目标类型，逐一对每个元素做转换。
                try {
                    final Collection toCollection = collection.getClass().newInstance();
                    collection.forEach(itemInstance -> {
                        Object toItem = toDTO(itemType, itemInstance);
                        toCollection.add(toItem);
                    });
                    toData = toCollection;

                    if (toData == null) return true;

                    try {
                        field.set(toInstance, toData);
                    } catch (IllegalAccessException ignore) {
                        //已经设置为“可以访问”。
                    }

                    return true;

                } catch (InstantiationException | IllegalAccessException ignore) {
                    //无法创建目标字段类型的集合，则直接返回.
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCollection(Object data) {
        Class clazz = data.getClass();
        return Collection.class.isAssignableFrom(clazz);
    }

    private static boolean isBasicType(Object data) {
        //先判断字符串，将字符串归结为基本类型
        if (data instanceof String) {
            return true;
        }

        try {
            Class primitive = (Class) data.getClass().getField("TYPE").get(null);
            return primitive.isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    private static List<Row> lookup(Field field, FieldTable fieldTable) {
        String name = field.getName();
        Class type = field.getType();

        List<Row> rows;

        if (field.isAnnotationPresent(From.class)) {
            From from = field.getDeclaredAnnotation(From.class);
            String fromName = from.name();
            //如果有注解字段名，使用注解中的指定字段名称。
            if (StringUtils.isNotBlank(fromName)) {
                name = fromName;
            }

            //如果不是默认类型，则使用注解指定的字段类型。
            Class fromType = from.fieldType();
            if (!fromType.equals(Object.class)) {
                type = fromType;
            }

            Class clazz = from.clazz();
            if (clazz.equals(Object.class)) {
                //如果是默认值，说明没有设置字段来源属性.
                rows = fieldTable.find(name, type);
            } else {
                rows = fieldTable.find(name, type, clazz);
            }
        } else {
            rows = fieldTable.find(name, type);
        }

        if (rows.size() < 1) {
            return null;
        }

        return rows;
    }

    /**
     * 根据注解策略，选择合适的行。
     *
     * @param field
     * @param choices
     * @return
     */
    private static Row chooseOne(Field field, List<Row> choices) {
        if (choices == null) return null;

        int size = choices.size();
        if (size == 0) return null;
        if (size == 1) return choices.get(0);

        //具有多个选择，说明通过Field名称和Field类型已经不能对Field做出区分。
        //此时，需要根据注解“获取策略”确定Field来源。
        if (field.isAnnotationPresent(From.class)) {
            From from = field.getAnnotation(From.class);
            FetchPolicy fetchPolicy = from.fetchPolicy();
            try {
                Row row = getFetcher(fetchPolicy).fetch(choices);
                return row;
            } catch (NoSuchFieldException ignore) {
                //直接返回null。
            }
        } else {
            //没有注解，直接返回第一个遇到的字段。
            return choices.get(0);
        }

        return null;
    }

    private static Fetcher getFetcher(FetchPolicy fetchPolicy) {
        Fetcher fetcher = null;
        switch (fetchPolicy) {
            case First:
                fetcher = new FirstFetcher();
                break;
            case FirstNull:
                fetcher = new FirstNullFetcher();
                break;
            case FirstNotNull:
                fetcher = new FirstNotNullFetcher();
                break;
            case Last:
                fetcher = new LastFetcher();
                break;
            case LastNull:
                fetcher = new LastNullFetcher();
                break;
            case LastNotNull:
                fetcher = new LastNotNullFetcher();
                break;
            default:
                fetcher = new FirstFetcher();
        }

        return fetcher;
    }

    /**
     * 制作属性表。
     *
     * @param objects 对象列表.
     * @return 属性查找表.
     */
    private static FieldTable makeFieldTable(Object... objects) {
        FieldTable fieldTable = new FieldTableImpl();

        for (Object obj : objects) {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                fieldTable.insert(field, obj);
            }
        }

        return fieldTable;
    }
}
