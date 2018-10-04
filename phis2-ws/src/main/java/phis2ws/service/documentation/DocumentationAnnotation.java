//**********************************************************************************************
//                                       DocumentationAnnotation.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal, Eloan LAGIER
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, eloan.lagier@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  Janvier, 25 2018
// Subject: A class which group documentation informations ( try message bundle for the next version)
//***********************************************************************************************
package phis2ws.service.documentation;

import javax.inject.Singleton;
import phis2ws.service.PropertiesFileManager;

/**
 * A class which group documentation informations
 *
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
@Singleton
public final class DocumentationAnnotation {

    //Default page number is 0
    public static final String PAGE = "Current page number";
    /**
     * To be updated if the variable in service.properties is updated
     *
     * @see service.properties
     */
    public static final String PAGE_SIZE = "Number of elements per page (limited to 150000)";

    public static final String PAGE_SIZE_MONGO = "Number of elements per page (limited to 1000000)";

    // user messages
    public static final String ERROR_SEND_DATA = "Server error. Cannot send data.";
    public static final String ERROR_FETCH_DATA = "Server error. Cannot fetch data.";
    public static final String BAD_USER_INFORMATION = "Bad informations send by user";
    public static final String ACCES_TOKEN = "Access token given";
    public static final String SQL_ERROR_FETCH_DATA = "SQL Error can't fetch results";
    public static final String NO_RESULTS = "No data found";
    public static final String USER_NOT_AUTHORIZED = "You aren't authorized to fetch the result of this ressource call";

    public static final String SWAGGER_DOCUMENTATION_HEADER
            = "This page describes the methods allowed by this web service. <p style=\"color: red;\"> You must read the paragraph below before use it !</p>"
            + "<br>"
            + "<ol>"
            + "<li>"
            + "<b> 1. You must first retrieve an acces token using the \"token\" call (fill with your PHIS-SILEX username and password)</b> and after you will be able to use other service calls.</li>"
            + "<li>"
            + "<b> 2. You must fill the sessionId parameter</b> with the created access token on each call. <p style=\"color: red;\">This token is available during " + Integer.valueOf(PropertiesFileManager.getConfigFileProperty("service", "sessionTime")) + " seconds.</p> This time will be reload at each use in order to keep the token valid without retrieve a new one.</li>"
            + "</ol>"
            + "<br>"
            + "<i>The response call <b>example values shown</b> in this api documentation represent the <b>data array</b> which is located <b>in the response result object</b> <p style=\"color: red;\">except for the token call.</p><i/><br>"
            + "<b>The token also include the response object header. </b>"
            + "For more information, the <b>Response object definition</b> is available at <b><a href=\"http://docs.brapi.apiary.io/#introduction/structure-of-the-response-object:\">Brapi response object</a></b>.";

    public static final String EXPERIMENT_URI_DEFINITION = "An experiment URI (Unique Resource Identifier)";
    public static final String EXPERIMENT_POST_DATA_DEFINITION = "JSON format of experiment data";

    public static final String PROJECT_URI_DEFINITION = "A project URI (Unique Resource Identifier)";
    public static final String PROJECT_POST_DATA_DEFINITION = "JSON format of project data";

    public static final String AGRONOMICAL_OBJECT_URI_DEFINITION = "Agronomical object URI (Unique Resource Identifier)";
    public static final String AGRONOMICAL_OBJECT_POST_DATA_DEFINITION = "JSON format of agronomical object data";

    public static final String GROUP_URI_DEFINITION = "A group uri";
    public static final String GROUP_POST_DATA_DEFINITION = "JSON format of group data";

    public static final String USER_EMAIL_DEFINITION = "A user email";
    public static final String USER_POST_DATA_DEFINITION = "JSON format of user data";

    public static final String VARIABLE_POST_DATA_DEFINITION = "JSON format of variable data";
    public static final String VARIABLE_URI_DEFINITION = "A variable URI (Unique Resource Identifier)";

    public static final String TRAIT_POST_DATA_DEFINITION = "JSON format of trait";
    public static final String TRAIT_URI_DEFINITION = "A trait URI (Unique Resource Identifier)";

    public static final String TRIPLET_POST_DATA_DEFINITION = "JSON format of a triplet";

    public static final String ANNOTATION_POST_DATA_DEFINITION = "JSON format of an annotation";

    public static final String METHOD_POST_DATA_DEFINITION = "JSON format of method";
    public static final String METHOD_URI_DEFINITION = "A method URI (Unique Resource Identifier)";

    public static final String UNIT_POST_DATA_DEFINITION = "JSON format of unit";
    public static final String UNIT_URI_DEFINITION = "A unit URI (Unique Resource Identifier)";

    public static final String SENSOR_URI_DEFINITION = "a sensor URI (Unique Resource Identifier)";
    public static final String SENSOR_POST_DEFINITION = "JSON format of sensor data";
    public static final String SENSOR_PROFILE_POST_DEFINITION = "JSON format of sensor profile data";

    public static final String VARIABLES_DEFINITION = "A variable or comma-separated variables list";

    public static final String VECTOR_POST_DEFINITION = "JSON format of vector data";
    public static final String VECTOR_RDF_TYPE_DEFINITION = "A vector rdf type URI";

    public static final String ADMIN_ONLY_NOTES = "This can only be done by a PHIS-SILEX admin.";
    public static final String USER_ONLY_NOTES = "This can only be done by a PHIS-SILEX user.";

    public static final String DOCUMENT_URI_DEFINITION = "A document URI (Unique Resource Identifier)";

    public static final String LAYER_POST_DATA_DEFINITION = "JSON format of requested layer";

    public static final String RAW_DATA_POST_DATA_DEFINITION = "JSON format of raw data";

    
    public static final String CONCEPT_URI_DEFINITION = "A concept URI (Unique Resource Identifier)";
    public static final String DEEP ="true or false deppending if you want instances of concept progenity";
    
    public static final String CALL_DATATYPE_DEFINITION = "The data type supported by the call";

    //Global examples
    public static final String EXAMPLE_DATETIME = "2017-06-15 10:51:00+0200";
    public static final String EXAMPLE_XSDDATETIME = "2017-06-15T10:51:00+0200";
    public static final String EXAMPLE_DATE = "2017-06-15";

    //Specific examples
    public static final String EXAMPLE_EXPERIMENT_URI = "http://phenome-fppn.fr/diaphen/DIA2012-1";
    public static final String EXAMPLE_EXPERIMENT_START_DATE = EXAMPLE_DATETIME;
    public static final String EXAMPLE_EXPERIMENT_END_DATE = EXAMPLE_DATETIME;
    public static final String EXAMPLE_EXPERIMENT_FIELD = "field";
    public static final String EXAMPLE_EXPERIMENT_PLACE = "place";
    public static final String EXAMPLE_EXPERIMENT_ALIAS = "alias";
    public static final String EXAMPLE_EXPERIMENT_KEYWORDS = "keywords";
    public static final String EXAMPLE_EXPERIMENT_CAMPAIGN = "2012";

    public static final String EXAMPLE_FILE_INFORMATION_CHECKSUM = "106fa487baa1728083747de1c6df73e9";
    public static final String EXAMPLE_FILE_INFORMATION_EXTENSION = "jpg";

    public static final String EXAMPLE_AGRONOMICAL_OBJECT_URI = "http://www.phenome-fppn.fr/phenovia/2017/o1032481";
    public static final String EXAMPLE_AGRONOMICAL_OBJECT_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#Plot";

    public static final String EXAMPLE_IMAGE_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#HemisphericalImage";
    public static final String EXAMPLE_IMAGE_URI = "http://www.phenome-fppn.fr/phis_field/2017/i170000000000";
    public static final String EXAMPLE_IMAGE_DATE = EXAMPLE_DATETIME;
    public static final String EXAMPLE_IMAGE_CONCERNED_ITEMS = EXAMPLE_AGRONOMICAL_OBJECT_URI + ";" + EXAMPLE_AGRONOMICAL_OBJECT_URI;

    public static final String EXAMPLE_PROJECT_URI = "http://phenome-fppn.fr/phis_field/projectTest";
    public static final String EXAMPLE_PROJECT_NAME = "projectTest";
    public static final String EXAMPLE_PROJECT_ACRONYME = "P T";
    public static final String EXAMPLE_PROJECT_SUBPROJECT_TYPE = "subproject type";
    public static final String EXAMPLE_PROJECT_FINANCIAL_SUPPORT = "financial support";
    public static final String EXAMPLE_PROJECT_FINANCIAL_NAME = "financial name";
    public static final String EXAMPLE_PROJECT_DATE_START = "2015-07-07";
    public static final String EXAMPLE_PROJECT_DATE_END = "2016-07-07";
    public static final String EXAMPLE_PROJECT_KEYWORDS = "keywords";
    public static final String EXAMPLE_PROJECT_PARENT_PROJECT = "parent project";
    public static final String EXAMPLE_PROJECT_WEBSITE = "http://example.com";
    public static final String EXAMPLE_PROJECT_TYPE = "project type";

    public static final String EXAMPLE_PROVENANCE_URI = "http://www.phenome-fppn.fr/mtp/2018/pv181515071552";
    public static final String EXAMPLE_PROVENANCE_DATE = EXAMPLE_DATE;

    public static final String EXAMPLE_GROUP_URI = "http://phenome-fppn.fr/mauguio/INRA-MISTEA-GAMMA";
    public static final String EXAMPLE_GROUP_NAME = "INRA-MISTEA-GAMMA";
    public static final String EXAMPLE_GROUP_LEVEL = "Owner";
    public static final String EXAMPLE_GROUP_AVAILABLE = "true";

    public static final String EXAMPLE_USER_EMAIL = "admin@phis.fr";
    public static final String EXAMPLE_USER_PASSWORD = "21232f297a57a5a743894a0e4a801fc3";
    public static final String EXAMPLE_USER_FIRST_NAME = "Marie";
    public static final String EXAMPLE_USER_FAMILY_NAME = "Dupont";
    public static final String EXAMPLE_USER_ADDRESS = "2 place Pierre Viala, Montpellier";
    public static final String EXAMPLE_USER_PHONE = "0400000000";
    public static final String EXAMPLE_USER_AFFILIATION = "affiliation";
    public static final String EXAMPLE_USER_ORCID = "orcid";
    public static final String EXAMPLE_USER_ADMIN = "true";
    public static final String EXAMPLE_USER_AVAILABLE = "true";
    public static final String EXAMPLE_USER_URI = "http://www.phenome-fppn.fr/diaphen/id/agent/marie_dupond";

    public static final String EXAMPLE_DATA_INCERTITUDE = "0.4";
    public static final String EXAMPLE_DATA_VALUE = "3.0000000";

    public static final String EXAMPLE_DOCUMENT_URI = "http://www.phenome-fppn.fr/phis_field/documents/documente597f57ba71d421a86277d830f4b9885";
    public static final String EXAMPLE_DOCUMENT_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#ScientificDocument";
    public static final String EXAMPLE_DOCUMENT_CREATOR = "John Doe";
    public static final String EXAMPLE_DOCUMENT_LANGUAGE = "fr";
    public static final String EXAMPLE_DOCUMENT_CREATION_DATE = "2017-07-07";
    public static final String EXAMPLE_DOCUMENT_EXTENSION = "png";
    public static final String EXAMPLE_DOCUMENT_TITLE = "title";
    public static final String EXAMPLE_DOCUMENT_CONCERNED_TYPE_URI = "http://www.phenome-fppn.fr/vocabulary/2017#Experiment";
    public static final String EXAMPLE_DOCUMENT_STATUS = "linked";

    public static final String EXAMPLE_SENSOR_URI = "http://www.phenome-fppn.fr/diaphen/2018/s18001";
    public static final String EXAMPLE_SENSOR_RDF_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#Sensor";
    public static final String EXAMPLE_SENSOR_LABEL = "par03_p";
    public static final String EXAMPLE_SENSOR_BRAND = "Skye Instruments";
    public static final String EXAMPLE_SENSOR_VARIABLE = "http://www.phenome-fppn.fr/phenovia/id/variables/v001";
    public static final String EXAMPLE_SENSOR_IN_SERVICE_DATE = EXAMPLE_DATE;
    public static final String EXAMPLE_SENSOR_DATE_OF_PURCHASE = EXAMPLE_DATE;
    public static final String EXAMPLE_SENSOR_DATE_OF_LAST_CALIBRATION = EXAMPLE_DATE;
    public static final String EXAMPLE_SENSOR_SERIAL_NUMBER = "A1E345F32";

    public static final String EXAMPLE_SHOOTING_CONFIGURATION_TIMESTAMP = "1512744238";
    public static final String EXAMPLE_SHOOTING_CONFIGURATION_POSITION = "POINT(0, 0)";
    public static final String EXAMPLE_SHOOTING_CONFIGURATION_DATE = EXAMPLE_EXPERIMENT_START_DATE;

    public static final String EXAMPLE_SPECIES_RDF_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#Species";
    public static final String EXAMPLE_SPECIES_URI = "http://www.phenome-fppn.fr/id/species/maize";
    public static final String EXAMPLE_SPECIES_FROM_SPECIES = "http://www.phenome-fppn.fr/vocabulary/2017#fromSpecies";

    public static final String EXAMPLE_TRAIT_URI = "http://www.phenome-fppn.fr/phis_field/id/traits/t001";
    public static final String EXAMPLE_TRAIT_LABEL = "Height";

    public static final String EXAMPLE_TRIPLET_SUBJECT = "http://www.phenome-fppn.fr/diaphen/DIA2018-2";
    public static final String EXAMPLE_TRIPLET_PROPERTY = "http://www.phenome-fppn.fr/vocabulary/2017#hasDocument";
    public static final String EXAMPLE_TRIPLET_OBJECT = "http://www.phenome-fppn.fr/phis_field/documents/documente597f57ba71d421a86277d830f4b9885";
    public static final String EXAMPLE_TRIPLET_OBJECT_LANGUAGE = "en-US";
    public static final String EXAMPLE_TRIPLET_OBJECT_TYPE = "uri";
    public static final String EXAMPLE_TRIPLET_GRAPH = "http://www.phenome-fppn.fr/diaphen/DIA2018-2";

    public static final String EXAMPLE_METHOD_URI = "http://www.phenome-fppn.fr/phis_field/id/methods/m001";
    public static final String EXAMPLE_METHOD_LABEL = "comptage";

    public static final String EXAMPLE_UNIT_URI = "http://www.phenome-fppn.fr/phis_field/id/units/u001";
    public static final String EXAMPLE_UNIT_LABEL = "cm";

    public static final String EXAMPLE_VARIABLE_URI = "http://www.phenome-fppn.fr/diaphen/id/variable/v0000001";
    public static final String EXAMPLE_VARIABLE_LABEL = "LAI";

    public static final String EXAMPLE_VECTOR_URI = "http://www.phenome-fppn.fr/diaphen/2018/v1801";
    public static final String EXAMPLE_VECTOR_RDF_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#UAV";
    public static final String EXAMPLE_VECTOR_LABEL = "par03_p";
    public static final String EXAMPLE_VECTOR_BRAND = "Skye Instruments";
    public static final String EXAMPLE_VECTOR_SERIAL_NUMBER = "A1E345F32";
    public static final String EXAMPLE_VECTOR_IN_SERVICE_DATE = EXAMPLE_DATE;
    public static final String EXAMPLE_VECTOR_DATE_OF_PURCHASE = EXAMPLE_DATE;
    public static final String EXAMPLE_VECTOR_PERSON_IN_CHARGE = EXAMPLE_USER_EMAIL;

    public static final String EXAMPLE_CONCEPT_URI = "http://www.phenome-fppn.fr/vocabulary/2017#Document";
    public static final String EXAMPLE_DEEP = "true";
    public static final String EXAMPLE_SIBLING_URI = "http://www.phenome-fppn.fr/vocabulary/2017#ScientificDocument";

    public static final String EXAMPLE_CONCEPT_LABEL = "'document'@en";

    public static final String EXAMPLE_RDFTYPE_URI = "http://www.phenome-fppn.fr/vocabulary/2017#ScientificDocument";
    public static final String EXAMPLE_INSTANCE_URI = "http://www.phenome-fppn.fr/phenovia/documents/document90fb96ace2894cdb9f4575173d8ed4c9";

    public static final String EXAMPLE_WAS_GENERATED_BY_DOCUMENT = EXAMPLE_DOCUMENT_URI;
    public static final String EXAMPLE_WAS_GENERATED_BY_DESCRIPTION = "Phenoscript v1.3";

    public static final String EXAMPLE_TOKEN_JWT_CLIENTID = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJQaGlzIiwic3ViIjoibW9yZ2FuZS52aWRhbEBpbnJhLmZyIiwiaWF0IjoxNTIwMjU5NjY5LCJleHAiOjE1MjM4NTYwNjl9.PjdxAb_U8fJaR6XwkNBa011XLFMM8psi0ym5VpfTV_fLkkIp6ZKFDOM-kTFAjNfPSR8tVUiU90pTkKSje9Ib9T629gA7Xw-8006smUL-n-ZIaQZ18mxzVb6jsVP6tOcJjjlucAmWZIhLIcT1e1KbOlBfToHpdRgOhDWkdkwENVc7n2TAcq-eLIJpcwQeDsCel7Ea2hssCxg4p8jCs68S43wg2mPTudTclW5_Q3HzKpf-DmPBFf6MMZmPtEAqi2aFfOoLp_8GDh92ywPOHJUdwvT9UkL87ELM7j5C8zrDE_CZAC-1IZdINE1KTTp36sScCJBsbm5DGLeIWF8g0e1vug";
    public static final String EXAMPLE_TOKEN_JWT_GRANTTYPE = "jwt";
    
    public static final String EXAMPLE_CALL_DATATYPE = "json";
  
    public static final String EXAMPLE_ANNOTATION_URI = "http://www.phenome-fppn.fr/platform/id/annotation/8247af37-769c-495b-8e7e-78b1141176c2";
    public static final String EXAMPLE_ANNOTATION_TARGET = "http://www.phenome-fppn.fr/diaphen/id/variable/v0000001";
    public static final String EXAMPLE_ANNOTATION_MOTIVATEDBY = "http://www.w3.org/ns/oa#commenting";
    public static final String EXAMPLE_ANNOTATION_COMMENT = "Ustilago maydis infection";
    public static final String EXAMPLE_ANNOTATION_CREATOR = "http://www.phenome-fppn.fr/diaphen/id/agent/marie_dupond";
  
    public static final String EXAMPLE_EXPERIMENT_CROP_SPECIES = "maize";
    
    // API global parameter
    public static final String EXAMPLE_SORTING_ALLOWABLE_VALUES = "asc,desc";

    public static final String EXAMPLE_INFRASTRUCTURE_URI = "http://www.phenome-fppn.fr/diaphen";
    public static final String EXAMPLE_INFRASTRUCTURE_RDF_TYPE = "http://www.phenome-fppn.fr/vocabulary/2017#Infrastructure";
    public static final String EXAMPLE_INFRASTRUCTURE_LABEL = "EMPHASIS";

}