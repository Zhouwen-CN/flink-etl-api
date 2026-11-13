package com.etl.api.sys.domain.validator;

import com.etl.api.sys.domain.form.SysUserChangePwdForm;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import lombok.val;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验表单对象内两个字段值是否相等
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SysUserChangePwdFormValidator.FieldMatchValidator.class)
public @interface SysUserChangePwdFormValidator {
    String message() default "两次输入的密码不一致";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class FieldMatchValidator implements ConstraintValidator<SysUserChangePwdFormValidator, SysUserChangePwdForm> {

        @Override
        public boolean isValid(SysUserChangePwdForm form, ConstraintValidatorContext constraintValidatorContext) {
            val newPwd = form.getNewPwd();
            val confirmPwd = form.getConfirmPwd();

            // 比较两个字段的值是否相等
            return newPwd != null && newPwd.equals(confirmPwd);
        }
    }
}
