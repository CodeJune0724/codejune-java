package com.codejune.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Column
 *
 * @author ZJ
 * */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name();

    String remark() default "";

    int length() default -1;

    boolean required() default false;

    boolean unique() default false;

}