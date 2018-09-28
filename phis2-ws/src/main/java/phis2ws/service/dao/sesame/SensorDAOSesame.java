//******************************************************************************
//                                       SensorDAOSesame.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 9 mars 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.dao.sesame;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.dao.manager.DAOSesame;
import phis2ws.service.dao.phis.UserDaoPhisBrapi;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.model.User;
import phis2ws.service.ontologies.Contexts;
import phis2ws.service.ontologies.Rdf;
import phis2ws.service.ontologies.Rdfs;
import phis2ws.service.ontologies.Vocabulary;
import phis2ws.service.resources.dto.SensorDTO;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.utils.UriGenerator;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.utils.sparql.SPARQLUpdateBuilder;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.model.phis.Sensor;

/**
 * allows CRUD methods of sensors in the triplestore rdf4j
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class SensorDAOSesame extends DAOSesame<Sensor> {

    final static Logger LOGGER = LoggerFactory.getLogger(SensorDAOSesame.class);

    //The following attributes are used to search sensors in the triplestore
    //uri of the sensor
    public String uri;
    //type uri of the sensor(s)
    public String rdfType;
    //alias of the sensor(s)
    public String label;
    //brand of the sensor(s)
    public String brand;
    private final String BRAND = "brand";
    //serial number of the sensor(s)
    public String serialNumber;
    private final String SERIAL_NUMBER = "serialNumber";
    //service date of the sensor(s)
    public String inServiceDate;
    private final String IN_SERVICE_DATE = "inServiceDate";
    //date of purchase of the sensor(s)
    public String dateOfPurchase;
    private final String DATE_OF_PURCHASE = "dateOfPurchase";
    //date of last calibration of the sensor(s)
    public String dateOfLastCalibration;
    private final String DATE_OF_LAST_CALIBRATION = "dateOfLastCalibration";
    //person in charge of the sensor(s)
    public String personInCharge;
    private final String PERSON_IN_CHARGE = "personInCharge";

    /**
     * prepare a query to get the higher id of the sensors
     * @return 
     */
    private SPARQLQueryBuilder prepareGetLastIdFromYear(String year) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        
        query.appendSelect("?" + URI);
        query.appendTriplet("?" + URI, Rdf.RELATION_TYPE.toString(), "?type", null);
        query.appendTriplet("?type", "<" + Rdfs.RELATION_SUBCLASS_OF.toString() + ">*", Vocabulary.CONCEPT_SENSING_DEVICE.toString(), null);
        query.appendFilter("regex(str(?uri), \".*/" + year + "/.*\")");
        query.appendOrderBy("desc(?uri)");
        query.appendLimit(1);
        
        LOGGER.debug(query.toString());
        
        return query;
    }
    
    /**
     * get the higher existing id of the sensor for a given year
     * @param year
     * @return the id
     */
    public int getLastIdFromYear(String year) {
        SPARQLQueryBuilder query = prepareGetLastIdFromYear(year);

        //get last sensor uri inserted
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        TupleQueryResult result = tupleQuery.evaluate();

        getConnection().close();
        
        String uriSensor = null;
        
        if (result.hasNext()) {
            BindingSet bindingSet = result.next();
            uriSensor = bindingSet.getValue(URI).stringValue();
        }
        
        if (uriSensor == null) {
            return 0;
        } else {
            //2018 -> 18. to get /s18
            String split = "/s" + year.substring(2, 4);
            String[] parts = uriSensor.split(split);
            if (parts.length > 1) {
                return Integer.parseInt(parts[1]);
            } else {
                return 0;
            }
        }
    }
    
    /**
     * generates a search query (search by uri, type, label, brand, variable,
     * inServiceDate, dateOfPurchase, dateOfLastCalibration)
     * @return the query to execute.
     * e.g.
     */
    @Override
    protected SPARQLQueryBuilder prepareSearchQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String sensorUri;
        if (uri != null) {
            sensorUri = "<" + uri + ">";
        } else {
            sensorUri = "?" + URI;
            query.appendSelect(sensorUri);
        }
        
        if (rdfType != null) {
            query.appendTriplet(sensorUri, Rdf.RELATION_TYPE.toString(), rdfType, null);
        } else {
            query.appendSelect("?" + RDF_TYPE);
            query.appendTriplet(sensorUri, Rdf.RELATION_TYPE.toString(), "?" + RDF_TYPE, null);
            query.appendTriplet("?" + RDF_TYPE, "<" + Rdfs.RELATION_SUBCLASS_OF.toString() + ">*", Vocabulary.CONCEPT_SENSING_DEVICE.toString(), null);
        }        

        if (label != null) {
            query.appendTriplet(sensorUri, Rdfs.RELATION_LABEL.toString(), "\"" + label + "\"", null);
        } else {
            query.appendSelect(" ?" + LABEL);
            query.beginBodyOptional();
            query.appendToBody(sensorUri + " <" + Rdfs.RELATION_LABEL.toString() + "> " + "?" + LABEL + " . ");
            query.endBodyOptional();
        }

        if (brand != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_HAS_BRAND.toString(), "\"" + brand + "\"", null);
        } else {
            query.appendSelect(" ?" + BRAND);
            query.appendTriplet(sensorUri, Vocabulary.RELATION_HAS_BRAND.toString(), "?" + BRAND, null);
        }
        
        if (serialNumber != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_SERIAL_NUMBER.toString(), "\"" + serialNumber + "\"", null);
        } else {
            query.appendSelect("?" + SERIAL_NUMBER);
            query.beginBodyOptional();
            query.appendToBody(sensorUri + " <" + Vocabulary.RELATION_SERIAL_NUMBER.toString() + "> ?" + SERIAL_NUMBER + " . ");
            query.endBodyOptional();
        }

        if (inServiceDate != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_IN_SERVICE_DATE.toString(), "\"" + inServiceDate + "\"", null);
        } else {
            query.appendSelect(" ?" + IN_SERVICE_DATE);
            query.beginBodyOptional();
            query.appendToBody(sensorUri + " <" + Vocabulary.RELATION_IN_SERVICE_DATE.toString() + "> " + "?" + IN_SERVICE_DATE + " . ");
            query.endBodyOptional();
        }

        if (dateOfPurchase != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_DATE_OF_PURCHASE.toString(), "\"" + dateOfPurchase + "\"", null);
        } else {
            query.appendSelect("?" + DATE_OF_PURCHASE);
            query.beginBodyOptional();
            query.appendToBody(sensorUri + " <" + Vocabulary.RELATION_DATE_OF_PURCHASE.toString() + "> " + "?" + DATE_OF_PURCHASE + " . ");
            query.endBodyOptional();
        }

        if (dateOfLastCalibration != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_DATE_OF_LAST_CALIBRATION.toString(), "\"" + dateOfLastCalibration + "\"", null);
        } else {
            query.appendSelect("?" + DATE_OF_LAST_CALIBRATION);
            query.beginBodyOptional();
            query.appendToBody(sensorUri + " <" + Vocabulary.RELATION_DATE_OF_LAST_CALIBRATION.toString() + "> " + "?" + DATE_OF_LAST_CALIBRATION + " . ");
            query.endBodyOptional();
        }
        
        if (personInCharge != null) {
            query.appendTriplet(sensorUri, Vocabulary.RELATION_PERSON_IN_CHARGE.toString(), "\"" + personInCharge + "\"", null);
        } else {
            query.appendSelect(" ?" + PERSON_IN_CHARGE);
            query.appendTriplet(sensorUri, Vocabulary.RELATION_PERSON_IN_CHARGE.toString(), "?" + PERSON_IN_CHARGE, null);
        }
        
        query.appendLimit(this.getPageSize());
        query.appendOffset(this.getPage() * this.getPageSize());

        LOGGER.debug(SPARQL_SELECT_QUERY + query.toString());
        return query;
    }

    /**
     * Count query generated by the searched parameters : uri, rdfType, 
     * label, brand, variable, inServiceDate, dateOfPurchase, dateOfLastCalibration
     * @example 
     * SELECT DISTINCT  (count(distinct ?uri) as ?count) 
     * WHERE {
     *      ?uri  ?0  ?rdfType  . 
     *      ?rdfType  rdfs:subClassOf*  <http://www.phenome-fppn.fr/vocabulary/2017#SensingDevice> . 
     *      OPTIONAL {
     *          ?uri rdfs:label ?label . 
     *      }
     *      ?uri  <http://www.phenome-fppn.fr/vocabulary/2017#hasBrand>  ?brand  . 
     *      OPTIONAL {
     *          ?uri <http://www.phenome-fppn.fr/vocabulary/2017#serialNumber> ?serialNumber . 
     *      }
     *      OPTIONAL {
     *          ?uri <http://www.phenome-fppn.fr/vocabulary/2017#inServiceDate> ?inServiceDate . 
     *      }
     *      OPTIONAL {
     *          ?uri <http://www.phenome-fppn.fr/vocabulary/2017#dateOfPurchase> ?dateOfPurchase . 
     *      }
     *      OPTIONAL {
     *          ?uri <http://www.phenome-fppn.fr/vocabulary/2017#dateOfLastCalibration> ?dateOfLastCalibration . 
     *      }
     *      ?uri  <http://www.phenome-fppn.fr/vocabulary/2017#personInCharge>  ?personInCharge  . 
     * }
     * @return Query generated to count the elements, with the searched parameters
     */
    private SPARQLQueryBuilder prepareCount() {
        SPARQLQueryBuilder query = this.prepareSearchQuery();
        query.clearSelect();
        query.clearLimit();
        query.clearOffset();
        query.clearGroupBy();
        query.appendSelect("(COUNT(DISTINCT ?" + URI + ") AS ?" + COUNT_ELEMENT_QUERY + ")");
        LOGGER.debug(SPARQL_SELECT_QUERY + " " + query.toString());
        return query;
    }
    
    /**
     * Count the number of sensors by the given searched params : uri, rdfType, 
     * label, brand, variable, inServiceDate, dateOfPurchase, dateOfLastCalibration
     * @return The number of sensors 
     * @inheritdoc
     */
    @Override
    public Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        SPARQLQueryBuilder prepareCount = prepareCount();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, prepareCount.toString());
        Integer count = 0;
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                count = Integer.parseInt(bindingSet.getValue(COUNT_ELEMENT_QUERY).stringValue());
            }
        }
        return count;
    }
    
    /**
     * Query to count the number of cameras in the triplestore
     * @example
     * SELECT  (count(distinct ?uri) as ?count) 
     * WHERE {
     *      ?rdfType  rdfs:subClassOf*  <http://www.phenome-fppn.fr/vocabulary/2017#Camera> . 
     *      ?uri  rdf:type  ?rdfType  . 
     *      ?uri  rdfs:label  ?label  . 
     * }
     * @return Query generated to count the elements
     */
    private SPARQLQueryBuilder prepareCountCameras() {
        SPARQLQueryBuilder query = this.prepareSearchCamerasQuery();
        query.clearSelect();
        query.clearLimit();
        query.clearOffset();
        query.clearGroupBy();
        query.clearOrderBy();
        query.appendSelect("(COUNT(DISTINCT ?" + URI + ") AS ?" + COUNT_ELEMENT_QUERY + ")");
        LOGGER.debug(SPARQL_SELECT_QUERY + " " + query.toString());
        return query;
    }
    
    /**
     * Count the number of cameras 
     * @return The number of cameras 
     * @inheritdoc
     */
    public Integer countCameras() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        SPARQLQueryBuilder prepareCount = prepareCountCameras();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, prepareCount.toString());
        Integer count = 0;
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                count = Integer.parseInt(bindingSet.getValue(COUNT_ELEMENT_QUERY).stringValue());
            }
        }
        return count;
    }

    /**
     * get a sensor from a given binding set.
     * Assume that the following attributes exist :
     * uri, rdfType, label, brand, variable, inServiceDate, dateOfPurchase,
     * dateOfLastCalibration
     * @param bindingSet a bindingSet from a search query
     * @return a sensor with data extracted from the given bindingSet
     */
    private Sensor getSensorFromBindingSet(BindingSet bindingSet) {
        Sensor sensor = new Sensor();

        if (uri != null) {
            sensor.setUri(uri);
        } else if (bindingSet.getValue(URI) != null) {
            sensor.setUri(bindingSet.getValue(URI).stringValue());
        }

        if (rdfType != null) {
            sensor.setRdfType(rdfType);
        } else if (bindingSet.getValue(RDF_TYPE) != null) {
            sensor.setRdfType(bindingSet.getValue(RDF_TYPE).stringValue());
        }

        if (label != null) {
            sensor.setLabel(label);
        } else if (bindingSet.getValue(LABEL) != null ){
            sensor.setLabel(bindingSet.getValue(LABEL).stringValue());
        }

        if (brand != null) {
            sensor.setBrand(brand);
        } else if (bindingSet.getValue(BRAND) != null) {
            sensor.setBrand(bindingSet.getValue(BRAND).stringValue());
        }
        
        if (serialNumber != null) {
            sensor.setSerialNumber(serialNumber);
        } else if (bindingSet.getValue(SERIAL_NUMBER) != null) {
            sensor.setSerialNumber(bindingSet.getValue(SERIAL_NUMBER).stringValue());
        }

        if (inServiceDate != null) {
            sensor.setInServiceDate(inServiceDate);
        } else if (bindingSet.getValue(IN_SERVICE_DATE) != null) {
            sensor.setInServiceDate(bindingSet.getValue(IN_SERVICE_DATE).stringValue());
        }

        if (dateOfPurchase != null) {
            sensor.setDateOfPurchase(dateOfPurchase);
        } else if (bindingSet.getValue(DATE_OF_PURCHASE) != null) {
            sensor.setDateOfPurchase(bindingSet.getValue(DATE_OF_PURCHASE).stringValue());
        }

        if (dateOfLastCalibration != null) {
            sensor.setDateOfLastCalibration(dateOfLastCalibration);
        } else if (bindingSet.getValue(DATE_OF_LAST_CALIBRATION) != null) {
            sensor.setDateOfLastCalibration(bindingSet.getValue(DATE_OF_LAST_CALIBRATION).stringValue());
        }
        
        if (personInCharge != null) {
            sensor.setPersonInCharge(personInCharge);
        } else if (bindingSet.getValue(PERSON_IN_CHARGE) != null) {
            sensor.setPersonInCharge(bindingSet.getValue(PERSON_IN_CHARGE).stringValue());
        }

        return sensor;
    }
    
    /**
     * search all the sensors corresponding to the search params given by the user
     * @return the list of the sensors which match the given search params.
     */
    public ArrayList<Sensor> allPaginate() {
        SPARQLQueryBuilder query = prepareSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Sensor> sensors = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Sensor sensor = getSensorFromBindingSet(bindingSet);
                sensors.add(sensor);
            }
        }
        return sensors;
    }
    
    private SPARQLQueryBuilder prepareIsSensorQuery(String uri) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendTriplet("<" + uri + ">", Rdf.RELATION_TYPE.toString(), "?" + RDF_TYPE, null);
        query.appendTriplet("?" + RDF_TYPE, "<" + Rdfs.RELATION_SUBCLASS_OF.toString() + ">*", Vocabulary.CONCEPT_SENSING_DEVICE.toString(), null);
        
        query.appendAsk("");
        LOGGER.debug(query.toString());
        return query;
    }
    
    private boolean isSensor(String uri) {
        SPARQLQueryBuilder query = prepareIsSensorQuery(uri);
        BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
        
        return booleanQuery.evaluate();
    }
     
    /**
     * check if a given uri is a sensor
     * @param uri
     * @return true if the uri corresponds to a sensor
     *         false if it does not exist or if it is not a sensor
     */
    public boolean existAndIsSensor(String uri) {
        if (existObject(uri)) {
            return isSensor(uri);
            
        } else {
            return false;
        }
    }
    
    /**
     * check the given sensor's metadata
     * @param sensors
     * @return the result with the list of the errors founded (empty if no error founded)
     */
    public POSTResultsReturn check(List<SensorDTO> sensors) {
        POSTResultsReturn check = null;
        //list of the returned results
        List<Status> checkStatus = new ArrayList<>();
        boolean dataOk = true;
        
        //1. checl if user is an admin
        UserDaoPhisBrapi userDao = new UserDaoPhisBrapi();
        if (userDao.isAdmin(user)) {
            //2. check data
            for (SensorDTO sensor : sensors) {
                try {
                    //2.1 check type (subclass of SensingDevice)
                    UriDaoSesame uriDaoSesame = new UriDaoSesame();
                    if (!uriDaoSesame.isSubClassOf(sensor.getRdfType(), Vocabulary.CONCEPT_SENSING_DEVICE.toString())) {
                        dataOk = false;
                        checkStatus.add(new Status(StatusCodeMsg.DATA_ERROR, StatusCodeMsg.ERR, "Bad sensor type given. Must be sublass of SensingDevice concept"));
                    }

                    //2.2 check if person in charge exist
                    User u = new User(sensor.getPersonInCharge());
                    if (!userDao.existInDB(u)) {
                        dataOk = false;
                        checkStatus.add(new Status(StatusCodeMsg.UNKNOWN_URI, StatusCodeMsg.ERR, "Unknown person in charge email"));
                    }
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(SensorDAOSesame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else { //user is not an admin
            dataOk = false;
            checkStatus.add(new Status(StatusCodeMsg.ACCESS_DENIED, StatusCodeMsg.ERR, StatusCodeMsg.ADMINISTRATOR_ONLY));
        }
        
        check = new POSTResultsReturn(dataOk, null, dataOk);
        check.statusList = checkStatus;
        return check;
    }
    
    /**
     * generates an insert query for sensors.
     * e.g.
     * INSERT DATA {
     *  GRAPH <http://www.phenome-fppn.fr/diaphen/sensors> { 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  rdf:type  <http://www.phenome-fppn.fr/vocabulary/2017#Thermocouple> . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  rdfs:label  "par03_p"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#hasBrand>  "Homemade"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#inServiceDate>  "2017-06-15"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#personInCharge>  "morgane.vidal@inra.fr"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#serialNumber>  "A1E345F32"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#dateOfPurchase>  "2017-06-15"  . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/v18142>  <http://www.phenome-fppn.fr/vocabulary/2017#dateOfLastCalibration>  "2017-06-15"  . 
     *  }
     * }
     * @param sensor
     * @return the query
     */
    private SPARQLUpdateBuilder prepareInsertQuery(Sensor sensor) {
        SPARQLUpdateBuilder query = new SPARQLUpdateBuilder();
        
        query.appendGraphURI(Contexts.SENSORS.toString());
        query.appendTriplet(sensor.getUri(), Rdf.RELATION_TYPE.toString(), sensor.getRdfType(), null);
        query.appendTriplet(sensor.getUri(), Rdfs.RELATION_LABEL.toString(), "\"" + sensor.getLabel() + "\"", null);
        query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_HAS_BRAND.toString(), "\"" + sensor.getBrand() + "\"", null);
        query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_IN_SERVICE_DATE.toString(), "\"" + sensor.getInServiceDate() + "\"", null);
        query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_PERSON_IN_CHARGE.toString(), "\"" + sensor.getPersonInCharge() + "\"", null);
        
        if (sensor.getSerialNumber() != null) {
            query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_SERIAL_NUMBER.toString(), "\"" + sensor.getSerialNumber() + "\"", null);
        }
        
        if (sensor.getDateOfPurchase() != null) {
            query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_DATE_OF_PURCHASE.toString(), "\"" + sensor.getDateOfPurchase() + "\"", null);
        }
        
        if (sensor.getDateOfLastCalibration() != null) {
            query.appendTriplet(sensor.getUri(), Vocabulary.RELATION_DATE_OF_LAST_CALIBRATION.toString(), "\"" + sensor.getDateOfLastCalibration() + "\"", null);
        }
        
        LOGGER.debug(getTraceabilityLogs() + " query : " + query.toString());
        return query;
    }
    
    /**
     * insert the given sensors in the triplestore
     * @param sensorsDTO
     * @return the insertion result, with the errors list or the uri of the inserted
     *         sensors
     */
    public POSTResultsReturn insert(List<SensorDTO> sensorsDTO) {
        List<Status> insertStatus = new ArrayList<>();
        List<String> createdResourcesUri = new ArrayList<>();
        
        POSTResultsReturn results; 
        boolean resultState = false;
        boolean annotationInsert = true;
        
        UriGenerator uriGenerator = new UriGenerator();
        
        //SILEX:test
        //Triplestore connection has to be checked (this is kind of an hot fix)
        this.getConnection().begin();
        //\SILEX:test
        
        for (SensorDTO sensorDTO : sensorsDTO) {
            Sensor sensor = sensorDTO.createObjectFromDTO();
            sensor.setUri(uriGenerator.generateNewInstanceUri(sensorDTO.getRdfType(), null, null));
            
            SPARQLUpdateBuilder query = prepareInsertQuery(sensor);
            Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, query.toString());
            prepareUpdate.execute();
            
            createdResourcesUri.add(sensor.getUri());
        }
        
        if (annotationInsert) {
            resultState = true;
            getConnection().commit();
        } else {
            getConnection().rollback();
        }
        
        results = new POSTResultsReturn(resultState, annotationInsert, true);
        results.statusList = insertStatus;
        results.setCreatedResources(createdResourcesUri);
        if (resultState && !createdResourcesUri.isEmpty()) {
            results.createdResources = createdResourcesUri;
            results.statusList.add(new Status(StatusCodeMsg.RESOURCES_CREATED, StatusCodeMsg.INFO, createdResourcesUri.size() + " new resource(s) created"));
        }
        
        if (getConnection() != null) {
            getConnection().close();
        }
        
        return results;
    }
    
    /**
     * check and insert the given sensors in the triplestore
     * @param sensors
     * @return the insertion result. Message error if errors founded in data
     *         the list of the generated uri of the sensors if the insertion has been done
     */
    public POSTResultsReturn checkAndInsert(List<SensorDTO> sensors) {
        POSTResultsReturn checkResult = check(sensors);
        if (checkResult.getDataState()) {
            return insert(sensors);
        } else { //errors founded in data
            return checkResult;
        }
    }
    
    /**
     * prepare a delete query of the triplets corresponding to the given sensor
     * e.g.
     * DELETE WHERE { 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> rdf:type <http://www.phenome-fppn.fr/vocabulary/2017#Thermocouple> . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> rdfs:label "par03_p" . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#hasBrand> "Skye Instruments" . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#inServiceDate> "2017-06-15" . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#personInCharge> "morgane.vidal@inra.fr" . 
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#serialNumber> "A1E345F32" .
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#dateOfPurchase> "2017-06-15" .
     *      <http://www.phenome-fppn.fr/diaphen/2018/s18142> <http://www.phenome-fppn.fr/vocabulary/2017#dateOfLastCalibration> "2017-06-15"
     * }
     * @param sensor
     * @return 
     */
    private String prepareDeleteQuery(Sensor sensor) {
        String query;
        query = "DELETE WHERE { "
                + "<" + sensor.getUri() + "> <" + Rdf.RELATION_TYPE.toString() + "> <" + sensor.getRdfType() + "> . "
                + "<" + sensor.getUri() + "> <" + Rdfs.RELATION_LABEL.toString() + "> \"" + sensor.getLabel() + "\" . "
                + "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_HAS_BRAND.toString() + "> \"" + sensor.getBrand() + "\" . "
                + "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_IN_SERVICE_DATE.toString() + "> \"" + sensor.getInServiceDate() + "\" . "
                + "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_PERSON_IN_CHARGE.toString() + "> \"" + sensor.getPersonInCharge() + "\" . ";
        
        if (sensor.getSerialNumber() != null) {
            query += "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_SERIAL_NUMBER.toString() + "> \"" + sensor.getSerialNumber() + "\" . ";
        }
        if (sensor.getDateOfPurchase() != null) {
            query += "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_DATE_OF_PURCHASE.toString() + "> \"" + sensor.getDateOfPurchase() + "\" . ";
        }
        if (sensor.getDateOfLastCalibration() != null) {
            query += "<" + sensor.getUri() + "> <" + Vocabulary.RELATION_DATE_OF_LAST_CALIBRATION.toString() + "> \"" + sensor.getDateOfLastCalibration() + "\" . ";
        }
        
        query += " }";
        
        return query;
    }
    
    /**
     * update a list of sensors. The sensors data must have been checked before
     * @see SensorDAOSesame#check(java.util.List)
     * @param sensors 
     * @return the updated result
     */
    private POSTResultsReturn update(List<SensorDTO> sensors) {
        List<Status> updateStatus = new ArrayList<>();
        List<String> updatedResourcesUri = new ArrayList<>();
        POSTResultsReturn results;
        
        boolean annotationUpdate = true;
        boolean resultState = false;
        
        for (SensorDTO sensorDTO : sensors) {
            //1. delete already existing data
            //1.1 get informations that will be updated (to delete the right triplets)
            uri = sensorDTO.getUri();
            ArrayList<Sensor> sensorsCorresponding = allPaginate();
            if (sensorsCorresponding.size() > 0) {
                String deleteQuery = prepareDeleteQuery(sensorsCorresponding.get(0));
                
                //2. insert new data
                SPARQLUpdateBuilder insertQuery = prepareInsertQuery(sensorDTO.createObjectFromDTO());
                try {
                    this.getConnection().begin();
                    Update prepareDelete = this.getConnection().prepareUpdate(deleteQuery);
                    Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, insertQuery.toString());
                    LOGGER.debug(getTraceabilityLogs() + " query : " + prepareDelete.toString());
                    LOGGER.debug(getTraceabilityLogs() + " query : " + prepareUpdate.toString());
                    prepareDelete.execute();
                    prepareUpdate.execute();
                    updatedResourcesUri.add(sensorDTO.getUri());
                } catch (MalformedQueryException e) {
                    LOGGER.error(e.getMessage(), e);
                    annotationUpdate = false;
                    updateStatus.add(new Status(StatusCodeMsg.QUERY_ERROR, StatusCodeMsg.ERR, "Malformed update query: " + e.getMessage()));
                }
            } else {
                annotationUpdate = false;
                updateStatus.add(new Status(StatusCodeMsg.UNKNOWN_URI, StatusCodeMsg.ERR, "Unknown sensor " + uri));
            }
        }
        
        if (annotationUpdate) {
            resultState = true;
            try {
                this.getConnection().commit();
            } catch (RepositoryException ex) {
                LOGGER.error("Error during commit Triplestore statements: ", ex);
            }
        } else {
            try {
                this.getConnection().rollback();
            } catch (RepositoryException ex) {
                LOGGER.error("Error during rollback Triplestore statements : ", ex);
            }
        }
        
        results = new POSTResultsReturn(resultState, annotationUpdate, true);
        results.statusList = updateStatus;
        if (resultState && !updatedResourcesUri.isEmpty()) {
            results.createdResources = updatedResourcesUri;
            results.statusList.add(new Status(StatusCodeMsg.RESOURCES_UPDATED, StatusCodeMsg.INFO, updatedResourcesUri.size() + " resources updated"));
        }
        
        return results;
    }
    
    /**
     * check and update the given sensors in the triplestore
     * @see SensorDAOSesame#check(java.util.List)
     * @see SensorDAOSesame#update(java.util.List)
     * @param sensors
     * @return the update result. Message error if errors founded in data,
     *         the list of the updated sensors's uri if they has been updated correctly
     */
    public POSTResultsReturn checkAndUpdate(List<SensorDTO> sensors) {
        POSTResultsReturn checkResult = check(sensors);
        if (checkResult.getDataState()) {
            return update(sensors);
        } else { //errors founded in data
            return checkResult;
        }
    }
    
    /**
     * Generates the query to get the uri, label and rdf type of all the cameras
     * @example 
     * SELECT DISTINCT  ?uri ?label ?rdfType WHERE {
     *      ?uri  rdfs:subClassOf*  <http://www.phenome-fppn.fr/vocabulary/2017#Camera> . 
     *      ?uri rdf:type ?rdfType .
     *      ?uri  rdfs:label  ?label  .
     * }
     * @return the query
     */
    private SPARQLQueryBuilder prepareSearchCamerasQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        
        query.appendSelect("?" + URI + " ?" + RDF_TYPE + " ?" + LABEL );
        query.appendTriplet("?" + RDF_TYPE, "<" + Rdfs.RELATION_SUBCLASS_OF.toString() + ">*", Vocabulary.CONCEPT_CAMERA.toString(), null);
        query.appendTriplet("?" + URI, Rdf.RELATION_TYPE.toString(), "?" + RDF_TYPE, null);
        query.appendTriplet("?" + URI, Rdfs.RELATION_LABEL.toString(), "?" + LABEL, null);
        query.appendOrderBy("DESC(?" + LABEL + ")");
        
        query.appendLimit(this.getPageSize());
        query.appendOffset(this.getPage() * this.getPageSize());
        
        LOGGER.debug(query.toString());
        
        return query;
    }
    
    /**
     * Get the cameras (type, label, uri) of the triplestore.
     * @return The list of the cameras
     */
    public ArrayList<Sensor> getCameras() {
        SPARQLQueryBuilder query = prepareSearchCamerasQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Sensor> cameras = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Sensor camera = getSensorFromBindingSet(bindingSet);
                cameras.add(camera);
            }
        }
        return cameras;
    }
}
