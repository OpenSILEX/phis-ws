//**********************************************************************************************
//                                       DAOMongo.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 2016
// Subject:This abstract class is the base of all Dao class for the Mongo DB 
//***********************************************************************************************
package phis2ws.service.dao.manager;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import java.util.ArrayList;
import org.bson.Document;
import phis2ws.service.PropertiesFileManager;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.model.User;

/**
 * Répresente une définition de la classe DAO permettant de se connecter à la
 * source de données MongoDB
 *
 * @author Arnaud Charleroy
 * @param <T>
 */
public abstract class DAOMongo<T> {

    /**
     * @see service.properties file
     */
    private final static MongoClient MONGO_CLIENT = new MongoClient(
            new MongoClientURI(PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "url")));
    protected GridFS gridFS = new GridFS(MONGO_CLIENT.getDB(PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "db")));
    protected MongoDatabase database;
    protected MongoCollection<Document> collection;

    public User user;
    protected Integer page;
    protected Integer pageSize;
    /**
     * User ip adress
     */
    protected String remoteUserAdress;
    
    //The _id json data key in the mongodb documents
    public final static String DB_FIELD_ID = "_id";
    //The $gte mongo key
    public final static String MONGO_GTE = "$gte";
    //the $lte mongo key
    public final static String MONGO_LTE = "$lte";
    //the $elemMatch mongo key
    public final static String MONGO_ELEM_MATCH = "$elemMatch";
    //the $and mongo key
    public final static String MONGO_AND = "$and";

    /**
     * @see service.properties file
     */
    public DAOMongo() {
        this.setDatabase(MONGO_CLIENT.getDatabase(PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "db")));
    }

    public static MongoClient getMongoClient() {
        return MONGO_CLIENT;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    /**
     * La page de l'api brapi commence à 0
     *
     * @return numéro de la page courante
     */
    public Integer getPage() {
        if (page == null || pageSize < 0) {
            return 0;
        }
        return page;
    }

    /**
     * La page de l'api brapi pour pouvoir l'utiliser pour la pagination dans
     * une base de données
     *
     * @return numéro de la page courante + 1
     */
    public Integer getPageForDBQuery() {
        if (page == null || pageSize < 0) {
            return 1;
        }
        return page + 1;
    }

    /**
     * Définit le paramètre page
     *
     * @param page
     */
    public void setPage(Integer page) {
        if (page < 0) {
            this.page = Integer.valueOf(DefaultBrapiPaginationValues.PAGE);
        }
        this.page = page;
    }

    /**
     * Retourne le paramètre taille de la page
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return Integer.valueOf(DefaultBrapiPaginationValues.PAGE_SIZE);
        }
        return pageSize;
    }

    /**
     * 
     * @return Les logs qui seront utilisés pour la traçabilité
     */
    protected String getTraceabilityLogs() {
        String log = "";
        if (remoteUserAdress != null) {
            log += "IP Address " + remoteUserAdress + " - ";
        }
        if (user != null) {
            log += "User : " + user.getEmail() + " - ";
        }
        
        return log;
    }
    
    /**
     * Définit le paramètre taille de page
     *
     * @param pageSize
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Fonction qui permet de créer la partie commune d'une requête à la fois
     * pour lister les éléments et les récupérés
     *
     * @return BasicDBObject
     */
    abstract protected BasicDBObject prepareSearchQuery();
    
     /**
     * Retourne les élements retournés par la requête en prenant en compte la pagination de l'utilisateur
     * @return 
     */
    public abstract ArrayList<T> allPaginate();
}
