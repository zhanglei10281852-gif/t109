package com.company.material.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String operationType();

    String businessModule();

    String description() default "";

    String targetIdParam() default "id";

    Class<?> entityClass() default Object.class;
}
