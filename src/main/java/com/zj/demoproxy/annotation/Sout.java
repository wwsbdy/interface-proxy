package com.zj.demoproxy.annotation;

import java.lang.annotation.*;

/**
 * 打印value
 *
 * @author 19242
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Sout {
    String value();
}