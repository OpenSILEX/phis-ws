//**********************************************************************************************
//                                       DAOSesame.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 2016
// Subject:This abstract class is the base of all Dao class for the Sesame TripleStore 
//***********************************************************************************************
package phis2ws.service.dao.manager;

import java.util.Map;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.PropertiesFileManager;
import phis2ws.service.authentication.TokenManager;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.model.User;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.ResponseFormPOST;

/**
 * Répresente une définition de la classe DAO permettant de se connecter au
 * TripleStore Sesame
 *
 * @author Arnaud Charleroy
 * @param <T>
 */
public abstract class DAOSesame<T> {

    final static Logger LOGGER = LoggerFactory.getLogger(DAOSesame.class);
    protected static final String PROPERTY_FILENAME = "sesame_rdf_config";
    //SILEX:test
    // Pour le soucis de pool de connexion plein
    protected static final String SESAME_SERVER = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
    protected static final String REPOSITORY_ID = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "repositoryID");
    //\SILEX:test

    //used for logger
    protected static final String SPARQL_SELECT_QUERY = "SPARQL query : ";

    protected static Repository rep;
    private RepositoryConnection connection;

    protected static String resourceType;

    public User user;
    protected Integer page;
    protected Integer pageSize;
    /**
     * User ip adress
     */
    public String remoteUserAdress;

    public DAOSesame() {
        try {
            String sesameServer = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
            String repositoryID = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "repositoryID");
            rep = new HTTPRepository(sesameServer, repositoryID); //Stockage triplestore Sesame
            rep.initialize();
            setConnection(rep.getConnection());
        } catch (Exception e) {
            ResponseFormPOST postForm = new ResponseFormPOST(new Status("Can't connect to triplestore", StatusCodeMsg.ERR, e.getMessage()));
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(postForm).build());
        }
    }

    public DAOSesame(String repositoryID) {
        try {
            String sesameServer = PropertiesFileManager.getConfigFileProperty(PROPERTY_FILENAME, "sesameServer");
            rep = new HTTPRepository(sesameServer, repositoryID); //Stockage triplestore Sesame
            rep.initialize();
            setConnection(rep.getConnection());
        } catch (Exception e) {
            ResponseFormPOST postForm = new ResponseFormPOST(new Status("Can't connect to triplestore", StatusCodeMsg.ERR, e.getMessage()));
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(postForm).build());
        }
    }

    public RepositoryConnection getConnection() {
        return connection;
    }

    public final void setConnection(RepositoryConnection connection) {
        this.connection = connection;
    }

    public static Repository getRepository() {
        return rep;
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
     * @return
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return Integer.valueOf(DefaultBrapiPaginationValues.PAGE_SIZE);
        }
        return pageSize;
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
     * Méthode de test d'existence d'un sujet par triplet
     *
     * @param subject
     * @param predicate
     * @param object
     * @return boolean
     */
    public boolean exist(String subject, String predicate, String object) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        boolean exist = false;
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect(null);
        query.appendTriplet(subject, predicate, object, null);
        query.appendParameters("LIMIT 1");
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                exist = true;
            }
        }
//        LOGGER.trace(query.toString());
        return exist;
    }

    /**
     *
     * @param objectURI l'uri de l'objet recherché
     * @return true si l'objet est dans le triplestore, false sinon
     */
    public boolean existObject(String objectURI) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendSelect("?p");
        query.appendTriplet(objectURI, "?p", "?o", null);
        query.appendParameters("LIMIT 1");
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            if (result.hasNext()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Méthode de récupération d'élement d'existence par triplet
     *
     * @param subject
     * @param predicate
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public String getValueFromPredicate(String subject, String predicate) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String value = null;
        if (subject != null || predicate != null) {
            SPARQLQueryBuilder query = new SPARQLQueryBuilder();
            query.appendSelect("?x");
            query.appendTriplet(subject, predicate, "?x", null);
            query.appendParameters("LIMIT 1");
            LOGGER.trace(query.toString());
            TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    value = result.next().getBinding("x").getValue().stringValue();
                }
            }
            LOGGER.trace(value);
        }
        return value;
    }

    /**
     * Fonction qui permet de créer la partie commune d'une requête à la fois
     * pour lister les éléments et les récupérés
     *
     * @return SPARQLQueryBuilder
     */
    abstract protected SPARQLQueryBuilder prepareSearchQuery();

    /**
     * Compte le nombre d'élement retournés par la requête
     *
     * @return Integer
     */
    public abstract Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException;

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
     * Définit un objet utilisateur à partir d'un identifiant
     *
     * @param id identifiant
     */
    public void setUser(String id) {
//        LOGGER.debug(JsonConverter.ConvertToJson(TokenManager.Instance().getSession(id).getUser()));
        if (TokenManager.Instance().getSession(id).getUser() == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            this.user = TokenManager.Instance().getSession(id).getUser();
        }
    }

    /**
     * Fill a map with labels from binding set value
     *
     * @param labelMap this map will fill with the label
     * @param labelValue label binding set value
     */
    protected void retreiveBindingValueLabelInMap(Map labelMap, Value labelValue) {
        if (labelValue != null) {
            // if label is a literal we will be able to retreive this langage programmatically
            if (labelValue instanceof Literal) {
                Literal literal = (Literal) labelValue;
                Optional<String> propertyLanguage = literal.getLanguage();
                if (propertyLanguage.isPresent()) {
                    labelMap.put(propertyLanguage.get(), labelValue.stringValue());
                } else {
                    labelMap.put("default", labelValue.stringValue());
                }
            } else {
                labelMap.put("unknown lang", labelValue.stringValue());
            }
        }
    }
}
