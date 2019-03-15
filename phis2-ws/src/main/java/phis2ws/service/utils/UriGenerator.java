//******************************************************************************
//                            UriGenerator.java
// SILEX-PHIS
// Copyright © INRA 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.utils;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.UUID;
import org.apache.commons.codec.binary.Base32;
import org.apache.jena.sparql.AlreadyExists;
import phis2ws.service.PropertiesFileManager;
import phis2ws.service.dao.mongo.ImageMetadataDaoMongo;
import phis2ws.service.dao.phis.ExperimentDao;
import phis2ws.service.dao.phis.GroupDao;
import phis2ws.service.dao.phis.ProjectDao;
import phis2ws.service.dao.sesame.ScientificObjectDAOSesame;
import phis2ws.service.dao.sesame.AnnotationDAOSesame;
import phis2ws.service.dao.sesame.EventDAOSesame;
import phis2ws.service.dao.sesame.MethodDaoSesame;
import phis2ws.service.dao.sesame.RadiometricTargetDAOSesame;
import phis2ws.service.dao.sesame.SensorDAOSesame;
import phis2ws.service.dao.sesame.TimeDAOSesame;
import phis2ws.service.dao.sesame.UriDaoSesame;
import phis2ws.service.dao.sesame.TraitDaoSesame;
import phis2ws.service.dao.sesame.UnitDaoSesame;
import phis2ws.service.dao.sesame.VariableDaoSesame;
import phis2ws.service.dao.sesame.VectorDAOSesame;
import phis2ws.service.ontologies.Contexts;
import phis2ws.service.ontologies.Foaf;
import phis2ws.service.ontologies.Oeev;
import phis2ws.service.ontologies.Oeso;
import phis2ws.service.ontologies.Time;
import phis2ws.service.view.model.phis.Group;
import phis2ws.service.view.model.phis.Project;

/**
 * Generate different kinds of URIs (vector, sensor, ...)
 * @update [Andreas Garcia] 14 Feb. 2019: use the last inserted experiment
 * number instead of total number of experiment to calculate the number of a
 * new experiment
 * @author Morgane Vidal <morgane.vidal@inra.fr>, Arnaud Charleroy <arnaud.charleory@inra.fr>
 * SILEX:todo: 
 *       - Element: User agent uri
 *         Purpose: For now, generated user agent uris are not unic. 
 *         Numbers must be add at the end of user agent uri
 *         if two user agents have the same family name and first name.
 *         .e.g:
 *              - First user: Jean Dupont-Marie http://www.phenome-fppn.fr/diaphen/id/agent/jean_dupont-marie
 *              - Second user: Jean Dupont-Marie http://www.phenome-fppn.fr/diaphen/id/agent/jean_dupont-marie01
 * \SILEX:todo
 */
public class UriGenerator {    
    private static final String URI_CODE_AGRONOMICAL_OBJECT = "o";
    private static final String URI_CODE_IMAGE = "i";
    private static final String URI_CODE_METHOD = "m";
    private static final String URI_CODE_SENSOR = "s";
    private static final String URI_CODE_RADIOMETRIC_TARGET = "rt";
    private static final String URI_CODE_TRAIT = "t";
    private static final String URI_CODE_UNIT = "u";
    private static final String URI_CODE_VARIABLE = "v";
    private static final String URI_CODE_VECTOR = "v";

    private static final String PLATFORM_CODE =  PropertiesFileManager.getConfigFileProperty("sesame_rdf_config", "infrastructureCode") ;
    private static final String PLATFORM_URI = Contexts.PLATFORM.toString();
    private static final String PLATFORM_URI_ID = PLATFORM_URI + "id/";
    private static final String PLATFORM_URI_ID_AGENT = PLATFORM_URI_ID + "agent/";
    private static final String PLATFORM_URI_ID_ANNOTATION = PLATFORM_URI_ID + "annotation/";
    private static final String PLATFORM_URI_ID_EVENT = PLATFORM_URI_ID + "event/";
    private static final String PLATFORM_URI_ID_INSTANT = PLATFORM_URI_ID + "instant/";
    private static final String PLATFORM_URI_ID_METHOD = PLATFORM_URI_ID + "methods/";
    private static final String PLATFORM_URI_ID_RADIOMETRIC_TARGET = PLATFORM_URI_ID + "radiometricTargets/";
    private static final String PLATFORM_URI_ID_TRAITS = PLATFORM_URI_ID + "traits/";
    private static final String PLATFORM_URI_ID_UNITS = PLATFORM_URI_ID + "units/";
    private static final String PLATFORM_URI_ID_VARIABLES = PLATFORM_URI_ID + "variables/";
    private static final String PLATFORM_URI_ID_VARIETY = PLATFORM_URI + "v/";
    private static final String PLATFORM_URI_ID_PROVENANCE = PLATFORM_URI_ID + "provenance/";
    
