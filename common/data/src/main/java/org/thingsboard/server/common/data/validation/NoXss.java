package org.thingsboard.server.common.data.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
public @interface NoXss {
    String message() default "field value is malformed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
