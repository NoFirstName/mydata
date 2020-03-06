package com.bw.configmanager.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 带有缓存。
 * 该注释只支持通过类型来过滤数据的方法,即通过某个字段的值来过滤缓存中的数据,如果改值为null,则返回所有的数据。
 * @author zhuyiwen
 * @version 创建时间：2019年1月9日 下午1:56:16
 */
@Documented
@Target(
{ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WithCache
{

    /**
     * 通过该字段过滤，如通过id来过滤,就填id。
     * @return
     */
    public String field();

    /**
     * 如果出现多个参数的情况，需指定参数名，只有一个参数，默认就是那个参数。
     * @return
     */
    public String key() default "";

}