    private static final String EXPERIMENT_URI_SEPARATOR = "-";


    /**
     * Generate a new vector URI. a vector URI has the following pattern:
     * <prefix>:<year>/<unic_code>
     * <unic_code> = 1 letter type + 2 numbers year + auto incremented number
     * with 3 digits (per year)
     * @example http://www.phenome-fppn.fr/diaphen/2017/v1702
     * @param year the insertion year of the vector.
     * @return the new vector URI
     */
    private String generateVectorUri(String year) {
        //1. get the actual number of vectors in the triplestor for the year
        VectorDAOSesame vectorDAO = new VectorDAOSesame();
        int lastVectorIdFromYear = vectorDAO.getLastIdFromYear(year);

        //2. generate vectors URI
        String numberOfVectors = Integer.toString(lastVectorIdFromYear + 1);

        String newVectorNumber;

        if (numberOfVectors.length() == 1) {
            newVectorNumber = "0" + numberOfVectors;
        } else {
            newVectorNumber = numberOfVectors;
        }

        return PLATFORM_URI + year + "/" + URI_CODE_VECTOR + year.substring(2, 4) + newVectorNumber;
    }

    /**
     * Generate a new sensor URI. A sensor URI has the following pattern:
     * <prefix>:<year>/<unic_code>
     * <unic_code> = 1 letter type + 2 numbers year + auto incremented number
     * with 2 digits (per year) the year corresponds to the year of insertion in
     * the triplestore
     * @example http://www.phenome-fppn.fr/diaphen/2017/s17002
     * @param year the insertion year of the sensor.
     * @return the new sensor URI
     */
    private String generateSensorUri(String year) {
        //1. get the current number of sensors in the triplestor for the year
        SensorDAOSesame sensorDAO = new SensorDAOSesame();
        int lastSensorIdFromYear = sensorDAO.getLastIdFromYear(year);

        //2. generate sensor URI
        int sensorNumber = lastSensorIdFromYear + 1;
        String numberOfSensors = Integer.toString(sensorNumber);
        String newSensorNumber;
        switch (numberOfSensors.length()) {
            case 1:
                newSensorNumber = "00" + numberOfSensors;
                break;
            case 2:
                newSensorNumber = "0" + numberOfSensors;
                break;
            default:
                newSensorNumber = numberOfSensors;
                break;
        }
        return PLATFORM_URI + year + "/" + URI_CODE_SENSOR + year.substring(2, 4) + newSensorNumber;
    }

    /**
     * Generate a new agronomical object URI. A sensor URI has the following
     * form:
     * <prefix>:<year>/<unic_code>
     * <unic_code> = 1 letter type + 2 numbers year + auto incremented number
     * with 6 digits (per year) the year corresponds to the year of insertion in
     * the triplestore 
     * @example http://www.phenome-fppn.fr/diaphen/2017/o17000001
     * @param year the insertion year of the agronomical object.
     * @return the new agronomical object URI
     */
    private String generateAgronomicalObjectUri(String year) {
        //1. get the highest number for the year 
        //(i.e. the last inserted agronomical object for the year)
        ScientificObjectDAOSesame agronomicalObjectDAO = new ScientificObjectDAOSesame();
        int lastAgronomicalObjectIdFromYear = agronomicalObjectDAO.getLastScientificObjectIdFromYear(year);

        //2. generates agronomical object URI
        int agronomicalObjectNumber = lastAgronomicalObjectIdFromYear + 1;
        String agronomicalObjectId = Integer.toString(agronomicalObjectNumber);

        while (agronomicalObjectId.length() < 6) {
            agronomicalObjectId = "0" + agronomicalObjectId;
        }

        return PLATFORM_URI + year + "/" + URI_CODE_AGRONOMICAL_OBJECT + year.substring(2, 4) + agronomicalObjectId;
    }

