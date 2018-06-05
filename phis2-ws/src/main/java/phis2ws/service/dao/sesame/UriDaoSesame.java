//**********************************************************************************************
//                                       UriDaoSesame.java 
//
// Author(s): Eloan LAGIER, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: Feb 26 2018
// Contact: eloan.lagier@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  Feb 26, 2018
// Subject: A Dao specific to insert Uri into the triplestore
//***********************************************************************************************
package phis2ws.service.dao.sesame;

import java.util.ArrayList;
import java.util.Optional;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.configuration.URINamespaces;
import phis2ws.service.dao.manager.DAOSesame;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.view.model.phis.Ask;
import phis2ws.service.view.model.phis.Uri;

/**
 * Represents the Triplestore Data Access Object for the uris
 * @author Eloan LAGIER
 */
public class UriDaoSesame extends DAOSesame<Uri> {

    public String uri;
    public String label;
    
    final static String TRIPLESTORE_FIELDS_TYPE = "type";
    final static String TRIPLESTORE_FIELDS_CLASS = "class";
    final static String TRIPLESTORE_FIELDS_INSTANCE = "instance";
    final static String TRIPLESTORE_FIELDS_SUBCLASS = "subclass";    
    
    final static Logger LOGGER = LoggerFactory.getLogger(UriDaoSesame.class);
    public Boolean deep;

    URINamespaces uriNameSpace = new URINamespaces();
    
