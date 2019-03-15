//******************************************************************************
//                      DataResourceService.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 1 March 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import phis2ws.service.configuration.DateFormat;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.GlobalWebserviceValues;
import phis2ws.service.dao.mongo.DataDAOMongo;
import phis2ws.service.dao.mongo.DataFileDAOMongo;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.resources.dto.data.DataDTO;
import phis2ws.service.resources.dto.data.DataPostDTO;
import phis2ws.service.resources.dto.data.FileDescriptionDTO;
import phis2ws.service.resources.dto.data.FileDescriptionPostDTO;
import phis2ws.service.resources.validation.interfaces.Date;
import phis2ws.service.resources.validation.interfaces.Required;
import phis2ws.service.resources.validation.interfaces.URL;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.AbstractResultForm;
import phis2ws.service.view.brapi.form.ResponseFormData;
import phis2ws.service.view.brapi.form.ResponseFormFileDescription;
import phis2ws.service.view.brapi.form.ResponseFormPOST;
import phis2ws.service.view.model.phis.Data;
import phis2ws.service.view.model.phis.FileDescription;

/**
 * Data resource service
 * @Author Vincent Migot
 */
@Api("/data")
@Path("/data")
public class DataResourceService extends ResourceService {
    
    /**
     * Service to insert data. 
     * @example
     * [
     *  {
     *      "objectUri": "http://www.phenome-fppn.fr/diaphen/2018/s18521",
     *      "variableUri": "http://www.phenome-fppn.fr/id/variables/v001",
     *      "date": "2017-06-15T10:51:00+0200",
     *      "value": "0.5"
     *  }
     * ]
     * @param rawData
     * @param context
     * @return the insertion result. 
     */
    @POST
    @ApiOperation(value = "Post data",
                  notes = "Register data in the database")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "data saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postData(
        @ApiParam(value = DocumentationAnnotation.DATA_POST_DEFINITION) @Valid List<DataPostDTO> data,
        @Context HttpServletRequest context) {
        
        AbstractResultForm postResponse = null;
        
        try {
            if (data != null && !data.isEmpty()) {
                DataDAOMongo dataDAO = new DataDAOMongo();

                dataDAO.user = userSession.getUser();

                POSTResultsReturn result = dataDAO.checkAndInsert(dataDTOsToData(data));

                if (result.getHttpStatus().equals(Response.Status.CREATED)) {
                    postResponse = new ResponseFormPOST(result.statusList);
                    postResponse.getMetadata().setDatafiles(result.getCreatedResources());
                } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                        || result.getHttpStatus().equals(Response.Status.OK)
                        || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                    postResponse = new ResponseFormPOST(result.statusList);
                }
                return Response.status(result.getHttpStatus()).entity(postResponse).build();
            } else {
                postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, "No data to add"));
                return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
            }       
        } catch (ParseException e) {
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, e.getMessage()));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
    
    /**
     * Generates an data list from a given list of DataPostDTO.
     * @param dataDTOs
     * @return the list of data
     */
    private List<Data> dataDTOsToData(List<DataPostDTO> dataDTOs) throws ParseException {
        ArrayList<Data> dataList = new ArrayList<>();
        
        for (DataPostDTO dataDTO : dataDTOs) {
            dataList.add(dataDTO.createObjectFromDTO());            
        }
        
        return dataList;
    }

    /**
     * Service to get scientific data
     * @param pageSize
     * @param page
     * @param variable
     * @param startDate
     * @param endDate
     * @param object
     * @param provenance
     * @param dateSortAsc
     * @return list of the data corresponding to the search params given
     * @example
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
     *               {
     *                 "uri": "http://www.phenome-fppn.fr/diaphen/id/data/d2plf65my4rc2odiv2lbjgukc2zswkqyoddh25jtoy4b5pf3le3q4ec5c332f5cd44ce82977e404cebf83c",
     *                 "provenanceUri": "http://www.phenome-fppn.fr/mtp/2018/pv181515071552",
     *                 "objectUri": "http://www.phenome-fppn.fr/diaphen/2018/o18001199",
     *                 "variableUri": "http://www.phenome-fppn.fr/diaphen/id/variables/v009",
     *                 "date": "2017-06-15T00:00:00+0200",
     *                 "value": 2.4
     *               },
     *               {
     *                 "uri": "http://www.phenome-fppn.fr/diaphen/id/data/pttdrrqybxoyku4img323dyrhmpp267mhnpiw3vld2wm6tap3vwq93b344c429ec45bb9b185edfe5bc2b64",
     *                 "provenanceUri": "http://www.phenome-fppn.fr/mtp/2018/pv181515071552",
     *                 "objectUri": "http://www.phenome-fppn.fr/diaphen/2018/o18001199",
     *                 "variableUri": "http://www.phenome-fppn.fr/diaphen/id/variables/v009",
     *                 "date": "2017-06-16T00:00:00+0200",
     *                 "value": "2017-06-15T00:00:00+0200"
     *               }
     *          ]
     *      }
     * }
     */
    @GET
    @ApiOperation(value = "Get data corresponding to the search parameters given.",
                  notes = "Retrieve all data corresponding to the search parameters given,"
                          + "<br/>Date parameters could be either a datetime like: " + DocumentationAnnotation.EXAMPLE_XSDDATETIME 
                          + "<br/>or simply a date like: " + DocumentationAnnotation.EXAMPLE_DATE)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all data", response = Data.class, responseContainer = "List"),
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
    public Response getData(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
        @ApiParam(value = "Search by variable uri", example = DocumentationAnnotation.EXAMPLE_VARIABLE_URI, required=true) @QueryParam("variable") @URL @Required String variable,
        @ApiParam(value = "Search by minimal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("startDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String startDate,
        @ApiParam(value = "Search by maximal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("endDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String endDate,
        @ApiParam(value = "Search by object uri", example = DocumentationAnnotation.EXAMPLE_SENSOR_URI) @QueryParam("object")  @URL String object,
        @ApiParam(value = "Search by provenance uri", example = DocumentationAnnotation.EXAMPLE_PROVENANCE_URI) @QueryParam("provenance")  @URL String provenance,
        @ApiParam(value = "Date search result order ('true' for ascending and 'false' for descending)", example = "true") @QueryParam("dateSortAsc") boolean dateSortAsc
    ) {
        // 1. Initialize dataDAO with parameters
        DataDAOMongo dataDAO = new DataDAOMongo();
        
        dataDAO.variableUri = variable;

        dataDAO.startDate = startDate;
        dataDAO.endDate = endDate;
        dataDAO.objectUri = object;
        dataDAO.provenanceUri = provenance;
        dataDAO.dateSortAsc = dateSortAsc;
        
        dataDAO.user = userSession.getUser();
        dataDAO.setPage(page);
        dataDAO.setPageSize(pageSize);
        
        // 2. Get data count
        int totalCount = dataDAO.count();
        
        // 3. Get data page list
        ArrayList<Data> dataList = dataDAO.allPaginate();
        
        // 4. Initialize return variables
        ArrayList<DataDTO> list = new ArrayList<>();
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormData getResponse;
        
        if (dataList == null) {
            // Request failure
            getResponse = new ResponseFormData(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (dataList.isEmpty()) {
            // No results
            getResponse = new ResponseFormData(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else {
            // Convert all measures object to DTO's
            dataList.forEach((data) -> {
                list.add(new DataDTO(data));
            });
            
            // Return list of DTO
            getResponse = new ResponseFormData(dataDAO.getPageSize(), dataDAO.getPage(), list, true, totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }
    }     
    
    /**
     * Save data file with its metadata and use MULTIPART_FORM_DATA for it
     * fileContentDisposition parameter is automatically created from submited file
     * No example could be provided for this kind of MediaType 
     * @param descriptionDto
     * @param file
     * @param fileContentDisposition
     * @return the insertion result. 
     */
    @POST
    @Path("file")
    @ApiOperation(value = "Post data file")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Data file and metadata saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                          dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)  
    public Response postDataFile(
        @ApiParam(value = "File description with metadata", required = true) @NotNull @Valid @FormDataParam("description") FileDescriptionPostDTO descriptionDto,
        @ApiParam(value = "Data file", required = true) @NotNull @FormDataParam("file") File file,
        @FormDataParam("file") FormDataContentDisposition fileContentDisposition
    ) {
        
        DataFileDAOMongo dataFileDaoMongo = new DataFileDAOMongo();
        AbstractResultForm postResponse = null;
        try {
            FileDescription description = descriptionDto.createObjectFromDTO();
            description.setFilename(fileContentDisposition.getFileName());
            POSTResultsReturn result = dataFileDaoMongo.checkAndInsert(
                description,
                file
            );

            if (result.getHttpStatus().equals(Response.Status.CREATED)) {
                postResponse = new ResponseFormPOST(result.statusList);
                postResponse.getMetadata().setDatafiles(result.getCreatedResources());
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                        || result.getHttpStatus().equals(Response.Status.OK)
                        || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }

            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } catch (ParseException e) {
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, e.getMessage()));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
    
    /**
     * Return the content of the file corresponding to the uri given.
     * No authentication on this service because image file must be accessible directly
     * @param fileUri
     * @param response
     * @return The file content or null with a 404 status if it doesn't exists
     */
    @GET
    @Path("file/{fileUri}")
    @ApiOperation(value = "Get data file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve file"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 404, message = DocumentationAnnotation.FILE_NOT_FOUND),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @Produces(MediaType.APPLICATION_OCTET_STREAM)  
    public Response getDataFile(
        @ApiParam(value = "Search by fileUri", required = true, example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_URI ) @PathParam("fileUri") @URL @Required String fileUri,
            @Context HttpServletResponse response
    ) {
        
        DataFileDAOMongo dataFileDaoMongo = new DataFileDAOMongo();
        
        FileDescription description = dataFileDaoMongo.findFileDescriptionByUri(fileUri);
        
        if (description == null) {
            return Response.status(404).build();
        }
        
        try {
            FileInputStream stream = new FileInputStream(new File(description.getPath()));
            
            return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + description.getFilename() + "\"" ) //optional
                .build();
        } catch (FileNotFoundException ex) {
            return Response.status(404).build();
        }
    }
    
    /**
     * This service return the description of a file correponding to the uri given
     * @param fileUri
     * @param response
     * @return the file description
     * @example 
     * {
     *    "uri": "http://www.phenome-fppn.fr/diaphen/id/dataFile/RGBImage/55fjbbmtmr4m3kkizslzaddfkdt2ranum3ikz6cdiajqzfdc7yqa31d87b83efac4c358ceb5b0da6ed27ff",
     *    "rdfType": "http://www.opensilex.org/vocabulary/oeso#RGBImage",
     *    "date": "2017-06-15T10:51:00+0200",
     *    "concernedItems": [{
     *      "uri": "http://www.phenome-fppn.fr/diaphen/2018/o18001199",
     *      "typeURI": "http://www.opensilex.org/vocabulary/oeso#Plot"
     *    }],
     *    "provenanceUri": "http://www.phenome-fppn.fr/diaphen/id/provenance/1552405256945",
     *    "metadata": {
     *      "sensor": "http://www.phenome-fppn.fr/diaphen/2018/s18035",
     *      "position": "1"
     *    }
     * }
     */
    @GET
    @Path("file/{fileUri}/description")
    @ApiOperation(value = "Get data file description")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve file description", response = FileDescriptionDTO.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 404, message = DocumentationAnnotation.FILE_NOT_FOUND),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                          dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)  
    public Response getDataFileDescription(
        @ApiParam(value = "Search by fileUri", required = true, example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_URI ) @PathParam("fileUri") @URL @Required String fileUri,
            @Context HttpServletResponse response
    ) {
        
        DataFileDAOMongo dataFileDaoMongo = new DataFileDAOMongo();
        
        FileDescription description = dataFileDaoMongo.findFileDescriptionByUri(fileUri);
        
        if (description == null) {
            return Response.status(404).build();
        }
        
        return Response.status(Response.Status.OK).entity(new FileDescriptionDTO(description)).build();
    }
    
    
    /**
     * This service search for file descriptions according to the search parameters given.
     * @param pageSize
     * @param page
     * @param rdfType
     * @param startDate
     * @param endDate
     * @param provenance
     * @param concernedItems
     * @param jsonValueFilter
     * @param dateSortAsc
     * @return List of file description
    * @example 
     * [{
     *    "uri": "http://www.phenome-fppn.fr/diaphen/id/dataFile/RGBImage/55fjbbmtmr4m3kkizslzaddfkdt2ranum3ikz6cdiajqzfdc7yqa31d87b83efac4c358ceb5b0da6ed27ff",
     *    "rdfType": "http://www.opensilex.org/vocabulary/oeso#RGBImage",
     *    "date": "2017-06-15T10:51:00+0200",
     *    "concernedItems": [{
     *      "uri": "http://www.phenome-fppn.fr/diaphen/2018/o18001199",
     *      "typeURI": "http://www.opensilex.org/vocabulary/oeso#Plot"
     *    }],
     *    "provenanceUri": "http://www.phenome-fppn.fr/diaphen/id/provenance/1552405256945",
     *    "metadata": {
     *      "sensor": "http://www.phenome-fppn.fr/diaphen/2018/s18035",
     *      "position": "1"
     *    }
     * }]
     */
    @GET
    @Path("file/search")
    @ApiOperation(value = "Retrieve data file descriptions corresponding to the search parameters given.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve file descriptions", response = FileDescriptionDTO.class, responseContainer = "List"),
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
    public Response getDataFileDescriptionsBySearch(
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
        @ApiParam(value = "Search by rdf type uri", example = DocumentationAnnotation.EXAMPLE_VARIABLE_URI, required=true) @QueryParam("rdfType") @URL @Required String rdfType,
        @ApiParam(value = "Search by minimal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("startDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String startDate,
        @ApiParam(value = "Search by maximal date", example = DocumentationAnnotation.EXAMPLE_XSDDATETIME) @QueryParam("endDate") @Date({DateFormat.YMDTHMSZ, DateFormat.YMD}) String endDate,
        @ApiParam(value = "Search by provenance uri", example = DocumentationAnnotation.EXAMPLE_PROVENANCE_URI) @QueryParam("provenance")  @URL String provenance,
        @ApiParam(value = "Search by concerned items uri", example = DocumentationAnnotation.EXAMPLE_SCIENTIFIC_OBJECT_URI) @QueryParam("concernedItems")  @URL List<String> concernedItems,
        @ApiParam(value = "Search by json filter", example = DocumentationAnnotation.EXAMPLE_PROVENANCE_METADATA) @QueryParam("jsonValueFilter") String jsonValueFilter,
        @ApiParam(value = "Date search result order ('true' for ascending and 'false' for descending)", example = "true") @QueryParam("dateSortAsc") boolean dateSortAsc
    ) {
        
        DataFileDAOMongo dataFileDaoMongo = new DataFileDAOMongo();
        
        // 1. Set all varaibles corresponding to the search
        dataFileDaoMongo.user = userSession.getUser();
        dataFileDaoMongo.setPage(page);
        dataFileDaoMongo.setPageSize(pageSize);
        
        // 2. Get data count
        long totalCount = dataFileDaoMongo.count(
            rdfType,
            startDate,
            endDate,
            provenance,
            jsonValueFilter,
            concernedItems,
            dateSortAsc
        );
        
        // 3. Get data page list
        ArrayList<FileDescription> dataList = dataFileDaoMongo.search(
            rdfType,
            startDate,
            endDate,
            provenance,
            jsonValueFilter,
            concernedItems,
            dateSortAsc
        );
        
        // 4. Initialize return variables
        ArrayList<FileDescriptionDTO> list = new ArrayList<>();
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormFileDescription getResponse;
        
        if (dataList == null) {
            // Request failure
            getResponse = new ResponseFormFileDescription(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else if (dataList.isEmpty()) {
            // No results
            getResponse = new ResponseFormFileDescription(0, 0, list, true, 0);
            return noResultFound(getResponse, statusList);
        } else {
            // Convert all measures object to DTO's
            dataList.forEach((data) -> {
                list.add(new FileDescriptionDTO(data));
            });
            
            // Return list of DTO
            getResponse = new ResponseFormFileDescription(dataFileDaoMongo.getPageSize(), dataFileDaoMongo.getPage(), list, true, (int)totalCount);
            getResponse.setStatus(statusList);
            return Response.status(Response.Status.OK).entity(getResponse).build();
        }

    }
}