    /**
     * Generate a new variable URI. A variable URI follows the pattern:
     * <prefix>:id/variables/<unic_code>
     * <unic_code> = 1 letter type + auto incremented number with 3 digits e.g.
     * @example http://www.phenome-fppn.fr/diaphen/id/variables/v001
     * @return the new variable URI
     */
    private String generateVariableUri() {
        //1. get the higher variable id (i.e. the last inserted variable)
        VariableDaoSesame variableDAO = new VariableDaoSesame();
        int lastVariableId = variableDAO.getLastId();

        //2. generate variable URI
        int newVariableId = lastVariableId + 1;
        String variableId = Integer.toString(newVariableId);

        while (variableId.length() < 3) {
            variableId = "0" + variableId;
        }

        return PLATFORM_URI_ID_VARIABLES + URI_CODE_VARIABLE + variableId;
    }

    /**
     * Generate a new trait URI. A trait URI follows the pattern:
     * <prefix>:id/traits/<unic_code>
     * <unic_code> = 1 letter type + auto incremented number with 3 digits e.g.
     * @example http://www.phenome-fppn.fr/diaphen/id/traits/t001
     * @return the new trait URI
     */
    private String generateTraitUri() {
        //1. get the highest trait id (i.e. the last inserted trait)
        TraitDaoSesame traitDAO = new TraitDaoSesame();
        int lastTraitId = traitDAO.getLastId();

        //2. generate trait URI
        int newTraitId = lastTraitId + 1;
        String traitId = Integer.toString(newTraitId);

        while (traitId.length() < 3) {
            traitId = "0" + traitId;
        }

        return PLATFORM_URI_ID_TRAITS + URI_CODE_TRAIT + traitId;
    }

    /**
     * Generate a new method URI. A method URI follows the pattern:
     * <prefix>:id/methods/<unic_code>
     * <unic_code> = 1 letter type + auto incremented number with 3 digits e.g.
     * @example http://www.phenome-fppn.fr/diaphen/id/methods/m001
     * @return the new method URI
     */
    private String generateMethodUri() {
        //1. get the highest method id (i.e. the last inserted method)
        MethodDaoSesame methodDAO = new MethodDaoSesame();
        int lastMethodId = methodDAO.getLastId();

        //2. generate method URI
        int newMethodId = lastMethodId + 1;
        String methodId = Integer.toString(newMethodId);

        while (methodId.length() < 3) {
            methodId = "0" + methodId;
        }

        return PLATFORM_URI_ID_METHOD + URI_CODE_METHOD + methodId;
    }

    /**
     * Generate a new unit URI. A unit URI follows the pattern:
     * <prefix>:id/units/<unic_code>
     * <unic_code> = 1 letter type + auto incremented number with 3 digits
     * @example http://www.phenome-fppn.fr/diaphen/id/units/m001
     * @return the new unit URI
     */
    private String generateUnitUri() {
        //1. get the highest unit id (i.e. the last inserted unit)
        UnitDaoSesame unitDAO = new UnitDaoSesame();
        int lastUnitId = unitDAO.getLastId();

        //2. generates unit URI
        int newUnitId = lastUnitId + 1;
        String unitId = Integer.toString(newUnitId);

        while (unitId.length() < 3) {
            unitId = "0" + unitId;
        }

        return PLATFORM_URI_ID_UNITS + URI_CODE_UNIT + unitId;
    }
    
    /**
     * Generate a new radiometric target URI. A radiometric target URI follows the pattern: 
     * <prefix>:id/radiometricTargets/<unic_code>
     * <unic_code> = 2 letters type (rt) + auto incremented number with 3 digits
     * @example http://www.phenome-fppn.fr/diaphen/id/radiometricTargets/rt001
     * @return The new radiometric target URI
     */
    private String generateRadiometricTargetUri() {
        //1. Get the highest radiometric target id (i.e. the last inserted
        //radiometric target)
        RadiometricTargetDAOSesame radiometricTargetDAO = new RadiometricTargetDAOSesame();
        int lastID = radiometricTargetDAO.getLastId();
        
        //2. Generate radiometric target URI
        int newRadiometricTargetID = lastID + 1;
        String radiometricTargetID = Integer.toString(newRadiometricTargetID);
        
        while (radiometricTargetID.length() < 3) {
            radiometricTargetID = "0" + radiometricTargetID;
        }
        
        return PLATFORM_URI_ID_RADIOMETRIC_TARGET + URI_CODE_RADIOMETRIC_TARGET + radiometricTargetID;
    }

