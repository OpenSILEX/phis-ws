//******************************************************************************
//                              ProvenanceDAO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 5 March 2019
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.PropertiesFileManager;
import opensilex.service.dao.manager.MongoDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.ontology.Oeso;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.utils.UriGenerator;
import opensilex.service.view.brapi.Status;
import opensilex.service.view.model.provenance.Provenance;

/**
 * Provenance DAO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ProvenanceDAO extends MongoDAO<Provenance> {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ProvenanceDAO.class);
    
    //Mongodb collection of the provenance
    private final String provenanceCollectionName = PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "provenance");
        
    //MongoFields labels, used to query (CRUD) the provenance mongo data
    private final static String DB_FIELD_URI = "uri";
    private final static String DB_FIELD_LABEL = "label";
    private final static String DB_FIELD_COMMENT = "comment";
    private final static String DB_FIELD_METADATA = "metadata";

    @Override
    protected BasicDBObject prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Generates the query to search provenances.
     * @param searchProvenance
     * @param jsonValueFilter
     * @example
     * { 
     *      "metadata.SensingDevice" : "http://www.opensilex.org/demo/s001", 
     *      "metadata.Vector" : "http://www.opensilex.org/demo/v001", 
     *      "uri" : "http://www.opensilex.org/opensilex/id/provenance/1551805521606", 
     *      "label" : { "$regex" : "PROV2019-LEAF", "$options" : "" }, 
     *      "comment" : { "$regex" : "plant", "$options" : "" } 
     * }
     * @return the query
     */
    protected BasicDBObject searchQuery(Provenance searchProvenance, String jsonValueFilter) {
        BasicDBObject query = new BasicDBObject();
        
        if (jsonValueFilter != null) {
            query.putAll((BSONObject) BasicDBObject.parse(jsonValueFilter));
        }
        
        if (searchProvenance.getUri() != null) {
            query.append(DB_FIELD_URI, searchProvenance.getUri());
        }
        
        if (searchProvenance.getLabel() != null) {
            query.append(DB_FIELD_LABEL, Pattern.compile(searchProvenance.getLabel(), Pattern.CASE_INSENSITIVE));
        }
        
        if (searchProvenance.getComment() != null) {
            query.append(DB_FIELD_COMMENT, Pattern.compile(searchProvenance.getComment(), Pattern.CASE_INSENSITIVE));
        }
        LOGGER.debug(query.toJson());
        
        return query;
    }
    
    /**
     * Counts the number of results for the query.
     * @param searchProvenance
     * @param jsonValueFilter
     * @return the number of provenances corresponding to the search parameters.
     */
    public int count(Provenance searchProvenance, String jsonValueFilter) {
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);

        // Get the filter query
        BasicDBObject query = searchQuery(searchProvenance, jsonValueFilter);
        
        // Return the document count
        return (int)provenanceCollection.countDocuments(query);
    }
    
    /**
     * Generates the document to insert provenance.
     * @example
     * { 
     *      "uri" : "http://www.opensilex.org/opensilex/id/provenance/1551877498746",
     *      "label" : "PROV2019-LEAF",
     *      "comment" : "In this provenance we have count the number of leaf per plant",
     *      "metadata" : { 
     *          "SensingDevice" : "http://www.opensilex.org/demo/s001",
     *          "Vector" : "http://www.opensilex.org/demo/v001" 
     *      }
     * }
     * @param provenance
     * @return the document to insert
     */
    private Document prepareInsertProvenanceDocument(Provenance provenance) {
        Document provenanceDocument = new Document();
        
        provenanceDocument.append(DB_FIELD_URI, provenance.getUri());
        provenanceDocument.append(DB_FIELD_LABEL, provenance.getLabel());
        provenanceDocument.append(DB_FIELD_COMMENT, provenance.getComment());
        provenanceDocument.append(DB_FIELD_METADATA, provenance.getMetadata());
        
        LOGGER.debug(provenanceDocument.toJson());
        
        return provenanceDocument;
    }
    
    /**
     * Inserts the given provenances in the MongoDB database.
     * @param provenances
     * @return the insertion result
     */
    private POSTResultsReturn insert(List<Provenance> provenances) throws Exception {
        // Initialize transaction
        MongoClient client = MongoDAO.getMongoClient();
        ClientSession session = client.startSession();
        session.startTransaction();
        
        POSTResultsReturn result;
        List<Status> status = new ArrayList<>();
        List<String> createdResources = new ArrayList<>();
        
        //1. Prepare all the documents to insert
        List<Document> provenancesDocuments = new ArrayList<>();
        for (Provenance provenance : provenances) {
            //1. Generates the provenance uri
            provenance.setUri(UriGenerator.generateNewInstanceUri(Oeso.CONCEPT_PROVENANCE.toString(), null, null));
            
            //2. Generates the document insert
            provenancesDocuments.add(prepareInsertProvenanceDocument(provenance));
        }
        
        boolean hasError = false;
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);
        
        //2. Create index on the provenance uri
        Bson indexFields = Indexes.ascending(
            DB_FIELD_URI
        );
        IndexOptions indexOptions = new IndexOptions().unique(true);
        provenanceCollection.createIndex(indexFields, indexOptions);

        //3. Insert all the provenances
        try {
            provenanceCollection.insertMany(session, provenancesDocuments);
            status.add(new Status(
                StatusCodeMsg.RESOURCES_CREATED, 
                StatusCodeMsg.INFO, 
                StatusCodeMsg.DATA_INSERTED
            ));
            
            for (Provenance provenance : provenances) {
                createdResources.add(provenance.getUri());
            }

        } catch (MongoException ex) {
            // Define that an error occurs
            hasError = true;
            // Add the original exception message for debugging
            status.add(new Status(
                StatusCodeMsg.UNEXPECTED_ERROR, 
                StatusCodeMsg.ERR, 
                StatusCodeMsg.DATA_REJECTED + " - " + ex.getMessage()
            ));
        }
        
        //4. Prepare result to return
        result = new POSTResultsReturn(hasError);
        result.statusList = status;
        
        if (!hasError) {
            // If no errors commit transaction
            session.commitTransaction();
            result.setHttpStatus(Response.Status.CREATED);
            result.createdResources = createdResources;
        } else {
            // If errors abort transaction
            session.abortTransaction();
            result.setHttpStatus(Response.Status.BAD_REQUEST);
        }
        
        // Close transaction session
        session.close();
        return result;
    }
    
    /**
     * Inserts the given provenances. 
     * No check is needed, the Java Beans validation is enough. 
     * @param provenances
     * @return the insertion result.
     */
    public POSTResultsReturn checkAndInsert(List<Provenance> provenances) {
        try {
            return insert(provenances);
        } catch (Exception ex) {
            return new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, ex.toString());
        }
    }
    
    /**
     * Checks if the given provenance URI exist in the provenance collection.
     * @param uri
     * @example 
     * {"uri": "http://www.opensilex.org/demo/id/provenance/0193759540"}
     * @return true if the provenance exist, 
     *         false if it does not exist.
     */
    public boolean existProvenanceUri(String uri) {
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);
        
        BasicDBObject query = prepareGetProvenanceByUri(uri);
        int numberOfProvenancesCorresponding = (int)provenanceCollection.countDocuments(query);
        
        return numberOfProvenancesCorresponding > 0;
    }
    
    /**
     * Checks the given provenance.
     * @param provenances
     * @return the check result with the founded errors
     */
    private POSTResultsReturn check(List<Provenance> provenances) {
        POSTResultsReturn checkResult;
        List<Status> checkStatus = new ArrayList<>();
        
        boolean dataOk = true;
        for (Provenance provenance : provenances) {
            //Check if the provenance uri exist.
            if (!existProvenanceUri(provenance.getUri())) {
                dataOk = false;
                checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, 
                    "The given provenance uri (" + provenance.getUri()+ ") does not exist"));
            }
        }
        checkResult = new POSTResultsReturn(dataOk, null, dataOk);
        checkResult.statusList = checkStatus;
        return checkResult;
    }
    
    /**
     * Generates the document to update provenance.
     * @example
     * { 
     *      "uri" : "http://www.opensilex.org/opensilex/id/provenance/1551877498746",
     *      "label" : "PROV2019-LEAF",
     *      "comment" : "In this provenance we have count the number of leaf per plant",
     *      "metadata" : { 
     *          "SensingDevice" : "http://www.opensilex.org/demo/s001",
     *          "Vector" : "http://www.opensilex.org/demo/v001" 
     *      }
     * }
     * @param provenance
     * @return the document to update
     */
    private Document prepareUpdateProvenanceDocument(Provenance provenance) {
        Document provenanceDocument = new Document();
        
        provenanceDocument.put(DB_FIELD_URI, provenance.getUri());
        provenanceDocument.put(DB_FIELD_LABEL, provenance.getLabel());
        provenanceDocument.put(DB_FIELD_COMMENT, provenance.getComment());
        provenanceDocument.put(DB_FIELD_METADATA, provenance.getMetadata());
        
        LOGGER.debug(provenanceDocument.toJson());
        
        return provenanceDocument;
    }
    
    /**
     * Generates the query to get a provenance by an URI.
     * @param uri
     * @example { "uri" : "http://www.opensilex.org/opensilex/id/provenance/1551805521606" }
     * @return the query
     */
    private BasicDBObject prepareGetProvenanceByUri(String uri) {
        BasicDBObject query = new BasicDBObject();
        query.append(DB_FIELD_URI, uri);
        
        LOGGER.debug(query.toJson());
        
        return query;
    }
    
    /**
     * Updates the given provenances.
     * /!\ Prerequisite : data must have been checked before calling this method.
     * @see ProvenanceDAO#check(java.util.List)
     * @param provenances the list of the provenances to update
     * @return the update result with the list of all the updated provenances.
     */
    private POSTResultsReturn updateAndReturnPoSTResultsReturn(List<Provenance> provenances) throws Exception {
        // Initialize transaction
        MongoClient client = MongoDAO.getMongoClient();
        ClientSession session = client.startSession();
        session.startTransaction();
        
        POSTResultsReturn result;
        List<Status> status = new ArrayList<>();
        List<String> updatedResources = new ArrayList<>();
        boolean error = false;
        
        collection = database.getCollection(provenanceCollectionName);
        
        //1. Update documents
        for (Provenance provenance : provenances) {
            try {
                collection.replaceOne(prepareGetProvenanceByUri(provenance.getUri()), prepareUpdateProvenanceDocument(provenance));
                updatedResources.add(provenance.getUri());
            } catch (MongoException ex) {
                // Define that an error occurs
                error = true;
                // Add the original exception message for debugging
                status.add(new Status(
                    StatusCodeMsg.UNEXPECTED_ERROR, 
                    StatusCodeMsg.ERR, 
                    StatusCodeMsg.DATA_REJECTED + " - " + ex.getMessage()
                ));
            }
        }
        
        //2. Prepare result to return
        result = new POSTResultsReturn(error);
        result.statusList = status;
        
        if (!error) {
            // If no errors commit transaction
            session.commitTransaction();
            result.setHttpStatus(Response.Status.CREATED);
            result.createdResources = updatedResources;
        } else {
            // If errors abort transaction
            session.abortTransaction();
            result.setHttpStatus(Response.Status.BAD_REQUEST);
        }
        
        // Close transaction session
        session.close();
        return result;
    }
    
    /**
     * Check the given provenances data and update them.
     * @param provenances
     * @return the update result
     */
    public POSTResultsReturn checkAndUpdate(List<Provenance> provenances) {
        try {
            POSTResultsReturn checkResult = check(provenances);
            if (checkResult.getDataState()) {
                return updateAndReturnPoSTResultsReturn(provenances);
            } else { //errors found in data
                return checkResult;
            }
        } catch (Exception ex) {
            return new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, ex.toString());
        }
    }
    
    /**
     * Get the list of provenances corresponding to given search parameters.
     * @param searchProvenance
     * @param jsonValueFilter
     * @return the list of the provenances corresponding to the given search parameters
     */
    public ArrayList<Provenance> getProvenances(Provenance searchProvenance, String jsonValueFilter) {
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);
        // Get the filter query
        BasicDBObject query = searchQuery(searchProvenance, jsonValueFilter);
        
        // Get paginated documents
        FindIterable<Document> provenancesMongo = provenanceCollection.find(query);
        
        // Define pagination for the request
        provenancesMongo = provenancesMongo.skip(page * pageSize).limit(pageSize);

        ArrayList<Provenance> provenances = new ArrayList<>();
        
        // For each document, create a Provenance instance and add it to the result list
        try (MongoCursor<Document> provenancesCursor = provenancesMongo.iterator()) {
            while (provenancesCursor.hasNext()) {
                Document provenanceDocument = provenancesCursor.next();
                
                // Create and define the Provenance
                Provenance provenance = new Provenance();
                provenance.setUri(provenanceDocument.getString(DB_FIELD_URI));
                provenance.setLabel(provenanceDocument.getString(DB_FIELD_LABEL));
                provenance.setComment(provenanceDocument.getString(DB_FIELD_COMMENT));
                provenance.setMetadata(provenanceDocument.get(DB_FIELD_METADATA));
                
                // Add the provenance to the list
                provenances.add(provenance);
            }
        }
        return provenances;
    }
    
    /**
     * Find the list of uris of provenances with the given label (like).
     * @param label
     * @example
     * {"label": {"$regex": "provenance", "$options": ""}}
     * @return the list of provenances uris.
     */
    public Map<String, String> findUriAndLabelsByLabel(String label) {
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);
        BasicDBObject query = new BasicDBObject();
        query.put("label",  java.util.regex.Pattern.compile(label));
        
        LOGGER.debug(query.toJson());
        
        FindIterable<Document> provenancesMongo = provenanceCollection.find(query);
        Map<String, String> provenances = new HashMap();
        
        try (MongoCursor<Document> datasetCursor = provenancesMongo.iterator()) {
            while (datasetCursor.hasNext()) {
                Document provenanceDocument = datasetCursor.next();
                provenances.put(provenanceDocument.getString("uri"), provenanceDocument.getString("label"));
            }
        }
        
        return provenances;
    }
    
    /**
     * Find the label of a provenance from its uri.
     * @param uri
     * @example
     * {"uri": {"$regex": "http://www.opensilex.org/opensilex/id/provenance/1552386023784", "$options": ""}}
     * @return the label of the provenance
     */
    public String findLabelByUri(String uri) {
        MongoCollection<Document> provenanceCollection = database.getCollection(provenanceCollectionName);
        BasicDBObject query = new BasicDBObject();
        query.put("uri",  java.util.regex.Pattern.compile(uri));
        LOGGER.debug(query.toJson());
        
        FindIterable<Document> provenancesMongo = provenanceCollection.find(query);
        
        try (MongoCursor<Document> provenanceCursor = provenancesMongo.iterator()) {
            if (provenanceCursor.hasNext()) {
                Document provenanceDocument = provenanceCursor.next();
                return provenanceDocument.getString("label");
            } else {
                return null;
            }
        }
    }

    @Override
    public List<Provenance> create(List<Provenance> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<Provenance> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Provenance find(Provenance object) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Provenance findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Provenance> update(List<Provenance> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<Provenance> objects) throws DAOPersistenceException, DAODataErrorAggregateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
