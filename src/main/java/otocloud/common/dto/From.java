package otocloud.common.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记DTO类的某个域来源。
 * zhangyef@yonyou.com on 2015-11-03.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface From {
    /**
     * 来源Field名称。
     *
     * @return
     */
    String name() default "";

    /**
     * 默认为 Object.class。
     * 来源Field类型。
     *
     * @return Field类型.
     */
    Class<?> fieldType() default Object.class;

    /**
     * Field所在的来源类。
     *
     * @return
     */
    Class<?> clazz() default Object.class;

    /**
     * Field值的获取策略。
     *
     * @return
     */
    FetchPolicy fetchPolicy() default FetchPolicy.First;
}