    /**
     * Generate a new variety URI. A variety URI follows the pattern:
     * <prefix>:v/<varietynameinlowercase>
     * @example http://www.phenome-fppn.fr/diaphen/v/dkc4814
     * @param variety the variety name
     * @return the new variety uri
     */
    private String generateVarietyUri(String variety) {
        return PLATFORM_URI_ID_VARIETY + variety.toLowerCase();
    }

    /**
     * Generate a new agent uri. A agent URI follows the pattern:
     * <prefix>:id/agent/<unic_code>
     * <unic_code> = firstnames concat with lastnames in lowercase
     * @example http://www.phenome-fppn.fr/diaphen/id/agent/arnaud_charleroy
     * @author Arnaud Charleroy
     * @param agentSuffixe the agent suffix e.g. arnaud_charleroy
     * @return the new agent URI
     */
    private String generateAgentUri(String agentSuffixe) {
        // create URI
        return PLATFORM_URI_ID_AGENT + agentSuffixe;
    }

    /**
     * Generate a new annotation URI. A unit annotation follows the pattern:
     * <prefix>:id/annotation/<unic_code>
     * <unic_code> = 1 letter type + java.util.UUID.randomUUID(); 
     * @example http://www.phenome-fppn.fr/diaphen/id/annotation/e073961b-e766-4493-b98f-74a8b2846893
     * @return the new annotation URI
     */
    private String generateAnnotationUri() {
        //1. check if URI already exists
        AnnotationDAOSesame annotationDao = new AnnotationDAOSesame();
        String newAnnotationUri = PLATFORM_URI_ID_ANNOTATION + UUID.randomUUID();
        while (annotationDao.existUri(newAnnotationUri)) {
            newAnnotationUri = PLATFORM_URI_ID_ANNOTATION + UUID.randomUUID();
        }

        return newAnnotationUri;
    }

    /**
     * Generate a new event URI. an event URI follows the pattern:
     * <prefix>:id/event/<unic_code>
     * <unic_code> = java.util.UUID.randomUUID();
     * @example http://www.phenome-fppn.fr/diaphen/id/event/e073961b-e766-4493-b98f-74a8b2846893
     * @return the new event URI
     */
    private String generateEventUri() {
        // To check if URI already exists
        EventDAOSesame eventDao = new EventDAOSesame(null);
        String newEventUri = PLATFORM_URI_ID_EVENT + UUID.randomUUID();
        while (eventDao.existUri(newEventUri)) {
            newEventUri = PLATFORM_URI_ID_EVENT + UUID.randomUUID();
        }

        return newEventUri;
    }

    /**
     * Generate a new Instant URI. The URI follows the pattern:
     * <prefix>:id/instant/<unic_code>
     * <unic_code> = java.util.UUID.randomUUID();
     * @example http://www.phenome-fppn.fr/diaphen/id/instant/e073961b-e766-4493-b98f-74a8b2846893
     * @return the new URI
     */
    private String generateInstantUri() {
        // To check if the URI already exists
        TimeDAOSesame timeDao = new TimeDAOSesame(null);
        String newInstantUri = PLATFORM_URI_ID_INSTANT + UUID.randomUUID();
        while (timeDao.existUri(newInstantUri)) {
            newInstantUri = PLATFORM_URI_ID_INSTANT + UUID.randomUUID();
        }

        return newInstantUri;
    }

    /**
     * generates a new image uri. an image uri follows the pattern :
     * <prefix>:yyyy/<unic_code>
     * <unic_code> = 1 letter type (i) + 2 digits year + auto incremet with 10
     * digit
     * @example http://www.phenome-fppn.fr/diaphen/2018/i180000000001
     * @param year the year of insertion of the image
     * @param lastGeneratedUri if a few uri has been generated but not inserted,
     * corresponds to the last generated uri
     * @return the new uri
     */
    private String generateImageUri(String year, String lastGeneratedUri) {
        if (lastGeneratedUri == null) {
            ImageMetadataDaoMongo imageDaoMongo = new ImageMetadataDaoMongo();
            long imagesNumber = imageDaoMongo.getNbImagesYear();
            imagesNumber++;

            //calculate the number of 0 to add before the number of the image
            String nbImagesByYear = Long.toString(imagesNumber);
            while (nbImagesByYear.length() < 10) {
                nbImagesByYear = "0" + nbImagesByYear;
            }

            String uniqueId = URI_CODE_IMAGE + year.substring(2, 4) + nbImagesByYear;
            return PLATFORM_URI + year + "/" + uniqueId;
        } else {
            int uniqueId = Integer.parseInt(lastGeneratedUri.split("/" + URI_CODE_IMAGE + year.substring(2, 4))[1]);
            uniqueId++;

            String nbImagesByYear = Long.toString(uniqueId);
            while (nbImagesByYear.length() < 10) {
                nbImagesByYear = "0" + nbImagesByYear;
            }

            return PLATFORM_URI + year + nbImagesByYear;
        }
    }
    
