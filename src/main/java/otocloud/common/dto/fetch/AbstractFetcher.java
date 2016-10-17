package otocloud.common.dto.fetch;

import java.lang.reflect.Field;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public abstract class AbstractFetcher implements Fetcher {
    /**
     * 从指定实例中提取指定字段的数据。
     *
     * @param instance 实例.
     * @param field    字段.
     * @return 字段值.
     */
    Object get(Object instance, Field field) {
        boolean isAccessible = field.isAccessible();
        Object data = null;
        try {
            field.setAccessible(true);
            data = field.get(instance);
            field.setAccessible(isAccessible);
        } catch (IllegalAccessException ignore) {
            //已经设置访问
        }

        return data;
    }
}
