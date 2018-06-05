//**********************************************************************************************
//                                       DocumentsDaoSesame.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 12 2017 (add status on documents : linked/unlinked)
// Subject: A Dao specific to insert the metadata of a document inside the triplestore
//***********************************************************************************************
//SILEX:warning
//After the update of the June 12, 2018 document's metadata are inserted inside 
//the triplestore and in mongodb, the document is updated (linked/unlinked)
//\SILEX:warning
//SILEX:conception
//If the object concerned by the document does not exist in the triplestore, 
//dont add the triplet (element rdf:type elementType). It allows more genericity
//but might need to be updated in the future
//\SILEX:conception
package phis2ws.service.dao.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.configuration.URINamespaces;
import phis2ws.service.dao.manager.DAOSesame;
import phis2ws.service.dao.mongo.DocumentDaoMongo;
import phis2ws.service.dao.phis.ExperimentDao;
import phis2ws.service.dao.phis.UserDaoPhisBrapi;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.model.User;
import phis2ws.service.resources.dto.AdditionalDocumentPropertiesDTO;
import phis2ws.service.resources.dto.ConcernItemDTO;
import phis2ws.service.resources.dto.DocumentMetadataDTO;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.utils.ResourcesUtils;
import phis2ws.service.utils.sparql.SPARQLQueryBuilder;
import phis2ws.service.utils.sparql.SPARQLUpdateBuilder;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.model.phis.Document;
import phis2ws.service.view.model.phis.Experiment;
import phis2ws.service.view.model.phis.Label;
import phis2ws.service.view.model.phis.RDFClass;
import phis2ws.service.view.model.phis.DocumentProperty;

public class DocumentDaoSesame extends DAOSesame<Document> {

    final static Logger LOGGER = LoggerFactory.getLogger(DocumentDaoSesame.class);
    public String uri;
    public String documentType;
    public String creator;
    public String language;
    public String title;
    public String creationDate;
    public String format;
    public String comment;
    //List of the elements concerned by the document
    public List<String> concernedItemsUris = new ArrayList<>();
    //Document's status. Equals to linked if the document has been linked to at 
    //least one element (concernedItems). Unlinked if the document isnt linked 
    //to any element
    public String status;
    //Update 03/2018 add date filter and additionnal properties
    //SILEX:info 
    //look between two date
    public String startDate; // beginning date 
    public String endDate; // ending date
    //\SILEX:info
    public List<ConcernItemDTO> concernSearch = new ArrayList<>(); // list of searched concerned item 
    public String concernedItemType; // if a document concern a particular element type
    public List<AdditionalDocumentPropertiesDTO> additionnalProperties; // look for particular additionnal propoerties

    /**
     * Triplestore relations, graph, context, concept labels
     *
     * @see https://www.w3.org/TR/rdf-schema/
     * @see http://dublincore.org/documents/dces/
     */
    //SILEX:conception
    //Maybe change the URINamespaces class in static ? 
    //\SILEX:conception
    private final static URINamespaces NAMESPACES = new URINamespaces();

    final static String TRIPLESTORE_RELATION_TYPE = "rdf:type";
    final static String TRIPLESTORE_RELATION_CREATOR = "dc:creator";
    final static String TRIPLESTORE_RELATION_LANGUGAGE = "dc:language";
    final static String TRIPLESTORE_RELATION_TITLE = "dc:title";
    final static String TRIPLESTORE_RELATION_DATE = "dc:date";
    final static String TRIPLESTORE_RELATION_FORMAT = "dc:format";
    final static String TRIPLESTORE_RELATION_COMMENT = "rdfs:comment";
    final static String TRIPLESTORE_RELATION_STATUS = NAMESPACES.getRelationsProperty("rStatus");
    final static String TRIPLESTORE_RELATION_CONCERN = NAMESPACES.getRelationsProperty("rConcern");

    final static String TRIPLESTORE_GRAPH_DOCUMENT = NAMESPACES.getContextsProperty("documents");

    final static String TRIPLESTORE_PREFIX_DUBLIN_CORE = NAMESPACES.getContextsProperty("pxDublinCore");

    final static String TRIPLESTORE_CONCEPT_DOCUMENT = NAMESPACES.getObjectsProperty("cDocuments");

    public DocumentDaoSesame() {
        super(); // Repository
        resourceType = "documents";
    }

