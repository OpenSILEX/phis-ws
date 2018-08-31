//******************************************************************************
//                                       DocumentStatus.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 28, Jun 2018
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, 
// pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.resources.validation.interfaces;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import phis2ws.service.resources.validation.validators.DocumentStatusValidator;

/**
 * Interface to check that the given document status is one of thoses expected
 * @see DocumentStatusValidator
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>, Morgane Vidal <morgane.vidal@inra.fr>
 */
@Target(value={METHOD,FIELD,ANNOTATION_TYPE,CONSTRUCTOR,PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = DocumentStatusValidator.class)
public @interface DocumentStatus {
    String message() default "is not a valid document status";
 
    Class<?>[] groups() default {};
 
    Class<? extends Payload>[] payload() default {};
}