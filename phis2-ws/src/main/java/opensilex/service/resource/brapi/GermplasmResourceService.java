//******************************************************************************
//                                GermplasmResourceService.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 1 juil. 2019
// Contact: alice.boizet@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.brapi;

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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.AccessionDAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.model.Accession;
import opensilex.service.resource.ResourceService;
import opensilex.service.resource.dto.accession.GermplasmDTO;
import opensilex.service.resource.validation.interfaces.URL;
import opensilex.service.result.ResultForm;
import opensilex.service.view.brapi.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Germplasm resource service
 * @author Alice Boizet <alice.boizet@inra.fr>
 */
@Api("/brapi/v1/germplasm")
@Path("/brapi/v1/germplasm")
public class GermplasmResourceService extends ResourceService {
    final static Logger LOGGER = LoggerFactory.getLogger(GermplasmResourceService.class);
    
    @GET
    @ApiOperation(value = "Get all germplasm corresponding to the search params given",
                  notes = "Retrieve all germplasm authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all germplasm", response = GermplasmDTO.class, responseContainer = "List"),
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
    public Response getGermplasmBySearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
            @ApiParam(value = "Search by germplasmDbId", example = DocumentationAnnotation.EXAMPLE_GERMPLASM_URI) @QueryParam("germplasmDbId") String uri,
            @ApiParam(value = "Search by germplasmPUI", example = DocumentationAnnotation.EXAMPLE_GERMPLASM_URI) @QueryParam("germplasmPUI") String germplasmPUI,
            @ApiParam(value = "Search by germplasmName / not supported") @QueryParam("germplasmName") @URL String germplasmName,
            @ApiParam(value = "Search by commonCropName / not supported") @QueryParam("commonCropName") String commonCropName,
            //added the parameter language to choose the language of species label 
            @ApiParam(value = "choose the language of the species", example = "en") @QueryParam("language") String language
      ){
        
        if (germplasmPUI != null){
            uri = germplasmPUI;
        }
        
        AccessionDAO germplasmDAO = new AccessionDAO();
        //1. Get count
        Integer totalCount = germplasmDAO.count(uri, germplasmName, commonCropName, language);
        
        //2. Get germplasms
        ArrayList<Accession> germplasmFounded = germplasmDAO.find(page, pageSize, uri, germplasmName, commonCropName, language);
        
        //3. Return result
        ArrayList<Status> statusList = new ArrayList<>();
        ArrayList<GermplasmDTO> germplasmToReturn = new ArrayList<>();
        ResultForm<GermplasmDTO> getResponse;
        if (germplasmFounded == null) { //Request failure
            getResponse = new ResultForm<>(0, 0, germplasmToReturn, true);
            return noResultFound(getResponse, statusList);
        } else if (germplasmFounded.isEmpty()) { //No result found
            getResponse = new ResultForm<>(0, 0, germplasmToReturn, true);
            return noResultFound(getResponse, statusList);
        } else { //Results
            //Convert all objects to DTOs
            germplasmFounded.forEach((germplasm) -> {
                germplasmToReturn.add(new GermplasmDTO(germplasm));
            });
            
            getResponse = new ResultForm<>(pageSize, page, germplasmToReturn, true, totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }
    
}
