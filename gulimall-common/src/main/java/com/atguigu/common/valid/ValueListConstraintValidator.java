package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ValueListConstraintValidator implements ConstraintValidator<ValueList, Integer> {
    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(ValueList constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        if (vals.length > 0) {
            for (int val : vals) {
                set.add(val);
            }
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || set.contains(value);
    }
}
