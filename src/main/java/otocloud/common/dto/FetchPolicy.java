package otocloud.common.dto;

/**
 * 当存在相同类的多个实例时，标记存取属性的策略。
 * <p>
 * 策略可以组合使用。
 * <ul>
 * <li>[First] 取出第一个遇到的实例属性，不论是否为null。</li>
 * <li>[Last] 取出最后一个实例属性，不论是否为null。</li>
 * <li>[First, Null] 取出第一个为null的实例属性。</li>
 * <li>[First, NotNull] 取出第一个不为null的实例属性。</li>
 * <li>[Last, Null] 取出最后一个为null的实例属性。</li>
 * <li>[Last, NotNull] 取出最后一个不为null的实例属性。</li>
 * </ul>
 * zhangyef@yonyou.com on 2015-11-03.
 */
public enum FetchPolicy {
    /**
     * 取出第一个遇到的实例属性。
     */
    First,
    /**
     * 取出最后一个遇到的实例属性。
     */
    Last,
    /**
     * 第一个为null的实例属性。
     */
    FirstNull,
    /**
     * 第一个不为null的实例属性。
     */
    FirstNotNull,
    /**
     * 最后一个为null的实例属性。
     */
    LastNull,
    /**
     * 最后一个不为null的实例属性。
     */
    LastNotNull
}
