//******************************************************************************
//                           ScientificObjectSQLSAO.java 
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: July 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.dao.manager.PhisDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.utils.sql.SQLQueryBuilder;
import opensilex.service.view.brapi.Status;
import opensilex.service.model.ScientificObjectByContext;

/**
 * Scientific objects DAO for a relational database.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectSQLDAO extends PhisDAO<ScientificObjectByContext, Object> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(ScientificObjectSQLDAO.class);
    
    public String uri;
    private final String URI = "uri";
    public String rdfType;
    private final String RDF_TYPE = "rdfType";
    private final String TYPE = "type";
    public String geometry;
    private final String GEOMETRY = "geometry";
    public String namedGraph;
    private final String NAMED_GRAPH = "named_graph";
    
    public ScientificObjectSQLDAO() {
        super();
        setTable("agronomical_object");
        setTableAlias("ao");
    }
    
    private POSTResultsReturn checkAndInsertScientificObjectsList(List<ScientificObjectByContext> newScientificObjects) throws Exception {
        //init result returned maps
        List<Status> insertStatusList = new ArrayList<>();
        boolean dataState = true;
        boolean resultState = true;
        boolean insertionState = true;
        POSTResultsReturn results = null;
        
        if (dataState) {
            PreparedStatement insertPreparedStatement = null;
            
            final String insertGab = "INSERT INTO \"" + table + "\" (\"" + URI + "\", \"" + TYPE + "\", \"" + GEOMETRY + "\", \"" + NAMED_GRAPH + "\") "
                                   + "VALUES (?, ?, ST_GeomFromText(?, 4326), ?)";
            Connection connection = null;
            int inserted = 0;
            int exists = 0;
            
            try {
                //batch
                boolean insertionLeft = true;
                int count = 0;
                
                //connexion + préparation de la transaction
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                
                insertPreparedStatement = connection.prepareStatement(insertGab);
                
                for (ScientificObjectByContext scientificObject : newScientificObjects) {
                    if (!existInDB(scientificObject) && scientificObject.getGeometry() != null) {
                        insertionLeft = true;
                        insertPreparedStatement.setString(1, scientificObject.getUri());
                        insertPreparedStatement.setString(2, scientificObject.getRdfType());
                        insertPreparedStatement.setString(3, scientificObject.getGeometry());
                        insertPreparedStatement.setString(4, scientificObject.getUriExperiment());
                        
                        LOGGER.debug(getTraceabilityLogs() + " quert : " + insertPreparedStatement.toString());
                        
                        insertPreparedStatement.execute();
                        
                        inserted++;
                    } else {
                        exists++;
                    }
                    
                    //Insertion by batch
                    if (++count % batchSize == 0) {
                        insertPreparedStatement.executeBatch();
                        insertionLeft = false;
                    }
                }
                
                if (insertionLeft) {
                    insertPreparedStatement.executeBatch();
                }
                
                connection.commit();
                
                /**
                 * //SILEX:todo
                 *  Tests to review
                 */
                
                // data inserted and existing
                if (exists > 0 && inserted > 0) {
                    results = new POSTResultsReturn(resultState, insertionState, dataState);
                    insertStatusList.add(new Status(StatusCodeMsg.ALREADY_EXISTING_DATA, StatusCodeMsg.INFO, "All scientific objects already exist"));
                    results.setHttpStatus(Response.Status.OK);
                } else {
                    if (exists > 0) { // if existing and inserted data
                        insertStatusList.add(new Status (StatusCodeMsg.ALREADY_EXISTING_DATA, StatusCodeMsg.INFO, String.valueOf(exists) + " scientific objects already exists"));
                    } else { // if non existing data and therefor inserted
                        insertStatusList.add(new Status(StatusCodeMsg.DATA_INSERTED, StatusCodeMsg.INFO, String.valueOf(inserted) + " scientific objects inserted"));
                    }
                }   
                results = new POSTResultsReturn(resultState, insertionState, dataState);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
                
                // Rollback
                if (connection != null) {
                    connection.rollback();
                }
                
                results = new POSTResultsReturn(false, insertionState, dataState);
                insertStatusList.add(new Status(StatusCodeMsg.ERR, StatusCodeMsg.POSTGRESQL_ERROR, e.getMessage()));
                if (e.getNextException() != null) {
                    insertStatusList.add(new Status(StatusCodeMsg.ERR, StatusCodeMsg.POSTGRESQL_ERROR, e.getNextException().getMessage()));
                    insertStatusList.add(new Status(StatusCodeMsg.ERR, StatusCodeMsg.ERR, "Duplicated project in json or in database"));
                }
            } finally {
                if (insertPreparedStatement != null) {
                    insertPreparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            results = new POSTResultsReturn(resultState, insertionState, dataState);
        }
        if (results != null) {
            results.statusList = insertStatusList;
        }
        return results;
    }
    
    public POSTResultsReturn checkAndInsertListAO(List<ScientificObjectByContext> newObjects) {
        POSTResultsReturn postResult;
        try {
            postResult = this.checkAndInsertScientificObjectsList(newObjects);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            postResult = new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
        
        return postResult;
    }

    @Override
    public Map<String, String> pkeySQLFieldLink() {
       Map<String, String> pkeySQLFieldLink = new HashMap<>();
       pkeySQLFieldLink.put(URI, URI);
       return pkeySQLFieldLink;
    }

    @Override
    public Map<String, String> relationFieldsJavaSQLObject() {
        Map<String, String> createSQLFields = new HashMap<>();
        createSQLFields.put(URI, URI);
        createSQLFields.put(RDF_TYPE, TYPE);
        createSQLFields.put(GEOMETRY, GEOMETRY);
        createSQLFields.put("namedGraph", NAMED_GRAPH);
        
        return createSQLFields;
    }

    @Override
    public ScientificObjectByContext findByFields(Map<String, Object> Attr, String table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScientificObjectByContext single(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<ScientificObjectByContext> all() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScientificObjectByContext get(ResultSet result) throws SQLException {
        ScientificObjectByContext scientificObject = new ScientificObjectByContext();
        scientificObject.setUri(result.getString(URI));
        scientificObject.setGeometry(result.getString(GEOMETRY));
        scientificObject.setRdfType(result.getString(TYPE));
        scientificObject.setUriExperiment(result.getString(NAMED_GRAPH));
        
        return scientificObject;
    }

    @Override
    public ArrayList<ScientificObjectByContext> allPaginate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer count() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ScientificObjectByContext compareAndMergeObjects(ScientificObjectByContext fromDB, ScientificObjectByContext object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected SQLQueryBuilder prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Get the geometry of a given scientific object.
     * @param scientificObject
     * @return the geometry, null if no geometry founded.
     * @example
     * SELECT ST_AsGeoJSON(ST_Transform(geometry, 4326)), ao.uri  
     * FROM "agronomical_object" AS ao 
     * WHERE ao."uri" = 'http://www.opensilex.org/opensilex/2019/o19000019'
     * @throws SQLException 
     */
    public String getGeometry(String scientificObject) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        
        try {    
            connection = dataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            SQLQueryBuilder query = new SQLQueryBuilder();
            query.appendSelect("ST_AsGeoJSON(ST_Transform(" + GEOMETRY + ", 4326)), ao." + URI);
            query.appendFrom(table, tableAlias);
            query.appendORWhereConditionIfNeeded(URI, scientificObject, "=", null, tableAlias);

            LOGGER.debug(getTraceabilityLogs() + " quert : " + query.toString());

            ResultSet queryResult = statement.executeQuery(query.toString());
            String geometryToReturn = null;

            if(queryResult.next()) {
                geometryToReturn = queryResult.getString("st_asgeojson");
            }
            
            return geometryToReturn;                
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(ScientificObjectSQLDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null)  {
                connection.close();
            }
        }
    }
    
    /**
     * @param scientificObjectsURIs URIs for which the geometry is requested
     * @return the geometry associated to each URI in GeoSON 
     *          ex : {"type":"Polygon","coordinates":[[[0,0],[10,0],[10,10],[0,10],[0,0]]]}
     * @throws java.sql.SQLException
     */
    public HashMap<String, String> getGeometries(ArrayList<String> scientificObjectsURIs) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {    
            connection = dataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            SQLQueryBuilder query = new SQLQueryBuilder();
            query.appendSelect("ST_AsGeoJSON(ST_Transform(" + GEOMETRY + ", 4326)), ao." + URI);
            query.appendFrom(table, tableAlias);

            for (String scientificObjectURI : scientificObjectsURIs) {
                query.appendORWhereConditionIfNeeded(URI, scientificObjectURI, "=", null, tableAlias);
            }

            LOGGER.debug(getTraceabilityLogs() + " quert : " + query.toString());

            ResultSet queryResult = statement.executeQuery(query.toString());
            HashMap<String, String> geometries = new HashMap<>();

            while (queryResult.next()) {
                geometries.put(queryResult.getString(URI), queryResult.getString("st_asgeojson"));
            }

            return geometries;                
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(ScientificObjectSQLDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null)  {
                connection.close();
            }
        }
    }
    
    /**
     * @param year
     * @return String corresponds to the number of scientific objects 
     *                recorded for the year
     */
    public String getNumberOfScientificObjectForYear(String year) {
        try {
            String toReturn;
            try (Connection connection = dataSource.getConnection(); 
                    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
                SQLQueryBuilder query = new SQLQueryBuilder();
                query.appendSelect("count(*)");
                query.appendFrom(table, tableAlias);
                query.appendANDWhereConditionIfNeeded(URI, "/" + year + "/", "~*", null, tableAlias);
                LOGGER.debug(getTraceabilityLogs() + " quert : " + query.toString());
                ResultSet queryResult = statement.executeQuery(query.toString());
                queryResult.next();
                toReturn = queryResult.getString("count");
            }
            
            return toReturn;
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(ScientificObjectSQLDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }  
    }
    
    /**
     * Update the geometry of a scientific object identified by its URI.
     * @param uri
     * @param geometry
     * @param rdfType
     * @param experiment
     * @example 
     *  UPDATE "trial"
     *  SET "geometry" = ST_GeomFromText("POLYGON(1 0, 0 0, 0 1, 1 0)", 4326)
     *  WHERE "uri" = "http://www.opensilex.org/demo/o1800000000023"
     * @return the updated scientific object.
     * @throws SQLException 
     */
    public ScientificObjectByContext updateOneGeometry(String uri, String geometry, String rdfType, String experiment) throws Exception {
        ScientificObjectByContext scientificObject = new ScientificObjectByContext(uri);
        scientificObject.setGeometry(geometry);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            if (geometry == null || geometry.isEmpty()) {
                if (!existInDB(scientificObject)) { //The scientific object must be delete from the database.
                    String deleteScientificObject = "DELETE FROM \"" + table + "\" WHERE \"" + URI + "\" = '" + uri + "'";
                    LOGGER.debug(deleteScientificObject);
                    statement.execute(deleteScientificObject);
                }
            } else {
                if (!existInDB(scientificObject)) { //The scientific object must be inserted in the database.
                    String insertScientificObject = "INSERT INTO \"" + table + "\" (\"" + URI + "\", \"" + TYPE + "\", \"" + GEOMETRY + "\", \"" + NAMED_GRAPH + "\") "
                            + "VALUES (\"" + uri + "\", \"" + rdfType + "\", ST_GeomFromText(\"" + geometry + "\", 4326), \"" + experiment + "\")";
                    
                    LOGGER.debug(insertScientificObject);
                    statement.execute(insertScientificObject);
                } else { //The scientific object already exist in the database and must be updated.
                    String updateGeometry = "UPDATE \"" + table + "\" "
                            + "SET \"" + GEOMETRY + "\" = ST_GeomFromText('" + geometry + "', 4326) "
                            + "WHERE \"" + URI + "\" = '" + uri + "'";
                    
                    LOGGER.debug(updateGeometry);
                    statement.executeUpdate(updateGeometry);
                }
            }
            statement.close();
            connection.close();
        }
        
        return scientificObject;
    }

    @Override
    public List<ScientificObjectByContext> create(List<ScientificObjectByContext> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<ScientificObjectByContext> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ScientificObjectByContext> update(List<ScientificObjectByContext> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScientificObjectByContext findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<ScientificObjectByContext> objects) throws DAOPersistenceException, DAODataErrorAggregateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public POSTResultsReturn checkAndInsert(Object newObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public POSTResultsReturn checkAndInsertList(List<Object> newObjects) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public POSTResultsReturn checkAndUpdateList(List<Object> newObjects) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
