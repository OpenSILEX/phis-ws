//******************************************************************************
//                                       StructureDAOSesame.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 5 sept. 2018
// Contact: vincent.migot@inra.fr anne.tireau@inra.fr, pascal.neveu@inra.fr
// Subject: access to infrastructures in the triplestore
//******************************************************************************
package phis2ws.service.dao.sesame;

import java.util.ArrayList;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.dao.manager.DAOSesame;
import phis2ws.service.ontologies.Rdf;
import phis2ws.service.ontologies.Rdfs;
import phis2ws.service.ontologies.Vocabulary;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.view.model.phis.Infrastructure;

/**
 * Represents an infrastructure model
 * @author Vincent Migot <vincent.migot@inra.fr>
 */
public class InfrastructureDAOSesame extends DAOSesame<Infrastructure> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(InfrastructureDAOSesame.class);

    //The following attributes are used to search infrastructures in the triplestore
    //uri of the infrastructure
    public String uri;
    
    //type uri of the infrastructure(s)
    public String rdfType;
    
    //alias of the infrastructure(s)
    public String label;

     /**
     * generates a paginated search query (search by uri, type, label)
     * SELECT  ?uri ?rdfType  ?label 
     * WHERE {
     *   ?uri  ?0  ?rdfType  . 
     *   ?rdfType  rdfs:subClassOf*  <http://www.phenome-fppn.fr/vocabulary/2017#Infrastructure> . 
     *   OPTIONAL {
     *     ?uri rdfs:label ?label . 
     *   }
     * } 
     * @return the query to execute.
     */
    @Override
    protected SPARQLQueryBuilder prepareSearchQuery() {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();

        String infrastructureUri;
        if (uri != null) {
            infrastructureUri = "<" + uri + ">";
        } else {
            infrastructureUri = "?" + URI;
            query.appendSelect(infrastructureUri);
        }
        
        if (rdfType != null) {
            query.appendTriplet(infrastructureUri, Rdf.RELATION_TYPE.toString(), rdfType, null);
        } else {
            query.appendSelect("?" + RDF_TYPE);
            query.appendTriplet(infrastructureUri, Rdf.RELATION_TYPE.toString(), "?" + RDF_TYPE, null);
            query.appendTriplet("?" + RDF_TYPE, "<" + Rdfs.RELATION_SUBCLASS_OF.toString() + ">*", Vocabulary.CONCEPT_INFRASTRUCTURE.toString(), null);
        }        

        query.appendSelect(" ?" + LABEL);
        query.beginBodyOptional();
        query.appendToBody(infrastructureUri + " <" + Rdfs.RELATION_LABEL.toString() + "> " + "?" + LABEL + " . ");
        query.endBodyOptional();
            
        if (label != null) {
            query.appendFilter("REGEX ( ?" + LABEL + ",\".*" + label + ".*\",\"i\")");
        } 
        
        query.appendLimit(this.getPageSize());
        query.appendOffset(this.getPage()* this.getPageSize());
        LOGGER.debug(SPARQL_SELECT_QUERY + query.toString());
        return query;
    }

    /**
     * Get count of elements matching current prepared query
     * @return query total result count
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
     * Return prepared count query based on the current search query
     * SELECT (COUNT(DISTINCT ?uri) as ?count)
     * WHERE {
     *   ?uri  ?0  ?rdfType  . 
     *   ?rdfType  rdfs:subClassOf*  <http://www.phenome-fppn.fr/vocabulary/2017#Infrastructure> . 
     *   OPTIONAL {
     *     ?uri rdfs:label ?label . 
     *   }
     * } 
     * @return query
     */
    private SPARQLQueryBuilder prepareCount() {
        SPARQLQueryBuilder query = this.prepareSearchQuery();
        query.clearSelect();
        query.clearLimit();
        query.clearOffset();
        query.clearGroupBy();
        query.appendSelect("(COUNT(DISTINCT ?" + URI + ") as ?" + COUNT_ELEMENT_QUERY + ")");
        LOGGER.debug(SPARQL_SELECT_QUERY + " " + query.toString());
        return query;
    }

    /**
     * Search all the infrastructures corresponding to the search params given by the user
     * @return list of infrastructures which match given search params.
     */
    public ArrayList<Infrastructure> allPaginate() {
        SPARQLQueryBuilder query = prepareSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Infrastructure> infrastructures = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Infrastructure infrastructure = getInfrastructureFromBindingSet(bindingSet);
                infrastructures.add(infrastructure);
            }
        }
        return infrastructures;
    }

    
    /**
     * Get an infrastructure from a given binding set.
     * Assume that the following attributes exist :
     * uri, rdfType, label
     * @param bindingSet a bindingSet from a search query
     * @return a infrastructure with data extracted from the given bindingSet
     */
    private Infrastructure getInfrastructureFromBindingSet(BindingSet bindingSet) {
        Infrastructure infrastructure = new Infrastructure();

        if (uri != null) {
            infrastructure.setUri(uri);
        } else {
            infrastructure.setUri(bindingSet.getValue(URI).stringValue());
        }

        if (rdfType != null) {
            infrastructure.setRdfType(rdfType);
        } else {
            infrastructure.setRdfType(bindingSet.getValue(RDF_TYPE).stringValue());
        }

        infrastructure.setLabel(bindingSet.getValue(LABEL).stringValue());

        return infrastructure;
    }
}
