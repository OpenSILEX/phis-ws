//**********************************************************************************************
//                                       AgronomicalObjectDao.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: July 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  August 28, 2017
// Subject: A DAO specific to retrieve agronomical object data
//***********************************************************************************************
package phis2ws.service.dao.phis;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.dao.manager.DAOPhisBrapi;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.resources.dto.AgronomicalObjectDTO;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.utils.sql.SQLQueryBuilder;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.model.phis.AgronomicalObject;

public class AgronomicalObjectDAO extends DAOPhisBrapi<AgronomicalObject, AgronomicalObjectDTO> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(AgronomicalObjectDAO.class);
    
    public String uri;
    private final String URI = "uri";
    public String rdfType;
    private final String RDF_TYPE = "rdfType";
    private final String TYPE = "type";
    public String geometry;
    private final String GEOMETRY = "geometry";
    public String namedGraph;
    private final String NAMED_GRAPH = "named_graph";
    
    public AgronomicalObjectDAO() {
        super();
        setTable("agronomical_object");
        setTableAlias("ao");
    }

    @Override
    public POSTResultsReturn checkAndInsert(AgronomicalObjectDTO newObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private POSTResultsReturn checkAndInsertAgronomicalObjectsList(List<AgronomicalObject> newAgronomicalObjects) throws Exception {
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
                
                for (AgronomicalObject agronomicalObject : newAgronomicalObjects) {
                    if (!existInDB(agronomicalObject)) {
                        insertionLeft = true;
                        insertPreparedStatement.setString(1, agronomicalObject.getUri());
                        insertPreparedStatement.setString(2, agronomicalObject.getRdfType());
                        insertPreparedStatement.setString(3, agronomicalObject.getGeometry());
                        insertPreparedStatement.setString(4, agronomicalObject.getUriExperiment());
                        
                        LOGGER.debug(getTraceabilityLogs() + " quert : " + insertPreparedStatement.toString());
                        
                        insertPreparedStatement.execute();
                        
                        inserted++;
                    } else {
                        exists++;
                    }
                    
                    //Insertion par batch
                    if (++count % batchSize == 0) {
                        insertPreparedStatement.executeBatch();
                        insertionLeft = false;
                    }
                }
                
                if (insertionLeft) {
                    insertPreparedStatement.executeBatch();
                }
                
                connection.commit();
////////////////////
//ATTENTION, vérifications à re regarder et re vérifier
//////////////////
                //Si data insérées et existantes
                if (exists > 0 && inserted > 0) {
                    results = new POSTResultsReturn(resultState, insertionState, dataState);
                    insertStatusList.add(new Status(StatusCodeMsg.ALREADY_EXISTING_DATA, StatusCodeMsg.INFO, "All agronomical objects already exist"));
                    results.setHttpStatus(Response.Status.OK);
                } else {
                    if (exists > 0) { //Si données existantes et aucunes insérées
                        insertStatusList.add(new Status (StatusCodeMsg.ALREADY_EXISTING_DATA, StatusCodeMsg.INFO, String.valueOf(exists) + " agronomical objects already exists"));
                    } else { //Si données qui n'existent pas et donc sont insérées
                        insertStatusList.add(new Status(StatusCodeMsg.DATA_INSERTED, StatusCodeMsg.INFO, String.valueOf(inserted) + " agronomical objects inserted"));
                    }
                }   
                results = new POSTResultsReturn(resultState, insertionState, dataState);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
                
                //Rollback
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
    
    public POSTResultsReturn checkAndInsertListAO(List<AgronomicalObject> newObjects) {
        POSTResultsReturn postResult;
        try {
            postResult = this.checkAndInsertAgronomicalObjectsList(newObjects);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            postResult = new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
        
        return postResult;
    }

    @Override
    public POSTResultsReturn checkAndUpdateList(List<AgronomicalObjectDTO> newObjects) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public AgronomicalObject findByFields(Map<String, Object> Attr, String table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AgronomicalObject single(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<AgronomicalObject> all() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AgronomicalObject get(ResultSet result) throws SQLException {
        AgronomicalObject agronomicalObject = new AgronomicalObject();
        agronomicalObject.setUri(result.getString(URI));
        agronomicalObject.setGeometry(result.getString(GEOMETRY));
        agronomicalObject.setRdfType(result.getString(TYPE));
        agronomicalObject.setUriExperiment(result.getString(NAMED_GRAPH));
        
        return agronomicalObject;
    }

    @Override
    public ArrayList<AgronomicalObject> allPaginate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer count() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected AgronomicalObject compareAndMergeObjects(AgronomicalObject fromDB, AgronomicalObject object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected SQLQueryBuilder prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * 
     * @param agronomicalObjectsURIs la liste des uris pour lesquelles on veut la géométrie
     * @return la géométrie associée à chaque uri, dans la BD, en geojson 
     *          ex : {"type":"Polygon","coordinates":[[[0,0],[10,0],[10,10],[0,10],[0,0]]]}
     * @throws java.sql.SQLException
     */
    public HashMap<String, String> getGeometries(ArrayList<String> agronomicalObjectsURIs) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {    
            connection = dataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            SQLQueryBuilder query = new SQLQueryBuilder();
            query.appendSelect("ST_AsGeoJSON(ST_Transform(" + GEOMETRY + ", 4326)), ao." + URI);
            query.appendFrom(table, tableAlias);

            for (String agronomicalObjectURI : agronomicalObjectsURIs) {
                query.appendORWhereConditionIfNeeded(URI, agronomicalObjectURI, "=", null, tableAlias);
            }

            LOGGER.debug(getTraceabilityLogs() + " quert : " + query.toString());

            ResultSet queryResult = statement.executeQuery(query.toString());
            HashMap<String, String> geometries = new HashMap<>();

            while (queryResult.next()) {
                geometries.put(queryResult.getString(URI), queryResult.getString("st_asgeojson"));
            }

            return geometries;                
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(AgronomicalObjectDAO.class.getName()).log(Level.SEVERE, null, ex);
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
     * 
     * @param year
     * @return String correspondant au nombre d'agronomical objects actuellement 
     *                enregistrés sur l'année year
     */
    public String getNumberOfAgronomicalObjectForYear(String year) {
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
            java.util.logging.Logger.getLogger(AgronomicalObjectDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }  
    }

    @Override
    public POSTResultsReturn checkAndInsertList(List<AgronomicalObjectDTO> newObjects) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