    /**
     * prepare a query to get the triplets of an uri (given or not).
     * @return the query 
     * e.g.
     * SELECT DISTINCT  ?class ?type WHERE {
     * <http://www.phenome-fppn.fr/vocabulary/2017#Document>  ?class  ?type  . }
     */
    @Override
    protected SPARQLQueryBuilder prepareSearchQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }

        query.appendSelect(" ?class ?type");
        query.appendTriplet(contextURI, "?class", "?type", null);

        LOGGER.debug("sparql select query : " + query.toString());
        return query;
    }
    
    /**
     * Search uri with same label
     * @return the query
     * query example : 
     * SELECT ?class WHERE { ?class rdfs:label contextName }
     */
    protected SPARQLQueryBuilder prepareLabelSearchQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String selectLabel;

        if (label != null) {
            selectLabel = label;
        } else {
            selectLabel = " ?label ";
            query.appendSelect(" ?label ");

        }

        query.appendSelect(" ?class ");
        query.appendTriplet(" ?class ", uriNameSpace.getRelationsProperty("label"), selectLabel, null);

        LOGGER.debug(" sparql select query : " + query.toString());
        return query;
    }    
    
    /**
     * Search siblings of concept query example : SELECT DISTINCT ?class WHERE {
     * contextURI rdfs:subClassOf ?parent . ?class rdfs:subClassOf ?parent }
     *
     * @return SPARQLQueryBuilder
     */
    protected SPARQLQueryBuilder prepareSiblingsQuery() {
        //SILEX:warning
        //Siblings take ScientificDocument for exemple but it's different that all the other concept GET
        //where could this can be change?
        //\SILEX:warning
        
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        query.appendSelect(" ?class ");
        query.appendTriplet(contextURI,uriNameSpace.getRelationsProperty("subClassOf"), " ?parent ", null);
        query.appendTriplet("?class",uriNameSpace.getRelationsProperty("subClassOf"), "?parent", null);
        LOGGER.debug(query.toString());
        return query;
    }

    @Override
    public Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Ask if an Uri is in the triplestore
     * @return the ask query
     * query exemple : ASK { concept ?any1 ?any2 .}
     */
    protected SPARQLQueryBuilder prepareAskQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        
        query.appendAsk(contextURI + " ?any1 ?any2 "); //any = anything
        LOGGER.debug(query.toString());
        return query;
    }
    
    /**
     * check if the given uris exists in the triplestore and return the results
     * @return a boolean saying if the uri exist
     */
    public ArrayList<Ask> askUriExistance() {
        SPARQLQueryBuilder query = prepareAskQuery();
        BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Ask> uriExistancesResults = new ArrayList<>();
        boolean result = booleanQuery.evaluate();
        Ask ask = new Ask();
        ask.setExist(result);

        uriExistancesResults.add(ask);

        return uriExistancesResults;
    }
    
    /**
     * Search instances by uri, concept 
     * @return SPARQLQueryBuilder
     * query example : 
     * SELECT ?instance ?subclass 
     * WHERE {?subclass rdfs:subClassOf(*) context URI }
     */
    protected SPARQLQueryBuilder prepareInstanceSearchQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }

        query.appendSelect(" ?instance");
        query.appendSelect(" ?subclass");
        // if deep get descendents
        if (deep) {
            query.appendTriplet("?subclass", uriNameSpace.getRelationsProperty("subClassOf*"), contextURI, null);
        } else {
            query.appendTriplet("?subclass", uriNameSpace.getRelationsProperty("subClassOf"), contextURI, null);
        }
        query.appendTriplet("?instance", uriNameSpace.getRelationsProperty("type"), "?subclass", null);
        LOGGER.debug("sparql select query : " + query.toString());
        return query;
    }
    
   /**
     * Search ancestors of a concept 
     * @return SPARQLQueryBuilder
     * query example : 
     * SELECT DISTINCT ?class WHERE
     * { contextURI rdfs:subClassOf* ?class }
     */
    protected SPARQLQueryBuilder prepareAncestorsQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        query.appendSelect(" ?class ");
        query.appendTriplet(contextURI,uriNameSpace.getRelationsProperty("subClassOf"), " ?class ", null);
        LOGGER.debug(query.toString());
        return query;
    }
    
    /**
     * Search descendants of concept 
     * @return SPARQLQueryBuilder
     * query example : 
     * SELECT DISTINCT ?class
     * WHERE { ?class rdfs:subClassOf* contextURI }
     */
    protected SPARQLQueryBuilder prepareDescendantsQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        query.appendSelect(" ?class ");
        query.appendTriplet(" ?class ",uriNameSpace.getRelationsProperty("subClassOf*"), contextURI, null);
        LOGGER.debug(query.toString());

        return query;
    }
    
    
    /**
     * return the type of the uri given
     * @return SPARQLQueryBuilder
     * query example : 
     * SELECT DISTINCT ?type WHERE { concept rdf:type ?type . }
     */
    /* create the query that return the type of an URI if its in the Tupple */
    protected SPARQLQueryBuilder prepareGetUriType() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        query.appendSelect(" ?type ");
        query.appendTriplet(contextURI, uriNameSpace.getRelationsProperty("type"), " ?type ", null);
        LOGGER.debug(query.toString());
        return query;
    }
    
    /**
     * return all metadata for the uri given
     * @return the list of the uris corresponding to the search informations
     */
    public ArrayList<Uri> allPaginate() {
        SPARQLQueryBuilder query = prepareSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> uris = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                Uri uriFounded = new Uri();
                BindingSet bindingSet = result.next();
                uriFounded.setUri(this.uri);
                String classname = bindingSet.getValue(TRIPLESTORE_FIELDS_CLASS).stringValue();
                Value propertyType = bindingSet.getValue(TRIPLESTORE_FIELDS_TYPE);
                //if its a litteral we look what's the language
                if (propertyType instanceof Literal) {
                    Literal literal = (Literal) bindingSet.getValue(TRIPLESTORE_FIELDS_TYPE);
                    Optional<String> propertyLanguage = literal.getLanguage();
                    uriFounded.addAnnotation(classname.substring(classname.indexOf("#") + 1, classname.length()) + "_" + propertyLanguage.get(), bindingSet.getValue(TRIPLESTORE_FIELDS_TYPE).stringValue());
                } else {
                    uriFounded.addProperty(classname.substring(classname.indexOf("#") + 1, classname.length()), bindingSet.getValue(TRIPLESTORE_FIELDS_TYPE).stringValue());
                }
                 uris.add(uriFounded);
            }           
        }
        return uris;
    }
    
    /**
     * search the uris which has the given label as label and return the list 
     * @return ArrayList
     */
    public ArrayList<Uri> labelsPaginate() {
        SPARQLQueryBuilder query = prepareLabelSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> uris = new ArrayList();
        
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                Uri uriFounded = new Uri();
                BindingSet bindingSet = result.next();
                uriFounded.setUri(bindingSet.getValue(TRIPLESTORE_FIELDS_CLASS).toString());
                uris.add(uriFounded);
            }
        }
        return uris;
    }
    
    /**
     * @return the list of the instances, corresponding to the search params given
     */
    public ArrayList<Uri> instancesPaginate() {

        SPARQLQueryBuilder query = prepareInstanceSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());

        ArrayList<Uri> instances = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            while (result.hasNext()) {
                BindingSet bindingSet = result.next();

                Uri instance = new Uri();

                instance.setUri(bindingSet.getValue(TRIPLESTORE_FIELDS_INSTANCE).stringValue());
                instance.setRdfType(bindingSet.getValue(TRIPLESTORE_FIELDS_SUBCLASS).stringValue());

                instances.add(instance);
            }
        }

        return instances;
    }

   /**
     * call the query function for the ancestors GET
     * @return the ancestors info all paginate
     */
    public ArrayList<Uri> ancestorsAllPaginate() {

        SPARQLQueryBuilder query = prepareAncestorsQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> concepts = new ArrayList();

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            while (result.hasNext()) {
                Uri concept = new Uri();
                BindingSet bindingSet = result.next();
                concept.setUri(bindingSet.getValue(TRIPLESTORE_FIELDS_CLASS).stringValue());
                concepts.add(concept);
            }

        }
        return concepts;
    }
    
    /**
     * call the query function for the siblings GET
     * @return the siblings info all paginate
     */
    public ArrayList<Uri> siblingsAllPaginate() {

        SPARQLQueryBuilder query = prepareSiblingsQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> concepts = new ArrayList();

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            while (result.hasNext()) {
                Uri concept = new Uri();
                BindingSet bindingSet = result.next();
                concept.setUri(bindingSet.getValue(TRIPLESTORE_FIELDS_CLASS).stringValue());
                concepts.add(concept);
            }
        }
        return concepts;
    }
    
    /**
     * call the query function for the descendants GET
     * @return the descendants info all paginate
     */
    public ArrayList<Uri> descendantsAllPaginate() {

        SPARQLQueryBuilder query = prepareDescendantsQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> concepts = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            while (result.hasNext()) {
                Uri concept = new Uri();
                BindingSet bindingSet = result.next();
                concept.setUri(bindingSet.getValue(TRIPLESTORE_FIELDS_CLASS).stringValue());
                concepts.add(concept);
            }
        }
        return concepts;
    }
    
    /**
     * return the type of the uri if it's in the triplestore
     * @return a boolean or a type
     */
    public ArrayList<Uri> getAskTypeAnswer() {
        
        SPARQLQueryBuilder query = prepareAskQuery();
        BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Uri> uris = new ArrayList<>();
        boolean result = booleanQuery.evaluate();
        Ask ask = new Ask();
        ask.setExist(result);
        
        if (ask.getExist()) {
            Uri uriType = new Uri();
            query = prepareGetUriType();
            TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            TupleQueryResult resultat = tupleQuery.evaluate();
            BindingSet bindingSet = resultat.next();
            uriType.setRdfType(bindingSet.getValue(TRIPLESTORE_FIELDS_TYPE).toString());
            uris.add(uriType);
        }
        
        return uris;
    }
    
    /**
     * generates an ask query to know if the given rdfSubType is a subclass of 
     * rdfType. 
     * @param rdfSubType
     * @param rdfType
     * @return the query. 
     * e.g.
     * ASK {
     *	<http://www.phenome-fppn.fr/vocabulary/2017#HemisphericalCamera> rdfs:subClassOf* <http://www.phenome-fppn.fr/vocabulary/2017#SensingDevice>
     *  }
     */
    private SPARQLQueryBuilder prepareIsSubclassOf(String rdfSubType, String rdfType) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);

        String contextURI;

        if (uri != null) {
            contextURI = "<" + uri + ">";
        } else {
            contextURI = "?uri";
            query.appendSelect("?uri");
        }
        
        query.appendTriplet("<" + rdfSubType + ">", uriNameSpace.getRelationsProperty("subClassOf*"), "<" + rdfType + ">", null);
        
        query.appendAsk(""); //any = anything
        LOGGER.debug(query.toString());
        return query;
    }
    
    /**
     * check if the given rdfSubType is a sub class of the given rdfType
     * @param rdfSubType
     * @param rdfType
     * @return true if it is a subclass
     *         false if not
     */
    public boolean isSubClassOf(String rdfSubType, String rdfType) {
        SPARQLQueryBuilder query = prepareIsSubclassOf(rdfSubType, rdfType);

        BooleanQuery booleanQuery = getConnection().prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
        return booleanQuery.evaluate();
    }
}
