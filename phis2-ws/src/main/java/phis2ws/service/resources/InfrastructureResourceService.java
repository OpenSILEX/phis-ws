//******************************************************************************
//                                       InfrastructureResourceService.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 5 sept. 2018
// Contact: vincent.migot@inra.fr anne.tireau@inra.fr, pascal.neveu@inra.fr
// Subject: represnet infrastructures API
//******************************************************************************
package phis2ws.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.authentication.Session;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.GlobalWebserviceValues;
import phis2ws.service.dao.sesame.InfrastructureDAOSesame;
import phis2ws.service.dao.sesame.PropertyLabelDAOSesame;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.injection.SessionInject;
import phis2ws.service.ontologies.Vocabulary;
import phis2ws.service.resources.dto.PropertiesDTO;
import phis2ws.service.resources.dto.PropertyLabelsDTO;
import phis2ws.service.resources.validation.interfaces.Required;
import phis2ws.service.resources.validation.interfaces.URL;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.ResponseFormInfrastructure;
import phis2ws.service.view.brapi.form.ResponseFormProperties;
import phis2ws.service.view.manager.ResultForm;
import phis2ws.service.view.model.phis.Infrastructure;

/**
 * Infrastructure service
 * @author Vincent Migot <vincent.migot@inra.fr>
 */
@Api("/infrastructures")
@Path("/infrastructures")
public class InfrastructureResourceService {
    final static Logger LOGGER = LoggerFactory.getLogger(InfrastructureResourceService.class);
    
    //user session
    @SessionInject
    Session userSession;
    
