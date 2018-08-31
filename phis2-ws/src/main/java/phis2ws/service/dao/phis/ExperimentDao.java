//**********************************************************************************************
//                                       ExperimentDao.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: January 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 31 2017 : Passage de trial à experiment
// Subject: A DAO specific to retrieve experiment data
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
import phis2ws.service.view.brapi.Status;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.model.User;
import phis2ws.service.resources.dto.ExperimentDTO;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.utils.sql.JoinAttributes;
import phis2ws.service.utils.sql.SQLQueryBuilder;
import phis2ws.service.view.model.phis.Contact;
import phis2ws.service.view.model.phis.Group;
import phis2ws.service.view.model.phis.Project;
import phis2ws.service.view.model.phis.Experiment;


public class ExperimentDao extends DAOPhisBrapi<Experiment, ExperimentDTO> {

    final static Logger LOGGER = LoggerFactory.getLogger(ExperimentDao.class);
    
    public String uri;
    public String startDate;
    public String endDate;
    public String field;
    public String campaign;
    public String place;
    public String alias;
    public String keyword;
    public String groups;
    public String cropSpecies;
    
    public ExperimentDao() {
        super();
        setTable("trial");
        setTableAlias("t");
    }
    
    public ExperimentDao(String uri) {
        super();
        this.uri = uri;
        setTable("trial");
        setTableAlias("t");
    }

    @Override
    public Map<String, String> pkeySQLFieldLink() {
       Map<String, String> pkeySQLFieldLink = new HashMap<>();
       pkeySQLFieldLink.put("uri", "uri");
       return pkeySQLFieldLink;
    }

    @Override
    public Map<String, String> relationFieldsJavaSQLObject() {
        Map<String, String> createSQLFields = new HashMap<>();
        createSQLFields.put("uri","uri");
        createSQLFields.put("startDate", "start_date");
        createSQLFields.put("endDate", "end_date");
        createSQLFields.put("field", "field");
        createSQLFields.put("campaign", "campaign");
        createSQLFields.put("place", "place");
        createSQLFields.put("alias", "alias");
        createSQLFields.put("comment", "comment");
        createSQLFields.put("keywords", "keywords");
        createSQLFields.put("objective", "objective");
        createSQLFields.put("groups", "groups");
        createSQLFields.put("cropSpecies", "crop_species");
        
        return createSQLFields;
    }

