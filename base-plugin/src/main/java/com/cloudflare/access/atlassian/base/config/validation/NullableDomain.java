package com.cloudflare.access.atlassian.base.config.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.apache.bval.extras.constraints.net.Domain;

/**
 * Validation annotation to wrap the @Domain annotation allowing nulls.
 *
 * The message from
 * @author felipebn
 *
 */
@Documented
@Constraint(validatedBy = NullableDomainValidator.class)
@Target({ FIELD, ANNOTATION_TYPE, PARAMETER })
@Retention(RUNTIME)
public @interface NullableDomain {

	Domain domain() default @Domain();

    Class<?>[] groups() default {};

    String message() default "{org.apache.bval.extras.constraints.net.Domain.message}";

    Class<? extends Payload>[] payload() default {};
}