    /**
     * Generates a new project uri. A project uri follows the patter:
     * <prefix>:<projectAcronyme>
     * @example http://www.opensilex.org/demo/PA
     * @param projectAcronyme the project acronym
     * @return the new URI
     */
    private String generateProjectUri(String projectAcronyme) throws Exception {
        //1. generates URI
        String projectUri = PLATFORM_URI + projectAcronyme;
        //2. check if URI exists
        ProjectDao projectDAO = new ProjectDao();
        Project project = new Project(projectUri);
        if (projectDAO.existInDB(project)) {
            throw new AlreadyExists("The project uri " + projectUri + " already exist in the triplestore.");
        }
        
        return projectUri;
    }
    
    /**
     * Generate a new experiment URI. An experiment URI follows this pattern:
     * <prefix>:<unic_code>
     * <unic_code> = infrastructure code + 4 digits year + auto increment digit (per year)
     * @example http://www.opensilex.org/demo/DMO2019-1
     * @param campaign the year of the campaign of the experiment
     * @return the new URI
     */
    private String generateExperimentUri(String campaign) {
        //1. Get the campaign last experiment URI
        String campaignLastExperimentUri = (new ExperimentDao()).getCampaignLastExperimentUri(campaign);
        //2. Generate the URI of the experiment
        int newExperimentNumber;
        if (campaignLastExperimentUri == null) {
            newExperimentNumber = 0;
        }
        else {
            String[] uriSplitted = campaignLastExperimentUri.split(EXPERIMENT_URI_SEPARATOR);
            int campaignLastExperimentNumber = Integer.parseInt(uriSplitted[uriSplitted.length - 1]);
            newExperimentNumber = campaignLastExperimentNumber + 1;
        }
        return PLATFORM_URI + PLATFORM_CODE + campaign + EXPERIMENT_URI_SEPARATOR + newExperimentNumber;
    }
    
    /**
     * Generate a new group URI. A group URI follows the pattern:
     * <prefix>:<groupName>
     * @example http://www.opensilex.org/demo/INRA-MISTEA.GAMMA
     * @param name the group name
     * @return the new generated URI
     * @throws Exception 
     */
    private String generateGroupUri(String name) throws Exception {
        //1. Generate URI
        String groupUri = PLATFORM_URI + name;
        //2. Check if the generated URI already exists
        GroupDao groupDao = new GroupDao();
        Group group = new Group(groupUri);
        
        if (groupDao.existInDB(group)) {
            throw new AlreadyExists("The group uri " + groupUri + " already exist in the triplestore.");
        }
        
        return groupUri;
    }
    
    /**
     * Generates a new provenance uri. A provenance uri follows the pattern :
     * <prefix>:id/provenance/<timestamp>
     * @example http://www.opensilex.org/demo/id/provenance/019275849
     * @return the new generated uri
     * @throws Exception 
     */
    private String generateProvenanceUri() {
        //Generates uri
        Instant instant = Instant.now();
        long timeStampMillis = instant.toEpochMilli();
        return PLATFORM_URI_ID_PROVENANCE + Long.toString(timeStampMillis);
    }


    /**
     * Generates a new data uri.
     * @example http://www.opensilex.org/id/data/1e9eb2fbacc7222d3868ae96149a8a16b32b2a1870c67d753376381ebcbb5937e78da502ee3f42d3828eaa8cab237f93
     * @param additionalInformation the key of the data
     * @return the new generated uri
     * @throws Exception 
     */
    private String generateDataUri(String additionalInformation) throws Exception {
        // Define data URI with key hash  and random id to prevent collision
        String uri = Contexts.PLATFORM.toString() + "id/data/" + getUniqueHash(additionalInformation);
        
        return uri;
    }
    
