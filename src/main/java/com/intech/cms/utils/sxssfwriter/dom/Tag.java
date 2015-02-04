package com.intech.cms.utils.sxssfwriter.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Tag {

	String name();

	boolean canHaveBody() default true;

	String[] requiredAttributes() default {};

	String[] optionalAttributes() default {};
}
