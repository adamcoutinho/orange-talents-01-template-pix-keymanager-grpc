package br.com.zup.core.validations

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext


class UniqueValueImpl : ConstraintValidator<UniqueValue, String> {

    override fun isValid(value: String?,annotationMetadata: AnnotationValue<UniqueValue>,context: ConstraintValidatorContext,): Boolean {
        TODO("Not yet implemented")
    }
}