    @Override
    public Experiment findByFields(Map<String, Object> Attr, String table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Experiment single(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<Experiment> all() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Experiment get(ResultSet result) throws SQLException {
        Experiment experiment = new Experiment();
        experiment.setUri(result.getString("uri"));
        experiment.setStartDate(result.getString("start_date"));
        experiment.setEndDate(result.getString("end_date"));
        experiment.setField(result.getString("field"));
        experiment.setCampaign(result.getString("campaign"));
        experiment.setPlace(result.getString("place"));
        experiment.setAlias(result.getString("alias"));
        experiment.setComment(result.getString("comment"));
        experiment.setKeywords(result.getString("keywords"));
        experiment.setObjective(result.getString("objective"));
        experiment.setCropSpecies(result.getString("crop_species"));

        return experiment;
    }

    @Override
    public ArrayList<Experiment> allPaginate() {
        ResultSet queryResult = null;
        Connection connection = null;
        Statement statement = null;
        ArrayList<Experiment> experiments = new ArrayList();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
            SQLQueryBuilder query = new SQLQueryBuilder();
            
            Map<String, String> sqlFields = relationFieldsJavaSQLObject();
           
            //Ajout des conditions dans la requête
            query.appendFrom("trial", tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("uri"), uri, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("startDate"), startDate, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("endDate"), endDate, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("field"), field, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("campaign"), campaign, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("place"), place, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("alias"), alias, "ILIKE", null, tableAlias);
            query.appendANDWhereConditionIfNeeded(sqlFields.get("keyword"), keyword, "ILIKE", null, tableAlias);
            query.appendLimit(String.valueOf(pageSize));
            
            queryResult = statement.executeQuery(query.toString());
            
            UserDaoPhisBrapi userDao = new UserDaoPhisBrapi();
            userDao.isAdmin(user);
            boolean isAdmin = (user.getAdmin().equals("t") || user.getAdmin().equals("true"));

            while (queryResult.next()) {
                //SILEX:access
                Experiment experiment = get(queryResult);
                if (isAdmin || canUserSeeExperiment(user, experiment)) {
                    experiments.add(get(queryResult));
                }
                //\SILEX:access
            }
            
            //SILEX:dbjoin
            experiments = getExperimentsContacts(experiments, statement);
            //\SILEX:dbjoin
            experiments = getExperimentsProjects(experiments, statement);
            for (Experiment experiment : experiments) {
                experiment.setGroupList(this.getExperimentGroups(experiment));
            }
            
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(ExperimentDao.class.getName()).log(Level.SEVERE, null, ex);
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
                java.util.logging.Logger.getLogger(ExperimentDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return experiments;
    }
    
    /**
     * 
     * @param experiments ArrayList<Experiment> liste des expérimentations pour lesquels on veut aussi avoir la liste des projets
     * @param statement Statement
     * @return la liste envoyée en paramètre contenant en plus pour chaque expérimentation la liste de ses projets
     * @throws SQLException 
     */
    private ArrayList<Experiment> getExperimentsProjects(ArrayList<Experiment> experiments, Statement statement) throws SQLException {
        for (Experiment experiment : experiments) {
            SQLQueryBuilder query = new SQLQueryBuilder();
            query.appendSelect("p.acronyme, p.uri");
            query.appendFrom("at_trial_project", "tp");
            query.appendANDWhereConditionIfNeeded("trial_uri", experiment.getUri(), "=", null, "tp");
            query.appendJoin(JoinAttributes.INNERJOIN, "project", "p", "p.uri = tp.project_uri");
            
            ResultSet queryResult = statement.executeQuery(query.toString());
            while (queryResult.next()) {
                Project project = new Project(queryResult.getString("uri"));
                project.setAcronyme(queryResult.getString("acronyme"));
                experiment.addProject(project);
            }
        }
        return experiments;
    }
    
    /**
     * @param statement Statement
     * @param experiments ArrayList<Experiment> liste des expérimentations pour lesquels on veut aussi avoir la liste des contacts
     * @return la liste envoyée en paramètre contenant en plus pour chaque expérimentation la liste de ses contacts
     */
    private ArrayList<Experiment> getExperimentsContacts(ArrayList<Experiment> experiments, Statement statement) throws SQLException {
        for (Experiment experiment : experiments) {
            SQLQueryBuilder query = new SQLQueryBuilder();
            query.appendSelect("u.email, u.first_name, u.family_name, tu.type");
            query.appendFrom("at_trial_users", "tu");
            query.appendANDWhereConditionIfNeeded("trial_uri", experiment.getUri(), "=", null, "tu");
            query.appendJoin(JoinAttributes.INNERJOIN, "users", "u", "u.email = tu.users_email");
            
            ResultSet queryResult = statement.executeQuery(query.toString());
            while (queryResult.next()) {
                Contact contact = new Contact();
                contact.setEmail(queryResult.getString("email"));
                contact.setFirstName(queryResult.getString("first_name"));
                contact.setFamilyName(queryResult.getString("family_name"));
                contact.setType(queryResult.getString("type"));
                experiment.addContact(contact);
            }
        }
        
        return experiments;
    }
    
    @Override
    public Integer count() {
     
     SQLQueryBuilder query = new SQLQueryBuilder();
     query.appendCount();
     query.appendDistinct();
     query.appendSelect(tableAlias + ".uri");
     query.appendFrom(table, tableAlias);
     
     if (uri != null) {
         query.appendWhereConditions("uri", uri, "=", null, tableAlias);
     }
     
     Connection connection = null;
     ResultSet resultSet = null;
     Statement statement = null;
     
     try {
         connection = dataSource.getConnection();
         statement = connection.createStatement();
         resultSet = statement.executeQuery(query.toString());
         
         if(resultSet.next()) {
             return resultSet.getInt(1);
         }else{
             return 0;
         }
         
         
     } catch(SQLException e) {
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
    protected Experiment compareAndMergeObjects(Experiment fromDB, Experiment object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected SQLQueryBuilder prepareSearchQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private POSTResultsReturn checkAndInsertExperimentList(List<ExperimentDTO> newExperiments) throws SQLException, Exception {
        //init results returned maps
        List<Status> insertStatusList = new ArrayList<>();
        boolean dataState = true;
        boolean resultState = true;
        boolean insertionState = true;
        POSTResultsReturn results = null;
        ArrayList<Experiment> experiments = new ArrayList<>();
        
        for (ExperimentDTO experimentDTO : newExperiments) {
            Experiment experiment = experimentDTO.createObjectFromDTO();
            experiments.add(experiment);
        }
        
        if (dataState) {
            PreparedStatement insertPreparedStatementExperiments = null;
            PreparedStatement insertPreparedStatementAtExperimentProject = null;
            PreparedStatement insertPreparedStatementAtGroupExperiment = null;
            PreparedStatement insertPreparedStatementAtExperimentUsers = null;
            
            final String insertGabExperiment = "INSERT INTO \"trial\""
                                    + "(\"uri\", \"start_date\", \"end_date\", \"field\","
                                    + "\"campaign\", \"place\", \"alias\", \"comment\", \"keywords\","
                                    + " \"objective\", \"crop_species\")"
                                    + " VALUES (?, to_date(?, 'YYYY-MM-DD'), to_date(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?, ?, ?, ?)";
            
            final String insertGabAtExperimentProject = "INSERT INTO \"at_trial_project\""
                                    + "(\"project_uri\", \"trial_uri\")"
                                    + " VALUES (?, ?)";
            
            final String insertGabAtExperimentGroup = "INSERT INTO \"at_group_trial\""
                                    + "(\"group_uri\", \"trial_uri\")"
                                    + " VALUES (?, ?)";
            final String insertGabAtExperimentUsers = "INSERT INTO \"at_trial_users\""
                    + "(\"trial_uri\", \"users_email\", \"type\")"
                    + " VALUES (?, ?, ?)";
            
            Connection con = null;
            int inserted = 0;
            int exists = 0;
            
            try {
                //batch
                boolean insertionLeft = true;
                int count = 0;
                
                //connexion + préparation de la transaction
                con = dataSource.getConnection();
                con.setAutoCommit(false);
                
                insertPreparedStatementExperiments = con.prepareStatement(insertGabExperiment);
                insertPreparedStatementAtExperimentProject = con.prepareStatement(insertGabAtExperimentProject);
                insertPreparedStatementAtGroupExperiment = con.prepareStatement(insertGabAtExperimentGroup);
                insertPreparedStatementAtExperimentUsers = con.prepareStatement(insertGabAtExperimentUsers);
                
                for (Experiment experiment : experiments) {
                    if (!existInDB(experiment)) {
                        insertionLeft = true;
                        insertPreparedStatementExperiments.setString(1, experiment.getUri());
                        insertPreparedStatementExperiments.setString(2, experiment.getStartDate());
                        insertPreparedStatementExperiments.setString(3, experiment.getEndDate()); 
                        insertPreparedStatementExperiments.setString(4, experiment.getField());
                        insertPreparedStatementExperiments.setString(5, experiment.getCampaign());
                        insertPreparedStatementExperiments.setString(6, experiment.getPlace());
                        insertPreparedStatementExperiments.setString(7, experiment.getAlias());
                        insertPreparedStatementExperiments.setString(8, experiment.getComment());
                        insertPreparedStatementExperiments.setString(9, experiment.getKeywords());
                        insertPreparedStatementExperiments.setString(10, experiment.getObjective());
                        insertPreparedStatementExperiments.setString(11, experiment.getCropSpecies());
                        
                        //ajout dans les logs de qui a fait quoi (traçabilité)
                        String log = "";
                        if (remoteUserAdress != null) {
                            log += "IP Address " + remoteUserAdress + " - ";
                        }
                        if (user != null) {
                            log += "User : " + user.getEmail() + " - ";
                        }
                       
                        insertPreparedStatementExperiments.execute();
                        
                        //Ajout dans at_trial_project des projets auxquels l'essai appartient
                        for (Project project : experiment.getProjects()) {
                            insertPreparedStatementAtExperimentProject.setString(1, project.getUri());
                            insertPreparedStatementAtExperimentProject.setString(2, experiment.getUri());
                            LOGGER.trace(log + " quert : " + insertPreparedStatementAtExperimentProject.toString());
                            insertPreparedStatementAtExperimentProject.execute();
                        }
                        
                        //Ajout dans at_group_trial des groupes auxquels l'essai appartient
                        for (Group group : experiment.getGroups()) {
                            insertPreparedStatementAtGroupExperiment.setString(1, group.getUri());
                            insertPreparedStatementAtGroupExperiment.setString(2, experiment.getUri());
                            LOGGER.trace(log + " quert : " + insertPreparedStatementAtExperimentProject.toString());
                            insertPreparedStatementAtGroupExperiment.execute();
                        }
                        
                        //Ajout dans at_trial_users des contacts 
                        for (Contact contact : experiment.getContacts()) {
                            insertPreparedStatementAtExperimentUsers.setString(1, experiment.getUri());
                            insertPreparedStatementAtExperimentUsers.setString(2, contact.getEmail());
                            insertPreparedStatementAtExperimentUsers.setString(3, contact.getType());
                            insertPreparedStatementAtExperimentUsers.execute();
                            LOGGER.trace(log + " quert : " + insertPreparedStatementAtExperimentUsers.toString());
                        }
                        
                        inserted++;
                    } else {
                        exists++;
                    }
                    
                    //Insertion par batch
                    if (++count % batchSize == 0) {
                        insertPreparedStatementExperiments.executeBatch();
                        insertPreparedStatementAtExperimentProject.executeBatch();
                        insertPreparedStatementAtGroupExperiment.executeBatch();
                        insertPreparedStatementAtExperimentUsers.executeBatch();
                        insertionLeft = false;
                    }
                }
                
                if (insertionLeft) {
                    insertPreparedStatementExperiments.executeBatch(); //checkAndInsert remaining records
                    insertPreparedStatementAtExperimentProject.executeBatch();
                    insertPreparedStatementAtGroupExperiment.executeBatch();
                    insertPreparedStatementAtExperimentUsers.executeBatch();
                }
                
                con.commit(); //Envoi des données ds bd
                
////////////////////
    //ATTENTION, vérifications à re regarder et re vérifier
//////////////////
                //Si data insérées et existantes
                if (exists > 0 && inserted > 0) {
                    results = new POSTResultsReturn(resultState, insertionState, dataState);
                    insertStatusList.add(new Status("Already existing data", StatusCodeMsg.INFO, "All experiments already exist"));
                    results.setHttpStatus(Response.Status.OK);
                    results.statusList = insertStatusList;
                } else {
                    if (exists > 0) { //Si données existantes et aucunes insérées
                        insertStatusList.add(new Status ("Already existing data", StatusCodeMsg.INFO, String.valueOf(exists) + " experiment already exists"));
                    } else { //Si données qui n'existent pas et donc sont insérées
                        insertStatusList.add(new Status("Data inserted", StatusCodeMsg.INFO, String.valueOf(inserted) + " experiments inserted"));
                    }
                }   
                results = new POSTResultsReturn(resultState, insertionState, dataState);
                
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
                
                //Rollback
                if (con != null) {
                    con.rollback();
                }
                
                results = new POSTResultsReturn(false, insertionState, dataState);
                insertStatusList.add(new Status("Error", StatusCodeMsg.POSTGRESQL_ERROR, e.getMessage()));
                if (e.getNextException() != null) {
                    insertStatusList.add(new Status("Error", StatusCodeMsg.POSTGRESQL_ERROR, e.getNextException().getMessage()));
                    insertStatusList.add(new Status("Error", StatusCodeMsg.ERR, "Duplicated experiment in json or in database"));
                }
                results.statusList = insertStatusList;
            } finally {
                if (insertPreparedStatementExperiments != null) {
                    insertPreparedStatementExperiments.close();
                }
                if (insertPreparedStatementAtGroupExperiment != null) {
                    insertPreparedStatementAtGroupExperiment.close();
                }
                if (insertPreparedStatementAtExperimentProject != null) {
                    insertPreparedStatementAtExperimentProject.close();
                }
                if (insertPreparedStatementAtExperimentUsers != null) {
                    insertPreparedStatementAtExperimentUsers.close();
                }
                if (con != null) {
                    con.close();
                }
            }
        } else {
            results = new POSTResultsReturn(resultState, insertionState, dataState);
            results.statusList = insertStatusList;
        }
        
        return results;
    }
    
    /**
     * Vérifie tous les experimentations et les insère dans la BD Postgres
     * @param newObjects liste des expérimentations
     * @return
     */
    @Override
    public POSTResultsReturn checkAndInsertList(List<ExperimentDTO> newObjects) {
        
         POSTResultsReturn postResult;
        try {
            postResult = this.checkAndInsertExperimentList(newObjects);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            postResult = new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
        
        return postResult;
    }
    

    @Override
    public POSTResultsReturn checkAndInsert(ExperimentDTO newObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }  
    
    /**
     * 
     * @param experiment Experiment
     * @return la liste des groupes ayant accès à l'expérimentation (lite des noms de groupes)
     * @throws java.sql.SQLException
     */
    public ArrayList<Group> getExperimentGroups(Experiment experiment) throws SQLException {
        ResultSet result = null;
        Connection connection = null;
        Statement statement = null;
        ArrayList<Group> experimentGroups = new ArrayList<>();
        
        try {
            if (this.existInDB(experiment)) {
                //Récupération des groupes dans les tables at_group_trial et group
                SQLQueryBuilder query = new SQLQueryBuilder();
                query.appendSelect("gp.uri, gp.level, gp.name");
                query.appendFrom("at_group_trial", "gt");
                query.appendANDWhereConditionIfNeeded("trial_uri", experiment.getUri(), "=", null, "gt");
                query.appendJoin(JoinAttributes.INNERJOIN, "group", "gp", "gt.group_uri = gp.uri");
                
                connection = dataSource.getConnection();
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);

                result = statement.executeQuery(query.toString());
                while (result.next()) {
                    Group group = new Group(result.getString("uri"));
                    group.setLevel(result.getString("level"));
                    group.setName(result.getString("name"));
                    experimentGroups.add(group);
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ExperimentDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (result != null) {
                result.close();
            }
        }
        
        return experimentGroups;
    }
    
    /**
     * 
     * @param u User 
     * @param experiment Experiment
     * @return true si l'utilisateur fait partie d'un des groupes ayant accès à l'experimentation, false sinon
     */
    public boolean canUserSeeExperiment(User u, Experiment experiment) {
        try {
            UserDaoPhisBrapi userDao = new UserDaoPhisBrapi();

            ArrayList<Group> experimentGroups = this.getExperimentGroups(experiment);
            //Quand l'essai n'est dans aucun groupe, il est public donc l'utilisateur peut le consulter
            if (experimentGroups.isEmpty()) { 
                return true;
            } else {
                ArrayList<Group> userGroups = userDao.getUserGroups(u);
                for (Group userGroup : userGroups) {
                    for (Group experimentGroup : experimentGroups) {
                        if (userGroup.getUri().equals(experimentGroup.getUri())) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(ExperimentDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    /**
     * 
     * @param experiment
     * @return boolean true si l'utilisateur peut modifier l'essai 
     *                      (administrateur ou membre d'un groupe propriétaire de la donnée)
     *                 false sinon
     * @throws SQLException 
     */
    private boolean canUserUpdateExperiment(Experiment experiment) throws SQLException {
        UserDaoPhisBrapi userDao = new UserDaoPhisBrapi();
        
        if (userDao.isAdmin(user)) {
            return true;
        }
        
        ArrayList<Group> userGroups = userDao.getUserGroups(user);
        ArrayList<Group> experimentGroups = this.getExperimentGroups(experiment);
        
        if (!experimentGroups.isEmpty() || !userGroups.isEmpty()) {
            for (Group userGroup : userGroups) {
                for (Group experimentGroup : experimentGroups) {
                    if ( userGroup.getLevel().equals("Owner")
                            && userGroup.getUri().equals(experimentGroup.getUri())) {

                        return true;
                    }
                }
            }
        }
        return false;                
    }
    
    /**
     * 
     * @param updateExperiments List<ExperimentDTO> liste des expérimentations à modifier
     * @return POSTResultReturn, le resultat des updates
     */
    private POSTResultsReturn checkAndUpdateExperimentList(List<ExperimentDTO> updateExperiments) throws SQLException, Exception {
        //init result returned maps
        List<Status> insertStatusList = new ArrayList<>();
        boolean allExperimentsAlreadyInDB = true;
        POSTResultsReturn results = null;
        
        ArrayList<Experiment> experiments = new ArrayList<>();
        
        String log = getTraceabilityLogs();
        
        //1. On récupère la liste des expérimentations et on vérifie qu'ils existent bien en BD
        //   et que l'utilisateur a bien le droit de les modifier 
        if (updateExperiments != null && !updateExperiments.isEmpty()) {
            for (ExperimentDTO experimentDTO : updateExperiments) {
                Experiment experiment = experimentDTO.createObjectFromDTO();
                if (existInDB(experiment) && canUserUpdateExperiment(experiment)) {
                    experiments.add(experiment);
                } else {
                    allExperimentsAlreadyInDB = false;
                    insertStatusList.add(new Status("Data error", StatusCodeMsg.ERR, "Unknown experiment"));
                }
            }
        }
        
        //2. Modification de la BD en fonction des données envoyées
        if (allExperimentsAlreadyInDB) {
            PreparedStatement updatePreparedStatementExperiment = null;
            PreparedStatement deletePreparedStatementProject = null;
            PreparedStatement insertPreparedStatementProject = null;
            PreparedStatement deletePreparedStatementGroup = null;
            PreparedStatement insertPreparedStatementGroup = null;
            PreparedStatement deletePreparedStatementContact = null;
            PreparedStatement insertPreparedStatementContact = null;
            Connection connection = null;
            
            final String updateExperiment = "UPDATE \"trial\" SET \"start_date\" = to_date(?, 'YYYY:MM:DD'), \"end_date\" = to_date(?, 'YYYY:MM:DD'),"
                    + " \"field\" = ?,"
                    + "\"place\" = ?, \"alias\" = ?, \"comment\" = ?, \"keywords\" = ?, \"objective\" = ?,"
                    + "\"crop_species\" = ? "
                    + "WHERE \"uri\" = ?";
            final String deleteProject = "DELETE FROM \"at_trial_project\" WHERE \"trial_uri\" = ?";
            final String insertProject = "INSERT INTO \"at_trial_project\" (\"trial_uri\", \"project_uri\") VALUES (?, ?)";
            final String deleteGroup = "DELETE FROM \"at_group_trial\" WHERE \"trial_uri\" = ?";
            final String insertGroup = "INSERT INTO \"at_group_trial\" (\"trial_uri\", \"group_uri\") VALUES (?, ?)";
            final String deleteContact = "DELETE FROM \"at_trial_users\" WHERE \"trial_uri\" = ?";
            final String insertContact = "INSERT INTO \"at_trial_users\" (\"trial_uri\", \"users_email\", \"type\") VALUES (?, ?, ?)";
            
            try {
                //Batch
                int count = 0;
                boolean insertionLeft = true;
                
                //Connection + préparation de la transaction
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                
                updatePreparedStatementExperiment = connection.prepareStatement(updateExperiment);
                deletePreparedStatementProject = connection.prepareStatement(deleteProject);
                insertPreparedStatementProject = connection.prepareStatement(insertProject);
                deletePreparedStatementGroup = connection.prepareStatement(deleteGroup);
                insertPreparedStatementGroup = connection.prepareStatement(insertGroup);
                deletePreparedStatementContact = connection.prepareStatement(deleteContact);
                insertPreparedStatementContact = connection.prepareStatement(insertContact);
                
                for (Experiment experiment : experiments) {
                    //Update des données de la table trial
                    updatePreparedStatementExperiment.setString(1, experiment.getStartDate());
                    updatePreparedStatementExperiment.setString(2, experiment.getEndDate());
                    updatePreparedStatementExperiment.setString(3, experiment.getField());
                    updatePreparedStatementExperiment.setString(4, experiment.getPlace());
                    updatePreparedStatementExperiment.setString(5, experiment.getAlias());
                    updatePreparedStatementExperiment.setString(6, experiment.getComment());
                    updatePreparedStatementExperiment.setString(7, experiment.getKeywords());
                    updatePreparedStatementExperiment.setString(8, experiment.getObjective());
                    updatePreparedStatementExperiment.setString(9, experiment.getCropSpecies());
                    updatePreparedStatementExperiment.setString(10, experiment.getUri());
                    LOGGER.trace(log + " quert : " + updatePreparedStatementExperiment.toString());
                    updatePreparedStatementExperiment.execute();
                    
                    
                    //Delete des Projets
                    deletePreparedStatementProject.setString(1, experiment.getUri());
                    deletePreparedStatementProject.execute();
                    LOGGER.trace(log + " quert : " + deletePreparedStatementProject.toString());
                    
                    //Insert des projets
                    if (experiment.getProjects() != null && !experiment.getProjects().isEmpty()) {
                        for (Project project : experiment.getProjects()) {
                            insertPreparedStatementProject.setString(1, experiment.getUri());
                            insertPreparedStatementProject.setString(2, project.getUri());
                            insertPreparedStatementProject.execute();
                            LOGGER.trace(log + " quert : " + insertPreparedStatementProject);
                        }
                    }
                    
                    //Delete des groups 
                    deletePreparedStatementGroup.setString(1, experiment.getUri());
                    deletePreparedStatementGroup.execute();
                    LOGGER.trace(log + " quert : " + deletePreparedStatementGroup.toString());
                    
                    //Insert des groupes
                    if (experiment.getGroups() != null && !experiment.getGroups().isEmpty()) {
                        for (Group group : experiment.getGroups()) {
                            insertPreparedStatementGroup.setString(1, experiment.getUri());
                            insertPreparedStatementGroup.setString(2, group.getUri());
                            insertPreparedStatementGroup.execute();
                            LOGGER.trace(log + " quert : " + insertPreparedStatementGroup.toString());
                        }
                    }
                    
                    //Delete des contacts
                    deletePreparedStatementContact.setString(1, experiment.getUri());
                    deletePreparedStatementContact.execute();
                    LOGGER.trace(log + " quert : " + deletePreparedStatementContact.toString());
                    
                    //Insert des contacts
                    if (experiment.getContacts() != null && !experiment.getContacts().isEmpty()) {
                        for (Contact contact : experiment.getContacts()) {
                            insertPreparedStatementContact.setString(1, experiment.getUri());
                            insertPreparedStatementContact.setString(2, contact.getEmail());
                            insertPreparedStatementContact.setString(3, contact.getType());
                            insertPreparedStatementContact.execute();
                            LOGGER.trace(log + " quert : " + insertPreparedStatementContact.toString());
                        }
                    }
                    
                    //Insertion par batch
                    if (++count % batchSize == 0) {
                        updatePreparedStatementExperiment.executeBatch();
                        deletePreparedStatementProject.executeBatch();
                        insertPreparedStatementProject.executeBatch();
                        deletePreparedStatementGroup.executeBatch();
                        insertPreparedStatementGroup.executeBatch();
                        deletePreparedStatementContact.executeBatch();
                        insertPreparedStatementContact.executeBatch();
                        insertionLeft = false;
                    }
                 }
                if (insertionLeft) {
                    updatePreparedStatementExperiment.executeBatch();
                    deletePreparedStatementProject.executeBatch();
                    insertPreparedStatementProject.executeBatch();
                    deletePreparedStatementGroup.executeBatch();
                    insertPreparedStatementGroup.executeBatch();
                    deletePreparedStatementContact.executeBatch();
                    insertPreparedStatementContact.executeBatch();
                }
                
                connection.commit(); //Envoi des données à la BD
                
                insertStatusList.add(new Status("Data updated", StatusCodeMsg.INFO, "experiments updated"));
                results = new POSTResultsReturn(true, true, allExperimentsAlreadyInDB);
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);

                //Rollback
                if (connection != null) {
                    connection.rollback();
                }

                results = new POSTResultsReturn(false, true, allExperimentsAlreadyInDB);
                insertStatusList.add(new Status("Error", StatusCodeMsg.POSTGRESQL_ERROR, e.getMessage()));
                if (e.getNextException() != null) {
                    insertStatusList.add(new Status("Error", StatusCodeMsg.POSTGRESQL_ERROR, e.getNextException().getMessage()));
                }
                results.statusList = insertStatusList;
            } finally {
                if (updatePreparedStatementExperiment != null) {
                    updatePreparedStatementExperiment.close();
                }
                if (deletePreparedStatementProject != null) {
                    deletePreparedStatementProject.close();
                }
                if (insertPreparedStatementProject != null) {
                    insertPreparedStatementProject.close();
                }
                if (deletePreparedStatementGroup != null) {
                    deletePreparedStatementGroup.close();
                }
                if (insertPreparedStatementGroup != null) {
                    insertPreparedStatementGroup.close();
                }
                if (deletePreparedStatementContact != null) {
                    deletePreparedStatementContact.close();
                }
                if (insertPreparedStatementContact != null) {
                    insertPreparedStatementContact.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            results = new POSTResultsReturn(true, true, allExperimentsAlreadyInDB);
            results.statusList = insertStatusList;
        }
        return results;
    }

    @Override
    public POSTResultsReturn checkAndUpdateList(List<ExperimentDTO> newObjects) {
        POSTResultsReturn postResult;
        try {
            postResult = this.checkAndUpdateExperimentList(newObjects);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            postResult = new POSTResultsReturn(false, Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
        return postResult;
    }
}