    /**
     * Check if document's metadata are valid (check rules, documents types,
     * documents status)
     *
     * @see phis2ws.service.resources.dto.DocumentMetadataDTO#rules()
     * @param documentsMetadata
     * @return The POSTResultsReturn of the check. Contains list of errors if
     * errors found.
     */
    public POSTResultsReturn check(List<DocumentMetadataDTO> documentsMetadata) throws RepositoryException {
        POSTResultsReturn documentsMetadataCheck = null;
        // status list which will be returned. It will contains some fails or
        // informations
        List<Status> checkStatus = new ArrayList<>();

        //Get ontology documents types to check
        ArrayList<String> documentsTypes = null;
        try {
            documentsTypes = getDocumentsTypes();
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        boolean dataOk = true;
        for (DocumentMetadataDTO documentMetadata : documentsMetadata) {
            //Check if metadata respect the rules
            if ((boolean) documentMetadata.isOk().get(AbstractVerifiedClass.STATE)) {
                //1. Check document's type
                if (documentsTypes != null && !documentsTypes.contains(documentMetadata.getDocumentType())) {
                    dataOk = false;
                    checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, "Wrong document type value. Authorized document type values : " + documentsTypes.toString()));
                }

                //3. Check status (equals to linked or unlinked)
                if (!(documentMetadata.getStatus().equals("linked") || documentMetadata.getStatus().equals("unlinked"))) {
                    dataOk = false;
                    checkStatus.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                            "Wrong status value given : " + documentMetadata.getStatus() + ". Expected : \"linked\" or \"unlinked\""));
                }
            } else {
                // Metadata does not respect rules
                dataOk = false;
                documentMetadata.isOk().remove(AbstractVerifiedClass.STATE);
                checkStatus.add(new Status(StatusCodeMsg.BAD_DATA_FORMAT, StatusCodeMsg.ERR, new StringBuilder().append(StatusCodeMsg.MISSING_FIELDS_LIST).append(documentMetadata.isOk()).toString()));
            }
        }
        documentsMetadataCheck = new POSTResultsReturn(dataOk, null, dataOk);
        documentsMetadataCheck.statusList = checkStatus;
        return documentsMetadataCheck;
    }

    /**
     * save the document in mongodb
     *
     * @param filePath the file path of the document to save in mongodb
     * @return true document saved in mongodb false an error occurred
     */
    private POSTResultsReturn saveFileInMongoDB(String filePath, String fileURI) {
        DocumentDaoMongo documentDaoMongo = new DocumentDaoMongo();
        return documentDaoMongo.insertFile(filePath, fileURI);
    }

    /**
     * generate a unique document uri.
     *
     * @see phis2ws.service.utils.ResourcesUtils#getUniqueID()
     * @return the generated document's uri
     */
    private String generateDocumentsURI() {
        String fileName = "";
        boolean nameExist = true;
        final String uniqueID = ResourcesUtils.getUniqueID();
        while (nameExist) {
            fileName = new StringBuilder("document").append(uniqueID).toString();
            try {
                nameExist = exist(TRIPLESTORE_GRAPH_DOCUMENT + "/" + fileName, null, null);
            } catch (MalformedQueryException | QueryEvaluationException ex) {
                LOGGER.error(ex.getMessage(), ex);
                break;
            }
        }
        return TRIPLESTORE_GRAPH_DOCUMENT + "/" + fileName;
    }

    /**
     * insert document's metadata in the triplestore and the file in mongo
     *
     * @param documentsMetadata
     * @return the insert result, with each error or information
     */
    public POSTResultsReturn insert(List<DocumentMetadataDTO> documentsMetadata) {
        List<Status> insertStatus = new ArrayList<>(); // returned status, Failed or Info
        List<String> createdResourcesURIs = new ArrayList<>();

        POSTResultsReturn results = null;

        boolean resultState = false; // To know if data ok and saved

        boolean documentsMetadataState = true; // true if all the metadata is valid
        boolean AnnotationInsert = true; // true if the insertion has been done

        final Iterator<DocumentMetadataDTO> itAnot = documentsMetadata.iterator();

        while (itAnot.hasNext() && AnnotationInsert) {
            DocumentMetadataDTO annotObject = itAnot.next();

            //1. Save document in mongodb
            final String documentName = generateDocumentsURI();

            POSTResultsReturn saveFileResult = saveFileInMongoDB(annotObject.getServerFilePath(), documentName);
            insertStatus.addAll(saveFileResult.statusList);

            //Document has been save
            if (saveFileResult.getResultState()) {
                //2. Save document's metadata
                Map<String, Object> metadataOk = annotObject.isOk(); //check respect of rules

                documentsMetadataState = (boolean) metadataOk.get(AbstractVerifiedClass.STATE);
                //SILEX:conception
                // Here, the triplet corresponding to the concerned element which
                // does not exist should be added
                //\SILEX:conception
                //Document's metadata are correct and can be savec in triplestore
                if (documentsMetadataState) {
                    //3. Save metadata in triplestore
                    SPARQLUpdateBuilder spqlInsert = new SPARQLUpdateBuilder();
                    spqlInsert.appendPrefix("dc", TRIPLESTORE_PREFIX_DUBLIN_CORE);

                    spqlInsert.appendGraphURI(TRIPLESTORE_GRAPH_DOCUMENT); //Documents named graph
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_TYPE, annotObject.getDocumentType(), null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_CREATOR, "\"" + annotObject.getCreator() + "\"", null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_LANGUGAGE, "\"" + annotObject.getLanguage() + "\"", null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_TITLE, "\"" + annotObject.getTitle() + "\"", null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_DATE, "\"" + annotObject.getCreationDate() + "\"", null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_FORMAT, "\"" + annotObject.getExtension() + "\"", null);
                    spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_STATUS, "\"" + annotObject.getStatus() + "\"", null);

                    if (annotObject.getComment() != null) {
                        spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_COMMENT, "\"" + annotObject.getComment() + "\"", null);
                    }

                    if (!(annotObject.getConcern() == null) && !annotObject.getConcern().isEmpty()) {
                        for (ConcernItemDTO concernedItem : annotObject.getConcern()) {
                            spqlInsert.appendTriplet(documentName, TRIPLESTORE_RELATION_CONCERN, concernedItem.getUri(), null);
                            spqlInsert.appendTriplet(concernedItem.getUri(), TRIPLESTORE_RELATION_TYPE, concernedItem.getTypeURI(), null);
                        }
                    }

                    try {
                        // transaction begining
                        this.getConnection().begin();
                        Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, spqlInsert.toString());
                        LOGGER.trace(getTraceabilityLogs() + " query : " + prepareUpdate.toString());
                        prepareUpdate.execute();

                        createdResourcesURIs.add(documentName);
                    } catch (MalformedQueryException e) {
                        LOGGER.error(e.getMessage(), e);
                        AnnotationInsert = false;
                        insertStatus.add(new Status(StatusCodeMsg.QUERY_ERROR, StatusCodeMsg.ERR, StatusCodeMsg.MALFORMED_CREATE_QUERY + " : " + e.getMessage()));
                    }
                } else {
                    // JSON malformé de quelque sorte que ce soit
                    documentsMetadataState = false;
                    metadataOk.remove(AbstractVerifiedClass.STATE);
                    insertStatus.add(new Status(StatusCodeMsg.MISSING_FIELDS, StatusCodeMsg.ERR, new StringBuilder().append(StatusCodeMsg.MISSING_FIELDS_LIST).append(metadataOk).toString()));
                }

                // JSON bien formé et pas de problème avant l'insertion
                if (AnnotationInsert && documentsMetadataState) {
                    resultState = true;
                    try {
                        this.getConnection().commit();
                    } catch (RepositoryException ex) {
                        LOGGER.error("Error during commit Triplestore statements: ", ex);
                    }
                } else {
                    // retour en arrière sur la transaction
                    try {
                        this.getConnection().rollback();
                    } catch (RepositoryException ex) {
                        LOGGER.error("Error during rollback Triplestore statements : ", ex);
                    }
                }
            }
        }

        results = new POSTResultsReturn(resultState, AnnotationInsert, documentsMetadataState);
        results.statusList = insertStatus;
        if (resultState && !createdResourcesURIs.isEmpty()) {
            results.createdResources = createdResourcesURIs;
            results.statusList.add(new Status(StatusCodeMsg.RESOURCES_CREATED, StatusCodeMsg.INFO, createdResourcesURIs.size() + " new resource(s) created."));
        }

        return results;
    }

    /**
     * Return the list of documents types Retourne les types de documents
     * disponibles
     *
     * @return List de concepts de document
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public ArrayList<String> getDocumentsTypes() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<String> documentsSchemasUri = new ArrayList<>();
        SPARQLQueryBuilder sparqlQ = new SPARQLQueryBuilder();
        sparqlQ.appendDistinct(true);
        sparqlQ.appendSelect("?documentType");
        sparqlQ.appendTriplet("?documentType", "rdfs:subClassOf*", TRIPLESTORE_CONCEPT_DOCUMENT, null);
        sparqlQ.appendFilter("?documentType != <" + TRIPLESTORE_CONCEPT_DOCUMENT + ">");
        LOGGER.debug(sparqlQ.toString());
        TupleQuery tupleQueryTo = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQ.toString());

        try (TupleQueryResult resultTo = tupleQueryTo.evaluate()) {
            while (resultTo.hasNext()) {
                BindingSet bindingSet = resultTo.next();
                if (bindingSet.getValue("documentType") != null) {
                    documentsSchemasUri.add(bindingSet.getValue("documentType").stringValue());
                }
            }
        }
        return documentsSchemasUri;
    }

    @Override
    protected SPARQLQueryBuilder prepareSearchQuery() {
        SPARQLQueryBuilder sparqlQuery = new SPARQLQueryBuilder();
        sparqlQuery.appendPrefix("dc", TRIPLESTORE_PREFIX_DUBLIN_CORE);
        sparqlQuery.appendDistinct(true);
        sparqlQuery.appendGraph(TRIPLESTORE_GRAPH_DOCUMENT);
        String select;
        if (uri != null) {
            select = "<" + uri + ">";
            sparqlQuery.appendSelect("");
        } else {
            select = "?documentUri";
            sparqlQuery.appendSelect(select);
        }

        if (documentType != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_TYPE, documentType, null);
        } else {
            sparqlQuery.appendSelect(" ?documentType");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_TYPE, "?documentType", null);
        }

        if (creator != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_CREATOR, "\"" + creator + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?creator");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_CREATOR, "?creator", null);
        }

        if (language != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_LANGUGAGE, "\"" + language + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?language");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_LANGUGAGE, "?language", null);
        }

        if (title != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_TITLE, "\"" + title + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?title");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_TITLE, "?title", null);
        }

        if (creationDate != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_DATE, "\"" + creationDate + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?date");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_DATE, "?date", null);
        }

        if (format != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_FORMAT, "\"" + format + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?format");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_FORMAT, "?format", null);
        }

        if (!concernedItemsUris.isEmpty() && concernedItemsUris.size() > 0) {
            for (String concernedItemUri : concernedItemsUris) {
                sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_CONCERN, concernedItemUri, null);
            }
        }

        if (status != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_STATUS, "\"" + status + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?status");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_STATUS, "?status", null);
        }

        if (startDate != null) {
            if (sparqlQuery.getFilter() != "") {
                sparqlQuery.appendFilter(" && ");
            }
            sparqlQuery.appendFilter("?date > \"" + startDate + "\" ");
        }

        if (endDate != null) {
            if (sparqlQuery.getFilter() != "") {
                sparqlQuery.appendFilter(" && ");
            }
            sparqlQuery.appendFilter("?date < \"" + endDate + "\" ");
        }

        // TEST element concernes multiples
        if (concernedItemsUris != null && !concernedItemsUris.isEmpty() && concernedItemsUris.size() > 0) {
            String concernRequest = "  VALUES (?value) { \n";
            for (String concernedItemUri : concernedItemsUris) {
                concernRequest += "( <" + concernedItemUri + "> ) \n";
            }
            concernRequest += "  \n }";
            sparqlQuery.appendToBody("\n" + concernRequest + "\n");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_CONCERN, "?value", null);

        }
        //additionnal properties
        if (additionnalProperties != null && (!additionnalProperties.isEmpty() && additionnalProperties.size() > 0)) {
            String addPropRequest = "  VALUES (?addprop) { \n";
            for (AdditionalDocumentPropertiesDTO additionnalProperty : additionnalProperties) {
                addPropRequest += "( <" + additionnalProperty.getValue() + "> ) \n";
            }
            addPropRequest += "  \n }";
            sparqlQuery.appendToBody("\n" + addPropRequest + "\n");
            sparqlQuery.appendTriplet(select, "?prop", "?addprop", null);

        }

        if (status != null) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_STATUS, "\"" + status + "\"", null);
        } else {
            sparqlQuery.appendSelect(" ?status");
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_STATUS, "?status", null);
        }

        //Add filter by type of concerned item
        if (concernedItemType != null && concernedItemsUris.size() == 0) {
            sparqlQuery.appendTriplet(select, TRIPLESTORE_RELATION_CONCERN, "?concernedItemType", null);
            if (sparqlQuery.getFilter() != "") {
                sparqlQuery.appendFilter(" && ");
            }
            sparqlQuery.appendFilter("regex(str(?concernedItemType), \"" + concernedItemType + "\") ");
        }

        LOGGER.debug("sparql select query : " + sparqlQuery.toString());

        return sparqlQuery;
    }

    /**
     * prepare the query to search the comments of a document
     *
     * @param uriDocument
     * @return the document's comments search query
     */
    private SPARQLQueryBuilder prepareSearchCommentQuery(String uriDocument) {
        SPARQLQueryBuilder sparqlQuery = new SPARQLQueryBuilder();
        sparqlQuery.appendDistinct(true);
        sparqlQuery.appendGraph(TRIPLESTORE_GRAPH_DOCUMENT);
        sparqlQuery.appendSelect("?comment");
        sparqlQuery.appendTriplet(uriDocument, TRIPLESTORE_RELATION_COMMENT, "?comment", null);

        LOGGER.debug("sparql select query : " + sparqlQuery.toString());
        return sparqlQuery;
    }

    /**
     * prepare the query to search the elements which are concerned by the
     * document
     *
     * @param uriDocument
     * @return the search query
     */
    private SPARQLQueryBuilder prepareSearchConcernQuery(String uriDocument) {
        SPARQLQueryBuilder sparqlQuery = new SPARQLQueryBuilder();
        sparqlQuery.appendDistinct(true);
        sparqlQuery.appendGraph(TRIPLESTORE_GRAPH_DOCUMENT);

        sparqlQuery.appendSelect(" ?concern ?typeConcern");
        sparqlQuery.appendTriplet(uriDocument, TRIPLESTORE_RELATION_CONCERN, "?concern", null);
        sparqlQuery.appendTriplet("?concern", TRIPLESTORE_RELATION_TYPE, "?typeConcern", null);

        LOGGER.debug("sparql select query : " + sparqlQuery.toString());

        return sparqlQuery;
    }

    /**
     *
     * @param uriDocument
     * @return la requête permettant de récupérer les proprietes additionnelles
     * de la sous-classe du document
     */
    private SPARQLQueryBuilder prepareSearchAdditionalPropertiesQuery(String uriDocument) {
        final URINamespaces uriNS = new URINamespaces();
        SPARQLQueryBuilder sparqlQuery = new SPARQLQueryBuilder();
        sparqlQuery.appendPrefix("dc", uriNS.getContextsProperty("pxDublinCore"));
        sparqlQuery.appendDistinct(true);
        sparqlQuery.appendGraph(uriNS.getContextsProperty("documents"));

        sparqlQuery.appendSelect(" ?property ?value");
        sparqlQuery.appendTriplet(uriDocument, "?property", "?value", null);
        sparqlQuery.appendFilter("!strstarts(str(?property),str(rdfs:comment)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:date)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:status)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(rdf:type)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:format)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:title)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:language)) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(\"" + uriNS.getRelationsProperty("rConcern") + "\")) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(\"" + uriNS.getRelationsProperty("rStatus") + "\")) && ");
        sparqlQuery.appendFilter("!strstarts(str(?property),str(dc:creator)) ");

        LOGGER.trace("sparql select query : " + sparqlQuery.toString());

        return sparqlQuery;
    }

    @Override
    public Integer count() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * check if the given user has the right to see the document
     *
     * @param u the user
     * @param document
     * @return true if the user can see the document
     */
    private boolean canUserSeeDocument(User u, Document document) {
        UserDaoPhisBrapi userDao = new UserDaoPhisBrapi();
        userDao.isAdmin(u);
        if (u.getAdmin().equals("t") || u.getAdmin().equals("true")) {
            return true;
        } else {
            ExperimentDao experimentDao = new ExperimentDao();
            for (ConcernItemDTO concernItem : document.getConcernedItems()) {
                if (concernItem.getTypeURI().equals(NAMESPACES.getContextsProperty("pVoc2017") + "#Experiment")) {
                    Experiment experiment = new Experiment(concernItem.getUri());

                    if (experimentDao.canUserSeeExperiment(u, experiment)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     *
     * @return liste de documents, résultat de la recherche, vide si pas de
     * résultats.
     */
    public ArrayList<Document> allPaginate() {
        SPARQLQueryBuilder sparqlQuery = prepareSearchQuery();
        TupleQuery tupleQuery = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery.toString());
        ArrayList<Document> documents = new ArrayList<>();

        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Document document = new Document();

                if (uri != null) {
                    document.setUri(uri);
                } else {
                    document.setUri(bindingSet.getValue("documentUri").stringValue());
                }

                if (documentType != null) {
                    document.setDocumentType(documentType);
                } else {
                    document.setDocumentType(bindingSet.getValue("documentType").stringValue());
                }

                if (creator != null) {
                    document.setCreator(creator);
                } else {
                    document.setCreator(bindingSet.getValue("creator").stringValue());
                }

                if (language != null) {
                    document.setLanguage(language);
                } else {
                    document.setLanguage(bindingSet.getValue("language").stringValue());
                }

                if (title != null) {
                    document.setTitle(title);
                } else {
                    document.setTitle(bindingSet.getValue("title").stringValue());
                }

                if (creationDate != null) {
                    document.setCreationDate(creationDate);
                } else {
                    document.setCreationDate(bindingSet.getValue("date").stringValue());
                }

                if (format != null) {
                    document.setFormat(format);
                } else {
                    document.setFormat(bindingSet.getValue("format").stringValue());
                }

                if (status != null) {
                    document.setStatus(status);
                } else {
                    document.setStatus(bindingSet.getValue("status").stringValue());
                }

                //After having metadata, get comments
                SPARQLQueryBuilder sparqlQueryComment = prepareSearchCommentQuery(document.getUri());
                TupleQuery tupleQueryComment = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQueryComment.toString());
                TupleQueryResult resultComment = tupleQueryComment.evaluate();
                while (resultComment.hasNext()) {
                    BindingSet bindingSetComment = resultComment.next();
                    if (bindingSetComment.getValue("comment") != null) {
                        document.setComment(bindingSetComment.getValue("comment").stringValue());
                    }
                }

                //Check if document is linked to other elements
                SPARQLQueryBuilder sparqlQueryConcern = prepareSearchConcernQuery(document.getUri());
                TupleQuery tupleQueryConcern = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQueryConcern.toString());
                TupleQueryResult resultConcern = tupleQueryConcern.evaluate();
                while (resultConcern.hasNext()) {
                    BindingSet bindingSetConcern = resultConcern.next();
                    if (bindingSetConcern.getValue("concern") != null) {
                        ConcernItemDTO concernedItem = new ConcernItemDTO();
                        concernedItem.setTypeURI(bindingSetConcern.getValue("typeConcern").stringValue());
                        concernedItem.setUri(bindingSetConcern.getValue("concern").stringValue());
                        document.addConcernedItem(concernedItem);
                    }
                }

                //Check if document has additionnal properties
                SPARQLQueryBuilder sparqlQueryAdditionnalProperties = prepareSearchAdditionalPropertiesQuery(document.getUri());
                TupleQuery tupleQueryProperties = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQueryAdditionnalProperties.toString());
                TupleQueryResult resultProperties = tupleQueryProperties.evaluate();
                while (resultProperties.hasNext()) {
                    BindingSet bindingSetProperties = resultProperties.next();
                    if (bindingSetProperties.getValue("value") != null && bindingSetProperties.getValue("property") != null) {
                        AdditionalDocumentPropertiesDTO addProperty = new AdditionalDocumentPropertiesDTO();
                        addProperty.setProperty(bindingSetProperties.getValue("property").stringValue());
                        addProperty.setValue(bindingSetProperties.getValue("value").stringValue());
                        document.addAdditionnalProperties(addProperty);
                    }
                }

                if (canUserSeeDocument(user, document)) {
                    documents.add(document);
                }
            }
        }

        return documents;
    }

    //SILEX:todo
    //Create a check method for the update and the insert
    //\SILEX:todo
    /**
     * check the metadata and update them if metadata valid
     *
     * @see phis2ws.service.resources.dto.DocumentMetadataDTO#rules()
     * @param documentsMetadata
     * @return the query result with the list of error or informations
     */
    private POSTResultsReturn checkAndUpdateDocumentsMetadataList(List<DocumentMetadataDTO> documentsMetadata) {
        List<Status> updateStatusList = new ArrayList<>(); // Failed or Info
        List<String> updatedResourcesURIList = new ArrayList<>(); // Failed or Info
        POSTResultsReturn results;

        boolean annotationUpdate = true; // true if the update has been done
        boolean docsMetadataState = true;
        boolean resultState = false; // To know if the metadata where valid and updated

        for (DocumentMetadataDTO documentMetadata : documentsMetadata) {

            //Check that the metadata is valid (rules, status)
            Map<String, Object> metadataOk = documentMetadata.isOk();
            boolean documentMetadataState = (boolean) metadataOk.get(AbstractVerifiedClass.STATE);

            if (!(documentMetadata.getStatus().equals("linked") || documentMetadata.getStatus().equals("unlinked"))) {
                documentMetadataState = false;
                updateStatusList.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR,
                        "Wrong status value given : " + documentMetadata.getStatus() + ". Expected : \"linked\" or \"unlinked\""));
            }

            //If metadata valid
            if (documentMetadataState) {
                //1. Delete actual metadata
                //1.1 Get informations which will be updated (to remove triplets)
                DocumentDaoSesame docDaoSesame = new DocumentDaoSesame();
                docDaoSesame.user = user;
                docDaoSesame.uri = documentMetadata.getUri();
                ArrayList<Document> documentsCorresponding = docDaoSesame.allPaginate();

                String deleteQuery = null;
                //1.2 Delete metatada associated to the URI
                if (documentsCorresponding.size() > 0) {
                    //SILEX:conception
                    //Such as the existing querybuilder for the insert, create a
                    //delete query builder and use it
                    deleteQuery = "PREFIX dc: <" + TRIPLESTORE_PREFIX_DUBLIN_CORE + "#> "
                            + "DELETE WHERE { "
                            + "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_CREATOR + " \"" + documentsCorresponding.get(0).getCreator() + "\" . "
                            + "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_LANGUGAGE + " \"" + documentsCorresponding.get(0).getLanguage() + "\" . "
                            + "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_TITLE + " \"" + documentsCorresponding.get(0).getTitle() + "\" . "
                            + "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_DATE + " \"" + documentsCorresponding.get(0).getCreationDate() + "\" . "
                            + "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_TYPE + " <" + documentsCorresponding.get(0).getDocumentType() + "> . "
                            + "<" + documentsCorresponding.get(0).getUri() + "> <" + TRIPLESTORE_RELATION_STATUS + "> \"" + documentsCorresponding.get(0).getStatus() + "\" . ";

                    if (documentsCorresponding.get(0).getComment() != null) {
                        deleteQuery += "<" + documentsCorresponding.get(0).getUri() + "> " + TRIPLESTORE_RELATION_COMMENT + " \"" + documentsCorresponding.get(0).getComment() + "\" . ";
                    }

                    for (ConcernItemDTO concernedItem : documentsCorresponding.get(0).getConcernedItems()) {
                        deleteQuery += "<" + documentsCorresponding.get(0).getUri() + "> <" + TRIPLESTORE_RELATION_CONCERN + "> <" + concernedItem.getUri() + "> . ";
                    }
                    deleteQuery += "}";
                    //\SILEX:conception
                }

                //2. Insert updated metadata
                SPARQLUpdateBuilder spqlInsert = new SPARQLUpdateBuilder();
                spqlInsert.appendPrefix("dc", TRIPLESTORE_PREFIX_DUBLIN_CORE);
                spqlInsert.appendGraphURI(TRIPLESTORE_GRAPH_DOCUMENT);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_TYPE, documentMetadata.getDocumentType(), null);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_CREATOR, "\"" + documentMetadata.getCreator() + "\"", null);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_LANGUGAGE, "\"" + documentMetadata.getLanguage() + "\"", null);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_TITLE, "\"" + documentMetadata.getTitle() + "\"", null);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_DATE, "\"" + documentMetadata.getCreationDate() + "\"", null);
                spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_STATUS, "\"" + documentMetadata.getStatus() + "\"", null);

                if (documentMetadata.getComment() != null) {
                    spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_COMMENT, "\"" + documentMetadata.getComment() + "\"", null);
                }

                if (documentMetadata.getConcern() != null && !documentMetadata.getConcern().isEmpty() && documentMetadata.getConcern().size() > 0) {
                    for (ConcernItemDTO concernedItem : documentMetadata.getConcern()) {
                        spqlInsert.appendTriplet(documentMetadata.getUri(), TRIPLESTORE_RELATION_CONCERN, concernedItem.getUri(), null);
                        spqlInsert.appendTriplet(concernedItem.getUri(), TRIPLESTORE_RELATION_TYPE, concernedItem.getTypeURI(), null);
                    }
                }

                try {
                    // début de la transaction : vérification de la requête
                    this.getConnection().begin();
                    Update prepareDelete = this.getConnection().prepareUpdate(deleteQuery);
                    Update prepareUpdate = this.getConnection().prepareUpdate(QueryLanguage.SPARQL, spqlInsert.toString());
                    LOGGER.debug(getTraceabilityLogs() + " query : " + prepareDelete.toString());
                    LOGGER.debug(getTraceabilityLogs() + " query : " + prepareUpdate.toString());
                    prepareDelete.execute();
                    prepareUpdate.execute();

                    updatedResourcesURIList.add(documentMetadata.getUri());
                } catch (MalformedQueryException e) {
                    LOGGER.error(e.getMessage(), e);
                    annotationUpdate = false;
                    updateStatusList.add(new Status(StatusCodeMsg.QUERY_ERROR, StatusCodeMsg.ERR, StatusCodeMsg.MALFORMED_UPDATE_QUERY + e.getMessage()));
                }

            } else {
                // Missing fields
                docsMetadataState = false;
                metadataOk.remove(AbstractVerifiedClass.STATE);
                updateStatusList.add(new Status(StatusCodeMsg.MISSING_FIELDS, StatusCodeMsg.ERR, new StringBuilder().append(StatusCodeMsg.MISSING_FIELDS_LIST).append(metadataOk).toString()));
            }

            // Data ok, update
            if (annotationUpdate && docsMetadataState) {
                resultState = true;
                try {
                    this.getConnection().commit();
                } catch (RepositoryException ex) {
                    LOGGER.error("Error during commit Triplestore statements: ", ex);
                }
            } else {
                // retour en arrière sur la transaction
                try {
                    this.getConnection().rollback();
                } catch (RepositoryException ex) {
                    LOGGER.error("Error during rollback Triplestore statements : ", ex);
                }
            }
        }
        results = new POSTResultsReturn(resultState, annotationUpdate, docsMetadataState);
        results.statusList = updateStatusList;
        if (resultState && !updatedResourcesURIList.isEmpty()) {
            results.createdResources = updatedResourcesURIList;
            results.statusList.add(new Status(StatusCodeMsg.RESOURCES_CREATED, StatusCodeMsg.INFO, updatedResourcesURIList.size() + " new resource(s) created."));
        }

        return results;
    }

    /**
     * check new metadata and update if no error
     *
     * @param objectsToUpdate
     * @return the update result, with errors or informations
     */
    public POSTResultsReturn checkAndUpdateList(List<DocumentMetadataDTO> objectsToUpdate) {
        POSTResultsReturn postResult;

        postResult = this.checkAndUpdateDocumentsMetadataList(objectsToUpdate);

        return postResult;
    }

    /**
     * Return properties of a specific document sub class
     *
     * @param documentClass sous classe de documents
     * @return List de concepts de document
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public ArrayList<DocumentProperty> getDocumentsProperties(String documentClass) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<DocumentProperty> documentsSchemasUri = new ArrayList<>();
        // rdf4j query
        // show sub class properties(ranges and labels), properties(ranges,types, labels)
        SPARQLQueryBuilder sparqlQ = new SPARQLQueryBuilder();
        sparqlQ.appendDistinct(true);
        sparqlQ.appendSelect("?property ?range ?type ?label ?rangeLabel ?rangeType");
        sparqlQ.appendTriplet("?property", "rdfs:domain", documentClass, null);
        sparqlQ.appendTriplet("?property", "rdf:type", "?type", null);
        sparqlQ.appendTriplet("?property", "rdfs:range", "?range", null);
        sparqlQ.beginBodyOptional();
        sparqlQ.appendTriplet("?property", "rdfs:label", "?label", null);
        sparqlQ.endBodyOptional();
        sparqlQ.beginBodyOptional();
        sparqlQ.appendTriplet("?property", "rdfs:range", "?range", null);
        sparqlQ.appendTriplet("?range", "rdfs:label", "?rangeLabel", null);
        sparqlQ.appendTriplet("?range", "rdf:type", "?rangeType", null);
        sparqlQ.endBodyOptional();
        LOGGER.trace(sparqlQ.toString());

        TupleQuery tupleQueryTo = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQ.toString());

        // results  map et list
        HashMap<String, HashMap<String, Object>> propertiesMap = new HashMap<>();
        HashMap<String, HashMap<String, String>> rangeLabels = new HashMap<>();
        HashMap<String, String> rangeType = new HashMap<>();
        HashMap<String, ArrayList<RDFClass>> rangeSubTypeArray = new HashMap<>();

        try (TupleQueryResult resultTo = tupleQueryTo.evaluate()) {
            while (resultTo.hasNext()) {
                BindingSet bindingSet = resultTo.next();
                if (bindingSet.getValue("property") != null) {
                    // looking for properties
                    // example : wasgeneratedby => [type => owl:AnnotationProperty ]
                    // if not set a new map is created,  it represents the properties with these attributes
                    if (!propertiesMap.containsKey(bindingSet.getValue("property").stringValue())) {
                        propertiesMap.put(bindingSet.getValue("property").stringValue(), new HashMap<String, Object>());
                        HashMap<String, Object> propertyMap = propertiesMap.get(bindingSet.getValue("property").stringValue());
                        // add property type in map
                        if (!propertyMap.containsKey("type")) {
                            propertyMap.put("type", bindingSet.getValue("type").stringValue());
                        }
                    }
                    //looking for range
                    //example : wasgenertedby => [range => [ <http://www.phenome-fppn.fr/vocabulary/2017#MeasurementMethod>]
                    if (bindingSet.getValue("range") != null) {
                        HashMap<String, Object> propertyMap = propertiesMap.get(bindingSet.getValue("property").stringValue());
                        if (!propertyMap.containsKey("range")) {
                            propertyMap.put("range", new ArrayList<String>());
                        }
                        ArrayList rangesList = (ArrayList<String>) propertyMap.get("range");
                        if (!rangesList.contains(bindingSet.getValue("range").stringValue())) {
                            rangesList.add(bindingSet.getValue("range").stringValue());
                            // Subtypes
                            //A second query is made for a better comprehension of this code.
                            //It retreive sub range if they exist 
                            //example :wasgenertedby => [
//                            range => [ <http://www.phenome-fppn.fr/vocabulary/2017#MeasurementMethod>
//                              subRanges => http://www.phenome-fppn.fr/vocabulary/2017#AminoAcidAnalyzer
//                          ]
                            // Retreive properties (range, type, labels)
                            SPARQLQueryBuilder sparqlSubTypes = new SPARQLQueryBuilder();
                            sparqlSubTypes.appendDistinct(true);
                            sparqlSubTypes.appendSelect("?subtype ?sublabel ?subtypetype");
                            sparqlSubTypes.appendTriplet("?property", "rdfs:domain", documentClass, null);
                            sparqlSubTypes.appendTriplet("?property", "rdfs:range", bindingSet.getValue("range").stringValue(), null);
                            sparqlSubTypes.beginBodyOptional();
                            sparqlSubTypes.appendTriplet("?subtype", "rdfs:label", "?sublabel", null);
                            sparqlSubTypes.endBodyOptional();
                            sparqlSubTypes.beginBodyOptional();
                            sparqlSubTypes.appendTriplet("?property", "rdfs:range", "?range", null);
                            sparqlSubTypes.appendTriplet("?subtype", "rdfs:subClassOf*", "?range", null);
                            sparqlSubTypes.appendTriplet("?subtype", "rdf:type", "?subtypetype", null);
                            sparqlSubTypes.endBodyOptional();

                            LOGGER.trace(sparqlSubTypes.toString());

                            HashMap<String, String> subSubType = new HashMap<>();
                            HashMap<String, HashMap<String, String>> subLabels = new HashMap<>();

                            TupleQuery tupleQuerySubType = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlSubTypes.toString());
                            try (TupleQueryResult resultSubType = tupleQuerySubType.evaluate()) {
                                while (resultSubType.hasNext()) {

                                    BindingSet bindingSetSubTypes = resultSubType.next();

                                    if (bindingSetSubTypes.getValue("subtype") != null) {
                                        // retreive subtype type property
                                        if (!bindingSetSubTypes.getValue("subtype").stringValue().equals(bindingSet.getValue("range").stringValue())
                                                && !subSubType.containsKey(bindingSetSubTypes.getValue("subtype").stringValue())) {
                                            if (bindingSetSubTypes.getValue("subtypetype") != null) {
                                                subSubType.put(bindingSetSubTypes.getValue("subtype").stringValue(), bindingSetSubTypes.getValue("subtypetype").stringValue());
                                            }
                                        }

                                        // retreive labels
                                        Value subLabelVal = bindingSetSubTypes.getValue("sublabel");
                                        if (!subLabels.containsKey(bindingSetSubTypes.getValue("subtype").stringValue())) {
                                            // if label exist
                                            if (subLabelVal != null) {
                                                // create a label map for a label
                                                HashMap<String, String> langLabelMap = new HashMap<>();
                                                this.retreiveBindingValueLabelInMap(langLabelMap, subLabelVal);
                                                subLabels.put(bindingSetSubTypes.getValue("subtype").stringValue(), langLabelMap);
                                            }
                                        } else {
                                            if (subLabelVal != null) {
                                                // if label map already have one
                                                HashMap<String, String> langLabelMap = subLabels.get(bindingSetSubTypes.getValue("subtype").stringValue());
                                                this.retreiveBindingValueLabelInMap(langLabelMap, subLabelVal);
                                            }
                                        }
                                    }

                                }
                            }
                            //SILEX:test
                            //LOGGER.debug(JsonConverter.ConvertToJson(subSubType));
                            //LOGGER.debug(JsonConverter.ConvertToJson(subLabels));
                            //SILEX:test

                            // sub range list
                            ArrayList<RDFClass> subTypesArray = new ArrayList<>();
                            if (!subSubType.isEmpty()) {
                                subSubType.forEach(
                                        (subSubTypeUri, subSubTypeTypeValue) -> {
                                            RDFClass subTypesClass = new RDFClass();
                                            subTypesClass.setUri(subSubTypeUri);
                                            subTypesClass.setType(subSubTypeTypeValue);
                                            HashMap<String, String> langLabelMapSubType = subLabels.get(subSubTypeUri);
                                            if (langLabelMapSubType != null && !langLabelMapSubType.isEmpty()) {
                                                langLabelMapSubType.forEach(
                                                        (sLang, vLangValue) -> {
                                                            subTypesClass.addLabel(new Label(sLang, vLangValue));
                                                        });
                                            }
                                            subTypesArray.add(subTypesClass);
                                        });
                            }
                            rangeSubTypeArray.put(bindingSet.getValue("range").stringValue(), subTypesArray);
                        }
                    }
                    // retreive range label
                    Value rangeLabelVal = bindingSet.getValue("rangeLabel");
                    // <http://www.phenome-fppn.fr/vocabulary/2017#MeasurementMethod> => [ 'fr' => 'méthode de mesures']
                    if (!rangeLabels.containsKey(bindingSet.getValue("range").stringValue())) {
                        if (rangeLabelVal != null) {
                            HashMap<String, String> langLabelMap = new HashMap<>();
                            this.retreiveBindingValueLabelInMap(langLabelMap, rangeLabelVal);
                            rangeLabels.put(bindingSet.getValue("range").stringValue(), langLabelMap);
                        }
                    } else {
                        // case : not the first label in the map
                        if (rangeLabelVal != null) {
                            HashMap<String, String> langLabelMap = rangeLabels.get(bindingSet.getValue("range").stringValue());
                            this.retreiveBindingValueLabelInMap(langLabelMap, rangeLabelVal);
                        }
                    }
                    if (!rangeType.containsKey(bindingSet.getValue("range").stringValue())) {
                        if (bindingSet.getValue("rangeType") != null) {
                            rangeType.put(bindingSet.getValue("range").stringValue(), bindingSet.getValue("rangeType").stringValue());
                        }
                    }
                }
                // retreive property label
                // example wasgenertedby => [labels => ['fr' => 'est genere par" ]
                if (bindingSet.getValue("label") != null) {
                    HashMap<String, Object> propertyMap = propertiesMap.get(bindingSet.getValue("property").stringValue());
                    if (!propertyMap.containsKey("labels")) {
                        propertyMap.put("labels", new HashMap<String, String>());
                    }
                    HashMap<String, String> labelList = (HashMap<String, String>) propertyMap.get("labels");
                    Value label = bindingSet.getValue("label");
                    this.retreiveBindingValueLabelInMap(labelList, label);
                }
            }
        }
        // for each properties
        propertiesMap.forEach(
                (propertyUri, propertyValueMap) -> {
                    // fill a document property class
                    DocumentProperty documentProperty = new DocumentProperty();
                    RDFClass domain = new RDFClass();
                    domain.setUri(documentClass);
                    documentProperty.addDomain(domain);
                    documentProperty.setPropertyUri(propertyUri);
                    documentProperty.setPropertyType((String) propertyValueMap.get("type"));
                    // fill document property class labels
                    HashMap<String, String> labels = (HashMap<String, String>) propertyValueMap.get("labels");
                    if (labels != null && !labels.isEmpty()) {
                        labels.forEach((labelKey, labelValue) -> {
                            documentProperty.addLabel(new Label(labelKey, labelValue));
                        });
                    }
                    // fill ranges
                    ArrayList<String> range = (ArrayList<String>) propertyValueMap.get("range");
                    if (range != null && !range.isEmpty()) {
                        for (String rangeUri : range) {
                            RDFClass rangeItem = new RDFClass();
                            rangeItem.setUri(rangeUri);
                            if (!rangeLabels.isEmpty()) {
                                // For each range we look  for each subrange => depth only 1 level
                                if (rangeLabels.containsKey(rangeUri)) {
                                    HashMap<String, String> rangelabelMap = rangeLabels.get(rangeUri);
                                    rangelabelMap.forEach((rangeLabelKey, rangeLabelValue) -> {
                                        rangeItem.addLabel(new Label(rangeLabelKey, rangeLabelValue));
                                    });
                                }
                            }
                            // retreive type
                            if (!rangeType.isEmpty()) {
                                rangeItem.setType(rangeType.get(rangeItem.getUri()));
                            }
                            // retreive sub type
                            if (!rangeSubTypeArray.isEmpty() && rangeSubTypeArray.containsKey(rangeUri)) {
                                rangeItem.setSubRanges(rangeSubTypeArray.get(rangeUri));
                            }
                            documentProperty.addRange(rangeItem);
                        }
                    }
                    documentsSchemasUri.add(documentProperty);
                }
        );
        return documentsSchemasUri;
    }

    /**
     * Retourne les informations d'une sous-classes de documents avec ou sans
     * les instances
     *
     * @param documentProperty uri de la sous-classe de Document
     * @param instanceBoolean permet d'envoyer les instances liees a la
     * sous-classe de Document
     * @return List de concepts de document
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public ArrayList<DocumentProperty> getDocumentsProperty(String documentProperty, Boolean instanceBoolean) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        URINamespaces uriNS = new URINamespaces();
        ArrayList<DocumentProperty> documentsSchemasUri = new ArrayList<>();
        // requete rdf4j
        SPARQLQueryBuilder sparqlQ = new SPARQLQueryBuilder();
        sparqlQ.appendDistinct(true);
        sparqlQ.appendSelect("?range ?type ?label ?instance");
        sparqlQ.appendTriplet(documentProperty, "rdf:type", "?type", null);
        sparqlQ.beginBodyOptional();
        sparqlQ.appendTriplet(documentProperty, "rdfs:label", "?label", null);
        sparqlQ.endBodyOptional();
        sparqlQ.beginBodyOptional();
        sparqlQ.appendTriplet(documentProperty, "rdfs:range", "?range", null);
        sparqlQ.endBodyOptional();
        if (instanceBoolean != null && instanceBoolean == true) {
            sparqlQ.beginBodyOptional();
            sparqlQ.appendTriplet("?instance", "rdf:type", documentProperty, null);
            sparqlQ.endBodyOptional();
        }
        LOGGER.trace(sparqlQ.toString());

        TupleQuery tupleQueryTo = this.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQ.toString());

        // results map et list
        // Map of map or map of arraylist
        //SILEX:conception 
        // Maybe use map of collection
        //\SILEX:concpetion
        HashMap<String, HashMap<String, Object>> propertiesMap = new HashMap<>();
        ArrayList<String> range = new ArrayList<>();
        ArrayList<String> instances = new ArrayList<>();

        try (TupleQueryResult resultTo = tupleQueryTo.evaluate()) {
            while (resultTo.hasNext()) {
                BindingSet bindingSet = resultTo.next();
                if (bindingSet.getValue("type") != null) {
                    // looking for subclass property information (uri and type) 
                    // wasgeneratedby => [type => owl:AnnotationProperty ]
                    if (!propertiesMap.containsKey(documentProperty)) {
                        propertiesMap.put(documentProperty, new HashMap<String, Object>());
                        HashMap<String, Object> propertyMap = propertiesMap.get(documentProperty);
                        if (!propertyMap.containsKey("type")) {
                            propertyMap.put("type", bindingSet.getValue("type").stringValue());
                        }
                    }
                    // recherche de label
                    // wasgenertedby => [label => ['fr' => 'est genere par" ]
                    Value label = bindingSet.getValue("label");
                    if (label != null) {
                        HashMap<String, Object> propertyMap = propertiesMap.get(documentProperty);
                        if (!propertyMap.containsKey("labels")) {
                            propertyMap.put("labels", new HashMap<String, String>());
                        }
                        HashMap<String, String> labelList = (HashMap<String, String>) propertyMap.get("labels");
                        this.retreiveBindingValueLabelInMap(labelList, label);
                    }
                }
                // look for range
                if (bindingSet.getValue("range") != null) {
                    if (!range.contains(bindingSet.getValue("range").stringValue())) {
                        range.add(bindingSet.getValue("range").stringValue());
                    }
                }
                // look for subclass instances
                if (bindingSet.getValue("instance") != null) {
                    if (!instances.contains(bindingSet.getValue("instance").stringValue())) {
                        instances.add(bindingSet.getValue("instance").stringValue());
                    }
                }

            }
        }
        // for each proprieties
        propertiesMap.forEach(
                (documentPropertyKey, documentPropertyValue) -> {
                    DocumentProperty documentPropertyClass = new DocumentProperty();
                    RDFClass domain = new RDFClass();
                    domain.setUri(documentProperty);
                    documentPropertyClass.setPropertyUri(documentProperty);
                    documentPropertyClass.setPropertyType((String) documentPropertyValue.get("type"));
                    HashMap<String, String> labels = (HashMap<String, String>) documentPropertyValue.get("labels");
                    if (labels != null && !labels.isEmpty()) {
                        labels.forEach((labelKey, labelValue) -> {
                            documentPropertyClass.addLabel(new Label(labelKey, labelValue));
                        });
                    }
                    if (!range.isEmpty()) {
                        for (String rangeUri : range) {
                            RDFClass rangeItem = new RDFClass();
                            rangeItem.setUri(rangeUri);
                            documentPropertyClass.addRange(rangeItem);
                        }
                    }
                    if (!instances.isEmpty()) {
                        documentPropertyClass.setInstances(instances);
                    }
                    documentsSchemasUri.add(documentPropertyClass);
                }
        );

        return documentsSchemasUri;
    }
}
