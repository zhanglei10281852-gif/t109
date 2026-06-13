package com.company.material.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    String warehouseField() default "warehouseId";

    String departmentField() default "department";

    String creatorField() default "createdBy";

    boolean checkWarehouse() default false;

    boolean checkDepartment() default false;

    boolean checkCreator() default false;
}
