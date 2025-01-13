package org.example.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyCacheable {
    // 过期时间，多久之后过期
    String expire() default "";

    // 过期时间，几点过期
    String expireAt() default "";

    // 名称，一般用服务名
    String invoker();


}