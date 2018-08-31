//******************************************************************************
//                                       ValidationExceptionMapper.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 25 Jun, 2018
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************

package phis2ws.service.resources.validation;

import java.util.ArrayList;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.ResponseFormGET;

/**
 * Class that catches validation errors (related to validation annotations) 
 * on resource services parameters and return response object with specific error messages.
 * @author Arnaud Charleroy <arnaud.charleroy@inra.fr>
 */
@Produces(MediaType.APPLICATION_JSON)
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<javax.validation.ValidationException> {

    final static Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(javax.validation.ValidationException e) {
        ArrayList<Status> statusList = new ArrayList<>();
        // Loop over violated contraint array 
        for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) e).getConstraintViolations()) {
            //SILEX:info
            // Message pattern : [object property path] + property name + message + |  invalid value
            //\SILEX:info
            // Add violation error and associated message
            String errorMessage = "[" + constraintViolation.getPropertyPath().toString() + "]" + ((PathImpl) constraintViolation.getPropertyPath()).getLeafNode().getName()
                    + " " + constraintViolation.getMessage() + "  | Invalid value : "
                    + constraintViolation.getInvalidValue();
            statusList.add(new Status(StatusCodeMsg.INVALID_INPUT_PARAMETERS, StatusCodeMsg.ERR, errorMessage));
        }

        ResponseFormGET validationResponse = new ResponseFormGET(statusList);
        return Response.status(Response.Status.BAD_REQUEST).entity(validationResponse).build();
    }
}