    /**
     * Search infrastructures by uri, rdfType. 
     * 
     * @param pageSize
     * @param page
     * @param uri
     * @param rdfType
     * @param label
     * @return list of the infrastructures corresponding to the search params given
     * e.g
     * {
     *      "metadata": {
     *          "pagination": {
     *              "pageSize": 20,
     *              "currentPage": 0,
     *              "totalCount": 3,
     *              "totalPages": 1
     *          },
     *          "status": [],
     *          "datafiles": []
     *      },
     *      "result": {
     *          "data": [
     *              {
     *                  "uri": "http://www.phenome-fppn.fr",
     *                  "rdfType": "http://www.phenome-fppn.fr/vocabulary/2018/oepo#NationalInfrastructure",
     *                  "label": "alias"
     *              },
     *          ]
     *      }
     * }
     */
    @GET
    @ApiOperation(value = "Get all infrastructures corresponding to the search params given",
                  notes = "Retrieve all infrastructures authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all infrastructures", response = Infrastructure.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructuresBySearch(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
        @ApiParam(value = "Search by uri", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_URI) @QueryParam("uri") @URL String uri,
        @ApiParam(value = "Search by type uri", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_RDF_TYPE) @QueryParam("rdfType") @URL String rdfType,
        @ApiParam(value = "Search by label", example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_LABEL) @QueryParam("label") String label
    ) {
        InfrastructureDAOSesame infrastructureDAO = new InfrastructureDAOSesame();
        
        if (uri != null) {
            infrastructureDAO.uri = uri;
        }
        if (rdfType != null) {
            infrastructureDAO.rdfType = rdfType;
        }
        if (label != null) {
            infrastructureDAO.label = label;
        }
        
        infrastructureDAO.user = userSession.getUser();
        infrastructureDAO.setPage(page);
        infrastructureDAO.setPageSize(pageSize);
        
        return getInfrastructuresData(infrastructureDAO);
    }

    /**
     * Search infrastructures corresponding to search params given by a user
     * @param InfrastructureDAOSesame
     * @return the infrastructures corresponding to the search
     */
    private Response getInfrastructuresData(InfrastructureDAOSesame infrastructureDAO) {
        ArrayList<Infrastructure> infrastructures;
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormInfrastructure getResponse;

        // Count all annotations for this specific request
        Integer totalCount = infrastructureDAO.count();
        // Retreive all annotations returned by the query
        infrastructures = infrastructureDAO.allPaginate();

        if (infrastructures == null) {
            getResponse = new ResponseFormInfrastructure(0, 0, infrastructures, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (infrastructures.isEmpty()) {
            getResponse = new ResponseFormInfrastructure(0, 0, infrastructures, true, 0);
            return noResultFound(getResponse, statusList);
        } else {
            getResponse = new ResponseFormInfrastructure(infrastructureDAO.getPageSize(), infrastructureDAO.getPage(), infrastructures, true, totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }
    
    /**
     * Return a generic response when no result are found
     * @param getResponse
     * @param insertStatusList
     * @return the response "no result found" for the service
     */
    private Response noResultFound(ResultForm getResponse, ArrayList<Status> insertStatusList) {
        insertStatusList.add(new Status(StatusCodeMsg.NO_RESULTS, StatusCodeMsg.INFO, "No results for the infrastructures"));
        getResponse.setStatus(insertStatusList);
        return Response.status(Response.Status.NOT_FOUND).entity(getResponse).build();
    }
    
    /**
     * Search infrastructure details for a given uri
     * 
     * @param pageSize
     * @param page
     * @param uri
     * @return list of the infrastructure's detail corresponding to the search uri
     * e.g
     * {
     *   "metadata": {
     *     "pagination": null,
     *     "status": [],
     *     "datafiles": []
     *   },
     *   "result": {
     *     "data": [
     *       {
     *         "uri": "http://www.phenome-fppn.fr/diaphen",
     *         "properties": [
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
     *             "value": "http://www.phenome-fppn.fr/vocabulary/2017#Installation"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.w3.org/2000/01/rdf-schema#label",
     *             "value": "DIAPHEN"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.phenome-fppn.fr/vocabulary/2017#hasPart",
     *             "value": "http://www.phenome-fppn.fr/diaphen/ea1"
     *           },
     *           {
     *             "rdfType": null,
     *             "relation": "http://www.phenome-fppn.fr/vocabulary/2017#hasPart",
     *             "value": "http://www.phenome-fppn.fr/diaphen/ef1"
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     */
    @GET
    @Path("{uri}")
    @ApiOperation(value = "Get all infrastructure's details corresponding to the search uri",
                  notes = "Retrieve all infrastructure's details authorized for the user corresponding to the searched uri")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve infrastructure's details", response = PropertiesDTO.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfrastructureDetails(
        @ApiParam(value = DocumentationAnnotation.INFRASTRUCTURE_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_INFRASTRUCTURE_URI) @PathParam("uri") @URL @Required String uri,
        @ApiParam(value = "Language", example = DocumentationAnnotation.EXAMPLE_LANGUAGE) @QueryParam("lang") String lang,
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page) {            
        PropertyLabelDAOSesame propertiesDAO = new PropertyLabelDAOSesame();
        
        propertiesDAO.uri = uri;
        propertiesDAO.subClassOf = Vocabulary.CONCEPT_INFRASTRUCTURE;
        if (lang != null) {
            propertiesDAO.lang = lang;
        }
                
        propertiesDAO.user = userSession.getUser();
        propertiesDAO.setPage(page);
        propertiesDAO.setPageSize(pageSize);
        
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormProperties getResponse;

        // Retreive all annotations returned by the query
        ArrayList<PropertiesDTO<PropertyLabelsDTO>> infrastructureDetails = propertiesDAO.getAllProperties();

        if (infrastructureDetails == null) {
            getResponse = new ResponseFormProperties(0, 0, infrastructureDetails, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (infrastructureDetails.isEmpty()) {
            getResponse = new ResponseFormProperties(0, 0, infrastructureDetails, true, 0);
            return noResultFound(getResponse, statusList);
        } else {
            getResponse = new ResponseFormProperties(propertiesDAO.getPageSize(), propertiesDAO.getPage(), infrastructureDetails, true, infrastructureDetails.size());
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }
}
