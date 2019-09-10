package com.cloudflare.access.atlassian.base.config.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.bval.extras.constraints.net.DomainValidator;
import org.apache.commons.lang3.StringUtils;

public class NullableDomainValidator implements ConstraintValidator<NullableDomain, String> {

	private DomainValidator delegate = new DomainValidator();

	@Override
	public void initialize(NullableDomain constraintAnnotation) {
		delegate.initialize(constraintAnnotation.domain());
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(StringUtils.isEmpty(value)) return true;
		return delegate.isValid(value, context);
	}

}
