package com.etl.api.ai.domain.validator;

import com.etl.api.ai.domain.form.AiModelSelectorForm;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验 id 和 apiKey 不能同时为空
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AiModelSelectorFormValidator.ApiKeyOrIdValidator.class)
public @interface AiModelSelectorFormValidator {

    String message() default "id 和 apiKey 不能同时为空";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ApiKeyOrIdValidator implements ConstraintValidator<AiModelSelectorFormValidator, AiModelSelectorForm> {

        @Override
        public boolean isValid(AiModelSelectorForm form, ConstraintValidatorContext constraintValidatorContext) {
            return form == null
                    || form.getId() != null
                    || StringUtils.hasText(form.getApiKey());
        }
    }
}
