package org.thingsboard.server.dao.service;

import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.thingsboard.server.common.data.validation.NoXss;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Slf4j
public class NoXssValidator implements ConstraintValidator<NoXss, Object> {
    private static final AntiSamy xssChecker = new AntiSamy();
    private static Policy xssPolicy;

    @Override
    public void initialize(NoXss constraintAnnotation) {
        if (xssPolicy == null) {
            xssPolicy = Optional.ofNullable(getClass().getClassLoader().getResourceAsStream("xss-policy.xml"))
                    .map(inputStream -> {
                        try {
                            return Policy.getInstance(inputStream);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new IllegalStateException("XSS policy file not found"));
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            return true;
        }

        try {
            return xssChecker.scan((String) value, xssPolicy).getNumberOfErrors() == 0;
        } catch (ScanException | PolicyException e) {
            return false;
        }
    }
}
