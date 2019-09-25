//******************************************************************************
//                                StudySQLDAO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 1 mai 2019
// Contact: alice.boizet@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static opensilex.service.configuration.DateFormats.DATETIME_POSTGRES_FORMAT;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.exception.ResourceAccessDeniedException;
import opensilex.service.dao.manager.PostgreSQLDAO;
import opensilex.service.datasource.PostgreSQLDataSource;
import opensilex.service.model.Experiment;
import opensilex.service.resource.brapi.AdditionalInfo;
import opensilex.service.resource.dto.experiment.StudyDTO;
import opensilex.service.utils.sql.JoinAttributes;
import opensilex.service.utils.sql.SQLQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Study SQL DAO 
 * @author Alice Boizet <alice.boizet@inra.fr>
 */
public class StudySQLDAO extends PostgreSQLDAO<StudyDTO> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(StudySQLDAO.class);
    
    public String active;
    public String commonCropName;
    public ArrayList<String> seasonDbIds;
    public ArrayList<String> studyDbIds;
    public ArrayList<String> studyNames;
    public String sortBy;
    public String sortOrder;
    
    private final String STUDY_DB_ID = "studyDbId";
    private final String START_DATE = "startDate";
    private final String END_DATE = "endDate";
    private final String STUDY_NAME = "studyName";
    private final String COMMON_CROP_NAME = "commonCropName";
    private final String SEASON = "seasonDbId";
    private final String KEYWORDS = "keywords";
    private final String TRIAL_URI = "trial_uri";
    private final String PROJECT_URI = "project_uri";
    private final String URI = "uri";
    private final String ACRONYME = "acronyme";
    
    
    
    @Override
    public Map<String, String> pkeySQLFieldLink() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, String> relationFieldsJavaSQLObject() {
        Map<String, String> createSQLFields = new HashMap<>();
        createSQLFields.put("studyDbId","uri");
        createSQLFields.put("startDate", "start_date");
        createSQLFields.put("endDate", "end_date");
        createSQLFields.put("studyName", "alias");
        createSQLFields.put("commonCropName", "crop_species");  
        createSQLFields.put("seasonDbId", "campaign");  
        createSQLFields.put("keywords", "keywords"); 
        return createSQLFields;
    }

    @Override
    public StudyDTO findByFields(Map<String, Object> Attr, String table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StudyDTO single(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<StudyDTO> all() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StudyDTO get(ResultSet resultReturnedFromDatabase) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    @Override
    public ArrayList<StudyDTO> allPaginate() {
        ResultSet queryResult = null;
        Connection connection = null;
        Statement statement = null;
        ArrayList<StudyDTO> studies = new ArrayList();
        
        try {
            if (dataSource == null) {
                dataSource = PostgreSQLDataSource.getInstance();
            }
            connection = dataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
            SQLQueryBuilder query = prepareSearchQuery();
            query.appendLimit(String.valueOf(pageSize));
            query.appendOffset(Integer.toString(this.getPage() * this.getPageSize()));
            
            queryResult = statement.executeQuery(query.toString());
            
            LOGGER.debug (query.toString());
            
            UserDAO userDao = new UserDAO();
            userDao.isAdmin(user);
            boolean isAdmin = (user.getAdmin().equals("t") || user.getAdmin().equals("true"));

            while (queryResult.next()) {  
                StudyDTO study = new StudyDTO();
                Map<String, String> sqlFields = relationFieldsJavaSQLObject();
                study.setStudyDbId(queryResult.getString(sqlFields.get(STUDY_DB_ID)));
                study.setStudyName(queryResult.getString(sqlFields.get(STUDY_NAME)));
                study.setName(queryResult.getString(sqlFields.get(STUDY_NAME)));
                study.setCommonCropName(queryResult.getString(sqlFields.get(COMMON_CROP_NAME)));
                study.setStartDate(queryResult.getString(sqlFields.get(START_DATE)));
                study.setEndDate(queryResult.getString(sqlFields.get(END_DATE)));
                AdditionalInfo addInfo = new AdditionalInfo();
                addInfo.setKeywords(queryResult.getString(sqlFields.get(KEYWORDS)));
                study.setAdditionalInfo(addInfo);
                ArrayList<String> seasons = new ArrayList();
                seasons.add(queryResult.getString(sqlFields.get(SEASON)));
                study.setSeasons(seasons);
                Timestamp startDate = queryResult.getTimestamp(sqlFields.get(START_DATE));
                Timestamp endDate = queryResult.getTimestamp(sqlFields.get(END_DATE));
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                if (startDate.compareTo(timestamp) <= 0  && (endDate == null | endDate.compareTo(timestamp) >= 0 )) {
                    study.setActive("true");
                } else {
                    study.setActive("false");                        
                }
                
                //SILEX:INFO
                //check if the user has access to the study  
                Experiment experiment = new Experiment(study.getStudyDbId());
                ExperimentSQLDAO expDAO = new ExperimentSQLDAO();
                if (isAdmin || expDAO.canUserSeeExperiment(user, experiment)) {
                    studies.add(study);
                }
                //\SILEX:INFO
                
            }
            
            studies = getStudiesProject(studies, statement);
                      
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(StudySQLDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (queryResult != null) {
                    queryResult.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(ExperimentSQLDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return studies;
    }

    /**
     * Count studies corresponding to the search user query
     * @example 
     * SELECT DISTINCT count(tr.uri)  FROM "trial" AS tr 
        WHERE tr."crop_species" ILIKE '%maize%'
     * @return the number of studies
     */
    @Override
    public Integer count() {
        SQLQueryBuilder countQuery = prepareSearchQuery();
        countQuery.appendCount();
        countQuery.appendDistinct();
        countQuery.appendSelect(tableAlias + ".uri");
        countQuery.orderBy = null;
        
        LOGGER.debug (countQuery.toString());
        
        Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            if (dataSource == null) {
                dataSource = PostgreSQLDataSource.getInstance();
            }
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(countQuery.toString());

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    
    }

    @Override
    protected StudyDTO compareAndMergeObjects(StudyDTO firstObject, StudyDTO secondObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Builds the SQL query to get studies filtered on different attributes
     * @example 
     * SELECT * FROM trial AS tr
     * WHERE tr.studyNames IN ("studyA","studyB")
     *
     * @return the SQL query
     */
    @Override
    protected SQLQueryBuilder prepareSearchQuery() {
        
        SQLQueryBuilder query = new SQLQueryBuilder();
        tableAlias = "tr";
        table = "trial";
        query.appendFrom(table, tableAlias);
        Map<String, String> sqlFields = relationFieldsJavaSQLObject();
        
        //1. studyURI filter
        if (studyDbIds != null) {
            if (!studyDbIds.isEmpty()) {
                query.appendINConditions(sqlFields.get(STUDY_DB_ID), studyDbIds, tableAlias);
            }
        }      
        
        //2. studyNames filter
        if (studyNames != null) {
            if (!studyNames.isEmpty()) {
                query.appendINConditions(sqlFields.get(STUDY_NAME), studyNames, tableAlias);
            }
        }
        
        //3. commonCropName filter (crop in trial table)
        if (commonCropName != null) {
            query.appendANDWhereConditions(sqlFields.get(COMMON_CROP_NAME), commonCropName, SQLQueryBuilder.CONTAINS_OPERATOR, null, tableAlias);
        }
        if (seasonDbIds != null) {
            if (!seasonDbIds.isEmpty()) {
                query.appendINConditions(sqlFields.get(SEASON), seasonDbIds, tableAlias);
            }
        }
        
        //4. active filter (active = true if Today's date between startDate and endDate)
        if (active != null) {
            if (query.where.length() > 0) {
                query.addAND();
            }             
            query.where += "(";
            DateFormat dateFormat = new SimpleDateFormat(DATETIME_POSTGRES_FORMAT);
            Date todayDate = new Date();
            if ("true".equals(active)) {                
                query.appendWhereConditions(sqlFields.get(START_DATE), dateFormat.format(todayDate), "<=", null, tableAlias);
                query.appendANDWhereConditions(sqlFields.get(END_DATE), dateFormat.format(todayDate), ">=", null, tableAlias);
            } else if ("false".equals(active)) {
                query.appendWhereConditions(sqlFields.get(START_DATE), dateFormat.format(todayDate), ">", null, tableAlias);
                query.appendORWhereConditions(sqlFields.get(END_DATE), dateFormat.format(todayDate), "<", null, tableAlias);
            }
            query.where += ")";
        }
        
        //5. sortBy and sortOrder parameters
        if (sortBy != null) {
            if (sortOrder == null) {
                sortOrder = "ASC";
            }
            query.appendOrderBy(sqlFields.get(sortBy), sortOrder);                     
        }
        
        return query;
    }
    
    @Override
    public List<StudyDTO> create(List<StudyDTO> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<StudyDTO> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<StudyDTO> update(List<StudyDTO> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StudyDTO findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<StudyDTO> objects) throws DAOPersistenceException, DAODataErrorAggregateException, DAOPersistenceException, ResourceAccessDeniedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Gets studies projects.
     * @param experiments ArrayList<Experiment> list of experiments for which 
     * the list of projects is also needed
     * @param statement Statement
     * @example 
     * SELECT p.acronyme, p.uri  FROM "at_trial_project" AS tp 
        INNER JOIN  "project" AS p ON p.uri = tp.project_uri
        WHERE tp."trial_uri" = 'http://www.opensilex.org/demo/DMO-2-2'
     * @return the list given in parameter with the project list for each
     * experiment
     * @throws SQLException 
     */
    private ArrayList<StudyDTO> getStudiesProject(ArrayList<StudyDTO> studies, Statement statement) 
            throws SQLException {

        for (StudyDTO study : studies) {
            ResultSet queryResult = null;
            SQLQueryBuilder query = new SQLQueryBuilder();
            tableAlias = "tp";
            table = "at_trial_project";
            String projectTableAlias = "p";
            String projectTable = "project";
            query.appendSelect("p.acronyme, p.uri");
            query.appendFrom(table, tableAlias);
            query.appendANDWhereConditionIfNeeded(TRIAL_URI, study.getStudyDbId(), "=", null, tableAlias);
            query.appendJoin(JoinAttributes.INNERJOIN, projectTable, projectTableAlias, projectTableAlias + "." + URI + " = " + tableAlias + "." + PROJECT_URI);//p.uri = tp.project_uri");
            LOGGER.debug (query.toString());
            queryResult = statement.executeQuery(query.toString());
            while (queryResult.next()) {
                if (study.getAdditionalInfo() != null) {
                    study.getAdditionalInfo().addProjectsNames(queryResult.getString(ACRONYME));
                } else {
                    AdditionalInfo addInfo = new AdditionalInfo();
                    addInfo.addProjectsNames(queryResult.getString(ACRONYME));
                    study.setAdditionalInfo(addInfo);
                }
            }
        }
        return studies;
    }
}
