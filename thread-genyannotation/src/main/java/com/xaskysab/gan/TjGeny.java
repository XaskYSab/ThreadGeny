package com.xaskysab.gan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by XaskYSab on 2017/4/16 0016.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface TjGeny {

    TMode value() default TMode.POSTING;

}