    /**
     * Generates a new data file uri.
     * @example http://www.opensilex.org/id/dataFile/1e9eb2fbacc7222d3868ae96149a8a16b32b2a1870c67d753376381ebcbb5937e78da502ee3f42d3828eaa8cab237f93
     * @param additionalInformation the key of the data file
     * @return the new generated uri
     * @throws Exception 
     */
    private String generateDataFileUri(String collection, String key) throws Exception {
        // Define data URI with key hash  and random id to prevent collision
        String uri = Contexts.PLATFORM.toString() + "id/dataFile/" + collection + "/" + getUniqueHash(key);
        
        return uri;
    }
    
    private String getUniqueHash(String key) throws NoSuchAlgorithmException {
        // Generate SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        
        // Convert hash to base32 string in lower case string and remove = padding sign
        Base32 base32 = new Base32();
        String encodedString = base32.encodeAsString(encodedhash).replaceAll("=", "").toLowerCase();
        
        // Generate UUID without '-' sign
        String randomId = UUID.randomUUID().toString().replaceAll("-", "");
        
        return encodedString + randomId;
    }

    /**
     * Generate the URI of a new instance of instanceType
     * @param instanceType the RDF type of the instance (a concept URI)
     * @param year year of the creation of the element. If it is null, it will
     * be the current year
     * @param additionalInformation some additional information used for some
     * URI generators. (e.g. the variety name, or the last generated URI for the
     * images)
     * @return the generated URI
     * @throws java.lang.Exception
     */
    public String generateNewInstanceUri(String instanceType, String year, String additionalInformation) throws Exception {
        if (year == null) {
            year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        }

        UriDaoSesame uriDaoSesame = new UriDaoSesame();

        if (uriDaoSesame.isSubClassOf(instanceType, Oeso.CONCEPT_VECTOR.toString())) {
            return generateVectorUri(year);
        } else if (uriDaoSesame.isSubClassOf(instanceType, Oeso.CONCEPT_SENSING_DEVICE.toString())) {
            return generateSensorUri(year);
        } else if (Oeso.CONCEPT_VARIABLE.toString().equals(instanceType)) {
            return generateVariableUri();
        } else if (Oeso.CONCEPT_TRAIT.toString().equals(instanceType)) {
            return generateTraitUri();
        } else if (Oeso.CONCEPT_METHOD.toString().equals(instanceType)) {
            return generateMethodUri();
        } else if (Oeso.CONCEPT_UNIT.toString().equals(instanceType)) {
            return generateUnitUri();
        } else if (uriDaoSesame.isSubClassOf(instanceType, Oeso.CONCEPT_SCIENTIFIC_OBJECT.toString())) {
            return generateAgronomicalObjectUri(year);
        } else if (Oeso.CONCEPT_VARIETY.toString().equals(instanceType)) {
            return generateVarietyUri(additionalInformation);
        } else if (uriDaoSesame.isSubClassOf(instanceType, Oeso.CONCEPT_IMAGE.toString())) {
            return generateImageUri(year, additionalInformation);
        } else if (instanceType.equals(Foaf.CONCEPT_AGENT.toString()) || uriDaoSesame.isSubClassOf(instanceType, Foaf.CONCEPT_AGENT.toString())) {
            return generateAgentUri(additionalInformation);
        } else if (instanceType.equals(Oeso.CONCEPT_ANNOTATION.toString())) {
            return generateAnnotationUri();
        } else if (instanceType.equals(Oeso.CONCEPT_RADIOMETRIC_TARGET.toString())) {
            return generateRadiometricTargetUri();
        } else if (instanceType.equals(Oeso.CONCEPT_PROJECT.toString())) {
            return generateProjectUri(additionalInformation);
        } else if (instanceType.equals(Oeso.CONCEPT_EXPERIMENT.toString())) {
            return generateExperimentUri(year);
        } else if (instanceType.equals(Foaf.CONCEPT_GROUP.toString())) {
            return generateGroupUri(additionalInformation);
        } else if (instanceType.equals(Oeso.CONCEPT_PROVENANCE.toString())) {
            return generateProvenanceUri();
        } else if (instanceType.equals(Oeso.CONCEPT_DATA.toString())) {
            return generateDataUri(additionalInformation);
        } else if (uriDaoSesame.isSubClassOf(instanceType, Oeev.Event.getURI())) {
            return generateEventUri();
        } else if (instanceType.equals(Time.Instant.toString())) {
            return generateInstantUri();
        } else if (instanceType.equals(Oeso.CONCEPT_DATA_FILE.toString())) {
            return generateDataFileUri(year, additionalInformation);
        }

        return null;
    }
}
