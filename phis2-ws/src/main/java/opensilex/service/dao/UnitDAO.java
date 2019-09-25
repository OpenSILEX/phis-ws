//******************************************************************************
//                                UnitDAO.java 
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: 18 Nov. 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import static opensilex.service.dao.VariableDAO.OBJECT;
import static opensilex.service.dao.VariableDAO.PROPERTY;
import static opensilex.service.dao.VariableDAO.SEE_ALSO;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.dao.manager.Rdf4jDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.ontology.Contexts;
import opensilex.service.ontology.Rdf;
import opensilex.service.ontology.Rdfs;
import opensilex.service.ontology.Skos;
import opensilex.service.ontology.Oeso;
import opensilex.service.resource.dto.UnitDTO;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.utils.UriGenerator;
import opensilex.service.utils.sparql.SPARQLQueryBuilder;
import opensilex.service.view.brapi.Status;
import opensilex.service.model.OntologyReference;
import opensilex.service.model.Unit;
import static org.apache.jena.arq.querybuilder.AbstractQueryBuilder.makeVar;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.rdf4j.model.Value;

/**
 * Unit DAO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 * @update [Vincent Migot] 17 July 2019: Update getLastId method to fix bug and limitation in URI generation
 */
public class UnitDAO extends Rdf4jDAO<Unit> {
    final static Logger LOGGER = LoggerFactory.getLogger(UnitDAO.class);
    
    public String uri;
    public String label;
    public String comment;
    public ArrayList<OntologyReference> ontologiesReferences = new ArrayList<>();

    private static final String MAX_ID = "maxID";
        
    protected SPARQLQueryBuilder prepareSearchQuery() {
        //SILEX:todo
        // Add search by ontology references
        //\SILEX:todo
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);
        query.appendGraph(Contexts.VARIABLES.toString());
        String unitUri;
        
        if (uri != null) {
            unitUri = "<" + uri + ">";
        } else {
            unitUri = "?uri";
            query.appendSelect("?uri");
        }
        query.appendTriplet(unitUri, Rdf.RELATION_TYPE.toString(), Oeso.CONCEPT_UNIT.toString(), null);
        
        if (label != null) {
            query.appendTriplet(unitUri, Rdfs.RELATION_LABEL.toString(),"\"" + label + "\"", null);
        } else {
            query.appendSelect(" ?label");
            query.appendTriplet(unitUri, Rdfs.RELATION_LABEL.toString(), "?label", null);
        }
        
        if (comment != null) {
            query.appendTriplet(unitUri, Rdfs.RELATION_COMMENT.toString(), "\"" + comment + "\"", null);
        } else {
            query.appendSelect(" ?" + COMMENT);
            query.beginBodyOptional();
            query.appendToBody(unitUri + " <" + Rdfs.RELATION_COMMENT.toString() + "> " + "?" + COMMENT + " . ");
            query.endBodyOptional();
        }
        
