//******************************************************************************
//                                       SensorResourceService.java
//
// Author(s): Morgane Vidal <morgane.vidal@inra.fr>
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: 14 mars 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  14 mars 2018
// Subject: represents the sensor service
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.authentication.Session;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.GlobalWebserviceValues;
import phis2ws.service.dao.sesame.SensorDAOSesame;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.injection.SessionInject;
import phis2ws.service.resources.dto.SensorDTO;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.AbstractResultForm;
import phis2ws.service.view.brapi.form.ResponseFormGET;
import phis2ws.service.view.brapi.form.ResponseFormPOST;
import phis2ws.service.view.brapi.form.ResponseFormSensor;
import phis2ws.service.view.model.phis.Sensor;

/**
 * sensor service 
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
@Api("/sensors")
@Path("/sensors")
public class SensorResourceService {
    final static Logger LOGGER = LoggerFactory.getLogger(SensorResourceService.class);
    
    //user session
    @SessionInject
    Session userSession;
    
    /**
     * 
     * @param getResponse
     * @param insertStatusList
     * @return the response "no result found" for the service
     */
    private Response noResultFound(ResponseFormSensor getResponse, ArrayList<Status> insertStatusList) {
        insertStatusList.add(new Status(StatusCodeMsg.NO_RESULTS, StatusCodeMsg.INFO, "No results for the sensors"));
        getResponse.setStatus(insertStatusList);
        return Response.status(Response.Status.NOT_FOUND).entity(getResponse).build();
    }
    
    /**
     * Search sensors corresponding to search params given by a user
     * @param sensorDAOSesame
     * @return the sensors corresponding to the search
     */
    private Response getSensorsData(SensorDAOSesame sensorDAOSesame) {
        ArrayList<Sensor> sensors;
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormSensor getResponse;
        
        sensors = sensorDAOSesame.allPaginate();
        
        if (sensors == null) {
            getResponse = new ResponseFormSensor(0, 0, sensors, true);
            return noResultFound(getResponse, statusList);
        } else if (sensors.isEmpty()) {
            getResponse = new ResponseFormSensor(0, 0, sensors, true);
            return noResultFound(getResponse, statusList);
        } else {
            getResponse = new ResponseFormSensor(sensorDAOSesame.getPageSize(), sensorDAOSesame.getPage(), sensors, false);
            if (getResponse.getResult().dataSize() == 0) {
                return noResultFound(getResponse, statusList);
            } else {
                getResponse.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(getResponse).build();
            }
        }
    }    
    
    /**
     * search sensors by uri, rdfType, label, brand, in service date, 
     * date of purchase and date of last calibration. 
     * 
     * @param pageSize
     * @param page
     * @param uri
     * @param rdfType
     * @param label
     * @param brand
     * @param serialNumber
     * @param inServiceDate
     * @param dateOfPurchase
     * @param dateOfLastCalibration
     * @param personInCharge
     * @return list of the sensors corresponding to the search params given
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
     *                  "uri": "http://www.phenome-fppn.fr/diaphen/2018/s18001",
     *                  "rdfType": "http://www.phenome-fppn.fr/vocabulary/2017#LevelMeasurementRainGauge",
     *                  "label": "alias",
     *                  "brand": "brand",
     *                  "serialNumber": "E1ISHFUSK2345",
     *                  "inServiceDate": null,
     *                  "dateOfPurchase": null,
     *                  "dateOfLastCalibration": null,
     *                  "personInCharge": "username@mail.com"
     *              },
     *          ]
     *      }
     * }
     */
    @GET
    @ApiOperation(value = "Get all sensors corresponding to the search params given",
                  notes = "Retrieve all sensors authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all sensors", response = Sensor.class, responseContainer = "List"),
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
    public Response getSensorsBySearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int pageSize,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page,
            @ApiParam(value = "Search by uri", example = DocumentationAnnotation.EXAMPLE_SENSOR_URI) @QueryParam("uri") String uri,
            @ApiParam(value = "Search by type uri", example = DocumentationAnnotation.EXAMPLE_SENSOR_RDF_TYPE) @QueryParam("rdfType") String rdfType,
            @ApiParam(value = "Search by label", example = DocumentationAnnotation.EXAMPLE_SENSOR_LABEL) @QueryParam("label") String label,
            @ApiParam(value = "Search by brand", example = DocumentationAnnotation.EXAMPLE_SENSOR_BRAND) @QueryParam("brand") String brand,
            @ApiParam(value = "Search by serial number", example = DocumentationAnnotation.EXAMPLE_SENSOR_SERIAL_NUMBER) @QueryParam("serialNumber") String serialNumber,
            @ApiParam(value = "Search by service date", example = DocumentationAnnotation.EXAMPLE_SENSOR_IN_SERVICE_DATE) @QueryParam("inServiceDate") String inServiceDate,
            @ApiParam(value = "Search by date of purchase", example = DocumentationAnnotation.EXAMPLE_SENSOR_DATE_OF_PURCHASE) @QueryParam("dateOfPurchase") String dateOfPurchase,
            @ApiParam(value = "Search by date of last calibration", example = DocumentationAnnotation.EXAMPLE_SENSOR_DATE_OF_LAST_CALIBRATION) @QueryParam("dateOfLastCalibration") String dateOfLastCalibration,
            @ApiParam(value = "Search by person in charge", example = DocumentationAnnotation.EXAMPLE_USER_EMAIL) @QueryParam("personInCharge") String personInCharge) {
        
        SensorDAOSesame sensorDAO = new SensorDAOSesame();
        if (uri != null) {
            sensorDAO.uri = uri;
        }
        if (rdfType != null) {
            sensorDAO.rdfType = rdfType;
        }
        if (label != null) {
            sensorDAO.label = label;
        }
        if (brand != null) {
            sensorDAO.brand = brand;
        }
        if (serialNumber != null) {
            sensorDAO.serialNumber = serialNumber;
        }
        if (inServiceDate != null) {
            sensorDAO.inServiceDate = inServiceDate;
        }
        if (dateOfPurchase != null) {
            sensorDAO.dateOfPurchase = dateOfPurchase;
        }
        if (dateOfLastCalibration != null) {
            sensorDAO.dateOfLastCalibration = dateOfLastCalibration;
        }
        if (personInCharge != null) {
            sensorDAO.personInCharge = personInCharge;
        }
        
        sensorDAO.user = userSession.getUser();
        sensorDAO.setPage(page);
        sensorDAO.setPageSize(pageSize);
        
        return getSensorsData(sensorDAO);
    }

    /**
     * get the informations about a sensor
     * @param uri
     * @param pageSize
     * @param page
     * @return the informations about the sensor if it exists
     * e.g.
     * {
     *      "metadata": {
     *          "pagination": null,
     *          "status": [],
     *          "datafiles": []
     *      },
     *      "result": {
     *          "data": [
     *              {
     *                 "uri": "http://www.phenome-fppn.fr/diaphen/2018/s18025",
     *                 "rdfType": "http://www.phenome-fppn.fr/vocabulary/2017#HumiditySensor",
     *                 "label": "aria_hr1_p",
     *                 "brand": "unknown",
     *                 "serialNumber": null,
     *                 "inServiceDate": null,
     *                 "dateOfPurchase": null,
     *                 "dateOfLastCalibration": null,
     *                 "personInCharge": "user@mail.fr"
     *              }
     *          ]
     *      }
     * }
     */
    @GET
    @Path("{uri}")
    @ApiOperation(value = "Get a sensor",
                  notes = "Retrieve a sensor. Need URL encoded sensor URI")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve a sensor", response = Sensor.class, responseContainer = "List"),
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
    public Response getSensorDetails(
        @ApiParam(value = DocumentationAnnotation.SENSOR_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_SENSOR_URI) @PathParam("uri") String uri,
        @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam(GlobalWebserviceValues.PAGE_SIZE) @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int pageSize,
        @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam(GlobalWebserviceValues.PAGE) @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page) {

        if (uri == null) {
            final Status status = new Status(StatusCodeMsg.ACCESS_ERROR, StatusCodeMsg.ERR, "Empty sensor uri");
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseFormGET(status)).build();
        }

        SensorDAOSesame sensorDAO = new SensorDAOSesame();
        sensorDAO.uri = uri;
        sensorDAO.setPage(page);
        sensorDAO.setPageSize(pageSize);
        sensorDAO.user = userSession.getUser();

        return getSensorsData(sensorDAO);
    }
    
    /**
     * insert sensors in the database(s)
     * @param sensors list of sensors to insert.
     *                e.g.
     * {
     *      "rdfType": "http://www.phenome-fppn.fr/vocabulary/2017#Thermocouple",
     *      "label": "tcorg0001",
     *      "brand": "Homemade",
     *      "serialNumber": "A1E345F32",
     *      "inServiceDate": "2017-06-15",
     *      "dateOfPurchase": "2017-06-15",
     *      "dateOfLastCalibration": "2017-06-15",
     *      "personInCharge": "morgane.vidal@inra.fr"
     * }
     * @param context
     * @return the post result with the errors or the uri of the inserted sensors
     */
    @POST
    @ApiOperation(value = "Post a sensor",
                  notes = "Register a new sensor in the database")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Sensor saved", response = ResponseFormPOST.class),
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
    public Response post(
        @ApiParam (value = DocumentationAnnotation.SENSOR_POST_DEFINITION) ArrayList<SensorDTO> sensors,
        @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        
        if (sensors != null && !sensors.isEmpty()) {
            SensorDAOSesame sensorDAOSesame = new SensorDAOSesame();
            
            if (context.getRemoteAddr() != null) {
                sensorDAOSesame.remoteUserAdress = context.getRemoteAddr();
            }
            
            sensorDAOSesame.user = userSession.getUser();
            
            POSTResultsReturn result = sensorDAOSesame.checkAndInsert(sensors);
            
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
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, "Empty sensor(s) to add"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
    
    /**
     * update the given sensors
     * e.g. 
     * [
     *      {
     *          "uri": "http://www.phenome-fppn.fr/diaphen/2018/s18142",
     *          "rdfType": "http://www.phenome-fppn.fr/vocabulary/2017#Thermocouple",
     *          "label": "testNewLabel",
     *          "brand": "Skye Instrdfgduments",
     *          "serialNumber": "A1E34qsf5F32",
     *          "inServiceDate": "2017-06-15",
     *          "dateOfPurchase": "2017-06-15",
     *          "dateOfLastCalibration": "2017-06-15",
     *          "personInCharge": "morgane.vidal@inra.fr"
     *      }
     * ]
     * @param sensors
     * @param context
     * @return the post result with the founded errors or the uris of the updated sensors
     */
    @PUT
    @ApiOperation(value = "Update sensors")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Sensor(s) updated", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 404, message = "Sensor(s) not found"),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = GlobalWebserviceValues.AUTHORIZATION, required = true,
                dataType = GlobalWebserviceValues.DATA_TYPE_STRING, paramType = GlobalWebserviceValues.HEADER,
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    public Response put(
        @ApiParam(value = DocumentationAnnotation.VECTOR_POST_DEFINITION) ArrayList<SensorDTO> sensors,
        @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        
        if (sensors != null && !sensors.isEmpty()) {
            SensorDAOSesame sensorDAOSesame = new SensorDAOSesame();
            if (context.getRemoteAddr() != null) {
                sensorDAOSesame.remoteUserAdress = context.getRemoteAddr();
            }
            
            sensorDAOSesame.user = userSession.getUser();
            
            POSTResultsReturn result = sensorDAOSesame.checkAndUpdate(sensors);
            
            if (result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.CREATED)) {
                //Code 200, traits modifiés
                postResponse = new ResponseFormPOST(result.statusList);
                postResponse.getMetadata().setDatafiles(result.createdResources);
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                    || result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status(StatusCodeMsg.REQUEST_ERROR, StatusCodeMsg.ERR, "Empty sensors(s) to update"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
}