        LOGGER.debug(SPARQL_QUERY + query.toString());
        return query;
    }
    
    /**
     * Check if the objects are valid.
     * @param unitsDTO
     * @return 
     */
    public POSTResultsReturn check(List<UnitDTO> unitsDTO) {
        //Résultats attendus
        POSTResultsReturn traitsCheck;
        //Liste des status retournés
        List<Status> checkStatusList = new ArrayList<>();
        boolean dataOk = true;
        
        //Vérification des unités
        for (UnitDTO unitDTO : unitsDTO) {
            //Vérification des relations d'ontologies de référence
            for (OntologyReference ontologyReference : unitDTO.getOntologiesReferences()) {
                if (!ontologyReference.getProperty().equals(Skos.RELATION_EXACT_MATCH.toString())
                   && !ontologyReference.getProperty().equals(Skos.RELATION_CLOSE_MATCH.toString())
                   && !ontologyReference.getProperty().equals(Skos.RELATION_NARROWER.toString())
                   && !ontologyReference.getProperty().equals(Skos.RELATION_BROADER.toString())) {
                    dataOk = false;
                    checkStatusList.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, 
                            "Bad property relation given. Must be one of the following : " 
                            + Skos.RELATION_EXACT_MATCH.toString()
                            + ", " + Skos.RELATION_CLOSE_MATCH.toString()
                            + ", " + Skos.RELATION_NARROWER.toString()
                            + ", " + Skos.RELATION_BROADER.toString()
                            +". Given : " + ontologyReference.getProperty()));
                }
            }
        }
        
        traitsCheck = new POSTResultsReturn(dataOk, null, dataOk);
        traitsCheck.statusList = checkStatusList;
        return traitsCheck;
    }
    
    /**
     * Prepares a query to get the higher id of the units.
     * @example
     * <pre>
     * SELECT ?maxID WHERE {
     *   ?uri a <http://www.opensilex.org/vocabulary/oeso#Unit>
     *   BIND(xsd:integer>(strafter(str(?uri), "http://www.opensilex.org/diaphen/id/units/u")) AS ?maxID)
     * }
     * ORDER BY DESC(?maxID)
     * LIMIT 1
     * </pre>
     * @return 
     */
    private Query prepareGetLastId() {
        SelectBuilder query = new SelectBuilder();
        
        Var uri = makeVar(URI);
        Var maxID = makeVar(MAX_ID);
        
        // Select the highest identifier
        query.addVar(maxID);
        
        // Filter by unit
        Node methodConcept = NodeFactory.createURI(Oeso.CONCEPT_UNIT.toString());
        query.addWhere(uri, RDF.type, methodConcept);
        
        // Binding to extract the last part of the URI as a MAX_ID integer
        ExprFactory expr = new ExprFactory();
        Expr indexBinding =  expr.function(
            XSD.integer.getURI(), 
            ExprList.create(Arrays.asList(
                expr.strafter(expr.str(uri), UriGenerator.PLATFORM_URI_ID_UNITS))
            )
        );
        query.addBind(indexBinding, maxID);
        
        // Order MAX_ID integer from highest to lowest and select the first value
        query.addOrderBy(new SortCondition(maxID,  Query.ORDER_DESCENDING));
        query.setLimit(1);
        
        return query.build();
    }
    
    /**
     * Gets the higher id of the units.
     * @return the id
     */
    public int getLastId() {
        Query query = prepareGetLastId();

        //get last unit uri ID inserted
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        TupleQueryResult result = tupleQuery.evaluate();

        if (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value maxId = bindingSet.getValue(MAX_ID);
            if (maxId != null) {
                return Integer.valueOf(maxId.stringValue());
            }
        } 
        
        return 0;
    }
    
    /**
     * Prepares an update query for a unit.
     * @param unitDTO
     * @return update request
     */
    private UpdateRequest prepareInsertQuery(UnitDTO unitDTO) {
        UpdateBuilder spql = new UpdateBuilder();
        
        Node graph = NodeFactory.createURI(Contexts.VARIABLES.toString());
        
        Node unitConcept = NodeFactory.createURI(Oeso.CONCEPT_UNIT.toString());
        Resource unitUri = ResourceFactory.createResource(unitDTO.getUri());

        spql.addInsert(graph, unitUri, RDF.type, unitConcept);
        spql.addInsert(graph, unitUri, RDFS.label, unitDTO.getLabel());
        
        if (unitDTO.getComment() != null) {
            spql.addInsert(graph, unitUri, RDFS.comment, unitDTO.getComment());
        }
        
        unitDTO.getOntologiesReferences().forEach((ontologyReference) -> {
            Property ontologyProperty = ResourceFactory.createProperty(ontologyReference.getProperty());
            Node ontologyObject = NodeFactory.createURI(ontologyReference.getObject());
            spql.addInsert(graph, unitUri, ontologyProperty, ontologyObject);
            Literal seeAlso = ResourceFactory.createStringLiteral(ontologyReference.getSeeAlso());
            spql.addInsert(graph, ontologyObject, RDFS.seeAlso, seeAlso);
        });
        
        return spql.buildRequest();
    }
    
    /**
     * Create objects. 
     * The objects integrity must have been checked previously.
     * @param unitsDTO
     * @return 
     */
    public POSTResultsReturn insert(List<UnitDTO> unitsDTO) {
        List<Status> insertStatusList = new ArrayList<>();
        List<String> createdResourcesURI = new ArrayList<>();
        
        POSTResultsReturn results;
        boolean resultState = false;
        boolean annotationInsert = true;
        
        final Iterator<UnitDTO> iteratorUnitDTO = unitsDTO.iterator();
        
        while (iteratorUnitDTO.hasNext() && annotationInsert) {
            UnitDTO unitDTO = iteratorUnitDTO.next();
            try {
                unitDTO.setUri(UriGenerator.generateNewInstanceUri(Oeso.CONCEPT_UNIT.toString(), null, null));
            } catch (Exception ex) { //In the unit case, no exception should be raised
                annotationInsert = false;
            }
            
            // Register
            UpdateRequest spqlInsert = prepareInsertQuery(unitDTO);
            
            try {
                //SILEX:todo
                // Connection to review. Dirty hotfix.
                this.getConnection().begin();
                Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, spqlInsert.toString());
                LOGGER.trace(getTraceabilityLogs() + " query : " + prepareUpdate.toString());
                prepareUpdate.execute();
                //\SILEX:todo

                createdResourcesURI.add(unitDTO.getUri());

                if (annotationInsert) {
                    resultState = true;
                    getConnection().commit();
                } else {
                    getConnection().rollback();
                }
            } catch (RepositoryException ex) {
                    LOGGER.error("Error during commit or rolleback Triplestore statements: ", ex);
            } catch (MalformedQueryException e) {
                    LOGGER.error(e.getMessage(), e);
                    annotationInsert = false;
                    insertStatusList.add(new Status(
                            StatusCodeMsg.QUERY_ERROR, 
                            StatusCodeMsg.ERR, 
                            "Malformed insertion query: " + e.getMessage()));
            } 
        }
        
        results = new POSTResultsReturn(resultState, annotationInsert, true);
        results.statusList = insertStatusList;
        results.setCreatedResources(createdResourcesURI);
        if (resultState && !createdResourcesURI.isEmpty()) {
            results.createdResources = createdResourcesURI;
            results.statusList.add(new Status(
                    StatusCodeMsg.RESOURCES_CREATED, 
                    StatusCodeMsg.INFO, 
                    createdResourcesURI.size() + " new resource(s) created."));
        }
        
        return results;
    }
    
    /**
     * Checks the integrity of the objects and create them in the storage.
     * @param unitsDTO
     * @return the result
     */
    public POSTResultsReturn checkAndInsert(List<UnitDTO> unitsDTO) {
        POSTResultsReturn checkResult = check(unitsDTO);
        if (checkResult.getDataState()) {
            return insert(unitsDTO);
        } else {
            return checkResult;
        }
    }
    
    /**
     * @param uri
     * @return the ontology references links
     */
    private SPARQLQueryBuilder prepareSearchOntologiesReferencesQuery(String uri) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        
        query.appendDistinct(Boolean.TRUE);
        query.appendGraph(Contexts.VARIABLES.toString());
        
        if (ontologiesReferences.isEmpty()) {
            query.appendSelect(" ?property ?object ?seeAlso");
            query.appendTriplet(uri, "?property", "?object", null);
            query.appendOptional("{?object <" + Rdfs.RELATION_SEE_ALSO.toString() + "> ?seeAlso}");
            query.appendFilter("?property IN(<" + Skos.RELATION_CLOSE_MATCH.toString() + ">, <"
                                               + Skos.RELATION_EXACT_MATCH.toString() + ">, <"
                                               + Skos.RELATION_NARROWER.toString() + ">, <"
                                               + Skos.RELATION_BROADER.toString() + ">)");
        } else {
            for (OntologyReference ontologyReference : ontologiesReferences) {
                query.appendTriplet(uri, ontologyReference.getProperty(), ontologyReference.getObject(), null);
                query.appendTriplet(
                        ontologyReference.getObject(), 
                        Rdfs.RELATION_SEE_ALSO.toString(), 
                        ontologyReference.getSeeAlso(), 
                        null);
            }
        }
        
        LOGGER.debug(SPARQL_QUERY + query.toString());
        return query;
    }
    
    /**
     * @return the units found
     */
    public ArrayList<Unit> allPaginate() {
        SPARQLQueryBuilder query = prepareSearchQuery();
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        ArrayList<Unit> units = new ArrayList<>();
        
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Unit unit = new Unit();
                
                if (uri != null) {
                    unit.setUri(uri);
                } else {
                    unit.setUri(bindingSet.getValue("uri").stringValue());
                }
                
                if (label != null) {
                    unit.setLabel(label);
                } else {
                    unit.setLabel(bindingSet.getValue("label").stringValue());
                }
                
                if (comment != null) {
                    unit.setComment(comment);
                } else if (bindingSet.getValue(COMMENT) != null) {
                    unit.setComment(bindingSet.getValue(COMMENT).stringValue());
                }
                
                // Get ontology references  
                SPARQLQueryBuilder queryOntologiesReferences = prepareSearchOntologiesReferencesQuery(unit.getUri());
                TupleQuery tupleQueryOntologiesReferences = this.getConnection()
                        .prepareTupleQuery(QueryLanguage.SPARQL, queryOntologiesReferences.toString());
                TupleQueryResult resultOntologiesReferences = tupleQueryOntologiesReferences.evaluate();
                while (resultOntologiesReferences.hasNext()) {
                    BindingSet bindingSetOntologiesReferences = resultOntologiesReferences.next();
                    if (bindingSetOntologiesReferences.getValue("object") != null
                            && bindingSetOntologiesReferences.getValue("property") != null) {
                        OntologyReference ontologyReference = new OntologyReference();
                        ontologyReference.setObject(bindingSetOntologiesReferences.getValue("object").toString());
                        ontologyReference.setProperty(bindingSetOntologiesReferences.getValue("property").toString());
                        if (bindingSetOntologiesReferences.getValue("seeAlso") != null) {
                            ontologyReference.setSeeAlso(bindingSetOntologiesReferences.getValue("seeAlso").toString());
                        }
                        
                        unit.addOntologyReference(ontologyReference);
                    }
                }
                units.add(unit);
            }
        }
        return units;
    }
    
    /**
     * Prepares delete request for a unit.
     * @param unit
     * @return delete request
     */
    private UpdateRequest prepareDeleteQuery(Unit unit){
        UpdateBuilder spql = new UpdateBuilder();
        
        Node graph = NodeFactory.createURI(Contexts.VARIABLES.toString());
        Resource unitUri = ResourceFactory.createResource(unit.getUri());
        
        spql.addDelete(graph, unitUri, RDFS.label, unit.getLabel());
        if (unit.getComment() != null) {
            spql.addDelete(graph, unitUri, RDFS.comment, unit.getComment());
        }
        
        unit.getOntologiesReferences().forEach((ontologyReference) -> {
            Property ontologyProperty = ResourceFactory.createProperty(ontologyReference.getProperty());
            Node ontologyObject = NodeFactory.createURI(ontologyReference.getObject());
            spql.addDelete(graph, unitUri, ontologyProperty, ontologyObject);
            if (ontologyReference.getSeeAlso() != null) {
                Literal seeAlso = ResourceFactory.createStringLiteral(ontologyReference.getSeeAlso());
                spql.addDelete(graph, ontologyObject, RDFS.seeAlso, seeAlso);
            }
        });
                
        return spql.buildRequest();        
    }
    
    private POSTResultsReturn updateAndReturnPOSTResultsReturn(List<UnitDTO> unitsDTO) {
        List<Status> updateStatusList = new ArrayList<>();
        List<String> updatedResourcesURIList = new ArrayList<>();
        POSTResultsReturn results;
        
        boolean annotationUpdate = true;
        boolean resultState = false;
        
        for (UnitDTO unitDTO : unitsDTO) {
            //1. Delete existing data
            //1.1 Get information to modify (to delete the right triplets)
            uri = unitDTO.getUri();
            ArrayList<Unit> unitsCorresponding = allPaginate();
            if (unitsCorresponding.size() > 0) {
                UpdateRequest deleteQuery = prepareDeleteQuery(unitsCorresponding.get(0));

                //2. Insert the new data
                UpdateRequest queryInsert = prepareInsertQuery(unitDTO);
                 try {
                        // transaction start: check connection
                        this.getConnection().begin();
                        Update prepareDelete = this.getConnection().prepareUpdate(deleteQuery.toString());
                        LOGGER.debug(getTraceabilityLogs() + " query : " + prepareDelete.toString());
                        prepareDelete.execute();
                        Update prepareUpdate = this.getConnection()
                                .prepareUpdate(QueryLanguage.SPARQL, queryInsert.toString());
                        LOGGER.debug(getTraceabilityLogs() + " query : " + prepareUpdate.toString());
                        prepareUpdate.execute();

                        updatedResourcesURIList.add(unitDTO.getUri());
                    } catch (MalformedQueryException e) {
                        LOGGER.error(e.getMessage(), e);
                        annotationUpdate = false;
                        updateStatusList.add(new Status(
                                StatusCodeMsg.QUERY_ERROR, 
                                StatusCodeMsg.ERR, 
                                "Malformed update query: " + e.getMessage()));
                    }   
            } else {
                annotationUpdate = false;
                updateStatusList.add(
                        new Status("Unknown instance", StatusCodeMsg.ERR, "Unknown unit " + unitDTO.getUri()));
            }
        }
        
        if (annotationUpdate) {
            resultState = true;
            try {
                this.getConnection().commit();
            } catch (RepositoryException ex) {
                LOGGER.error("Error during commit Triplestore statements: ", ex);
            }
        } else {
            // Rollback
            try {
                this.getConnection().rollback();
            } catch (RepositoryException ex) {
                LOGGER.error("Error during rollback Triplestore statements : ", ex);
            }
        }
        
        results = new POSTResultsReturn(resultState, annotationUpdate, true);
        results.statusList = updateStatusList;
        if (resultState && !updatedResourcesURIList.isEmpty()) {
            results.createdResources = updatedResourcesURIList;
            results.statusList.add(new Status(
                    StatusCodeMsg.RESOURCES_UPDATED, 
                    StatusCodeMsg.INFO, 
                    updatedResourcesURIList.size() + " resources updated"));
        }
        return results;
    }
    
    /**
     * Checks the objects integrity and updates them in the storage.
     * @param unitsDTO
     * @return the result of check and update
     */
    public POSTResultsReturn checkAndUpdate(List<UnitDTO> unitsDTO) {
        POSTResultsReturn checkResult = check(unitsDTO);
        if (checkResult.getDataState()) {
            return updateAndReturnPOSTResultsReturn(unitsDTO);
        } else { //Les données ne sont pas bonnes
            return checkResult;
        }
    }

    @Override
    public List<Unit> create(List<Unit> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<Unit> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Unit> update(List<Unit> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Unit find(Unit object) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Prepare query to get a unit by it's URI
     * @example
     * SELECT DISTINCT   ?label ?comment ?property ?object ?seeAlso WHERE {
     * GRAPH <http://www.phenome-fppn.fr/diaphen/variables> { 
     *      <http://www.phenome-fppn.fr/diaphen/id/units/u001>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.opensilex.org/vocabulary/oeso#Unit> . 
     *      <http://www.phenome-fppn.fr/diaphen/id/units/u001>  <http://www.w3.org/2000/01/rdf-schema#label>  ?label  . 
     *      OPTIONAL {
     *          <http://www.phenome-fppn.fr/diaphen/id/units/u001> <http://www.w3.org/2000/01/rdf-schema#comment> ?comment . 
     *      }
     *      OPTIONAL {
     *          <http://www.phenome-fppn.fr/diaphen/id/units/u001> ?property ?object . 
     *          ?object <http://www.w3.org/2000/01/rdf-schema#seeAlso> ?seeAlso .  
     *          FILTER (?property IN(<http://www.w3.org/2008/05/skos#closeMatch>, <http://www.w3.org/2008/05/skos#exactMatch>, <http://www.w3.org/2008/05/skos#narrower>, <http://www.w3.org/2008/05/skos#broader>)) 
     *      } 
     *  }}
     * @param uri
     * @return 
     */
    private SPARQLQueryBuilder prepareSearchByUri(String uri) {
        SPARQLQueryBuilder query = new SPARQLQueryBuilder();
        query.appendDistinct(Boolean.TRUE);
        
        query.appendGraph(Contexts.VARIABLES.toString());
        
        String methodURI = "<" + uri + ">";
        query.appendTriplet(methodURI, Rdf.RELATION_TYPE.toString(), Oeso.CONCEPT_UNIT.toString(), null);
        
        query.appendSelect(" ?" + LABEL + " ?" + COMMENT + " ?" + PROPERTY + " ?" + OBJECT + " ?" + SEE_ALSO);
        
        //Label
        query.appendTriplet(methodURI, Rdfs.RELATION_LABEL.toString(), "?" + LABEL, null);
        
        //Comment
        query.beginBodyOptional();
        query.appendToBody(methodURI + " <" + Rdfs.RELATION_COMMENT.toString() + "> " + "?" + COMMENT + " . ");
        query.endBodyOptional();
        
        //Ontologies references
        query.appendOptional(methodURI + " ?" + PROPERTY + " ?" + OBJECT + " . "                
                + "?" + OBJECT + " <" + Rdfs.RELATION_SEE_ALSO.toString() + "> ?" + SEE_ALSO + " . "
                + " FILTER (?" + PROPERTY + " IN(<" + Skos.RELATION_CLOSE_MATCH.toString() + ">, <"
                                           + Skos.RELATION_EXACT_MATCH.toString() + ">, <"
                                           + Skos.RELATION_NARROWER.toString() + ">, <"
                                           + Skos.RELATION_BROADER.toString() + ">))");
        
        LOGGER.debug(SPARQL_QUERY + query.toString());
        
        return query;
    }

    /**
     * Map binding set value to OntologyReference object
     * @param bindingSet
     * @return 
     */
    private OntologyReference getOntologyReferenceFromBindingSet(BindingSet bindingSet) {
        if (bindingSet.getValue(OBJECT) != null
                    && bindingSet.getValue(PROPERTY) != null) {
            OntologyReference ontologyReference = new OntologyReference();
            ontologyReference.setObject(bindingSet.getValue(OBJECT).toString());
            ontologyReference.setProperty(bindingSet.getValue(PROPERTY).toString());
            if (bindingSet.getValue(SEE_ALSO) != null) {
                ontologyReference.setSeeAlso(bindingSet.getValue(SEE_ALSO).toString());
            }
            return ontologyReference;
        }
        return null;
    }
    
    /**
     * Find a unit by it's id
     * @param id
     * @return
     * @throws DAOPersistenceException
     * @throws Exception 
     */
    @Override
    public Unit findById(String id) throws DAOPersistenceException, Exception {
        SPARQLQueryBuilder query = prepareSearchByUri(id);
        TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
        
        Unit unit = new Unit();
        unit.setUri(id);
        try(TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet row = result.next();

                if (unit.getLabel() == null && row.getValue(LABEL) != null) {
                    unit.setLabel(row.getValue(LABEL).stringValue());
                }

                if (unit.getComment() == null && row.getValue(COMMENT) != null) {
                    unit.setComment(row.getValue(COMMENT).stringValue());
                }

                OntologyReference ontologyReference = getOntologyReferenceFromBindingSet(row);
                if (ontologyReference != null) {
                    unit.addOntologyReference(ontologyReference);
                }
            }
        }
        return unit;
    }
    
    @Override
    public void validate(List<Unit> objects) throws DAOPersistenceException, DAODataErrorAggregateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

