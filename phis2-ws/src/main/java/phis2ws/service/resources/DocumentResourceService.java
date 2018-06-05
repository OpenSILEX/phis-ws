//**********************************************************************************************
//                                       DocumentResourceService.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2016
// Creation date: august 2016
// Contact:arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  March, 2018
// Subject: Represents the documents service
//***********************************************************************************************
package phis2ws.service.resources;

import com.jcraft.jsch.SftpException;
import com.twmacinta.util.MD5;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.PropertiesFileManager;
import phis2ws.service.authentication.Session;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.configuration.GlobalWebserviceValues;
import phis2ws.service.configuration.URINamespaces;
import phis2ws.service.dao.manager.DAOFactory;
import phis2ws.service.dao.mongo.DocumentDaoMongo;
import phis2ws.service.dao.sesame.DocumentDaoSesame;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.documentation.StatusCodeMsg;
import phis2ws.service.injection.SessionInject;
import phis2ws.service.resources.dto.DocumentMetadataDTO;
import phis2ws.service.resources.dto.DocumentMetadataSearchDTO;
import phis2ws.service.utils.DocumentWaitingCheck;
import phis2ws.service.utils.FileUploader;
import phis2ws.service.utils.POSTResultsReturn;
import phis2ws.service.utils.ResourcesUtils;
import phis2ws.service.view.brapi.Status;
import phis2ws.service.view.brapi.form.AbstractResultForm;
import phis2ws.service.view.brapi.form.ResponseFormDocumentMetadata;
import phis2ws.service.view.brapi.form.ResponseFormDocumentProperty;
import phis2ws.service.view.brapi.form.ResponseFormDocumentType;
import phis2ws.service.view.brapi.form.ResponseFormGET;
import phis2ws.service.view.brapi.form.ResponseFormPOST;
import phis2ws.service.view.model.phis.Document;
import phis2ws.service.view.model.phis.DocumentProperty;

@Api("/documents")
@Path("/documents")
public class DocumentResourceService {

    @Context
    UriInfo uri;

    @SessionInject
    Session userSession;

    final static Logger LOGGER = LoggerFactory.getLogger(DocumentResourceService.class);

    //manage waiting document metadata

    public final static ExecutorService threadPool = Executors.newCachedThreadPool();
    //two Maps which contains information about waiting document metadata
    public final static Map<String, Boolean> waitingAnnotFileCheck = new HashMap<>();
    public final static Map<String, DocumentMetadataDTO> waitingAnnotInformation = new HashMap<>();

    /**
     * Verify document metadata before save it
     *
     * @param headers request headers
     * @param documentsAnnotations documentsAnnotations json body
     * @return response contains if the metadata have been saved correctly
     */
    @POST
    @ApiOperation(value = "Save a file", notes = DocumentationAnnotation.USER_ONLY_NOTES)
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Metadata verified and correct", response = DocumentMetadataDTO.class, responseContainer = "List")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDocuments(@Context HttpHeaders headers,
            @ApiParam(value = "JSON Document metadata", required = true) List<DocumentMetadataDTO> documentsAnnotations) throws RepositoryException {
        AbstractResultForm postResponse;
        if (documentsAnnotations != null && !documentsAnnotations.isEmpty()) {
            //insert document process started
            DocumentDaoSesame documentDao = new DocumentDaoSesame();
            documentDao.user = userSession.getUser();
            //metadata verification
            final POSTResultsReturn checkAnnots = documentDao.check(documentsAnnotations);

            if (checkAnnots.statusList == null) { //metadata verification failed
                postResponse = new ResponseFormPOST();
            } else if (checkAnnots.getDataState()) { //metadata verification succeed
                List<String> uriList = new ArrayList<>();
                Iterator<DocumentMetadataDTO> itdocsMetadata = documentsAnnotations.iterator();
                while (itdocsMetadata.hasNext()) {
                    DocumentMetadataDTO docsM = itdocsMetadata.next();
                    //building URI
                    final UriBuilder uploadPath = uri.getBaseUriBuilder();
                    String name = new StringBuilder("document").append(ResourcesUtils.getUniqueID()).toString(); // docsM + idUni
                    final URINamespaces uriNS = new URINamespaces();
                    final String docsUri = uriNS.getContextsProperty("documents") + "/" + name;
                    final String uploadLink = uploadPath.path("documents").path("upload").queryParam("uri", docsUri).toString();
                    //add URI in list of document that will be sent
                    uriList.add(uploadLink);
                    waitingAnnotFileCheck.put(docsUri, false); //file waiting
                    waitingAnnotInformation.put(docsUri, docsM);
                    //start THREAD for this waiting file
                    threadPool.submit(new DocumentWaitingCheck(docsUri));
                }
                final Status waitingTimeStatus = new Status("Timeout", StatusCodeMsg.INFO, " Timeout :" + PropertiesFileManager.getConfigFileProperty("service", "waitingFileTime") + " seconds");
                checkAnnots.statusList.add(waitingTimeStatus);
                postResponse = new ResponseFormPOST(checkAnnots.statusList);
                postResponse.getMetadata().setDatafiles(uriList);
            } else {
                postResponse = new ResponseFormPOST(checkAnnots.statusList);
            }
            return Response.status(checkAnnots.getHttpStatus()).entity(postResponse).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseFormPOST()).build();
        }
    }

    /**
     * file uri to send
     *
     * @param in File
     * @param docUri annotation uri
     * @param headers request headers
     * @param request request object
     * @return return document uri if the file has been successfully sent or an
     * error message
     * @throws URISyntaxException
     */
    @POST
    @Path("upload")
    @ApiOperation(value = "Post data file", notes = DocumentationAnnotation.USER_ONLY_NOTES + " Not working from this documentation. Implement a client or use Postman application.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Document file and document metadata saved", response = ResponseFormPOST.class)
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDocumentFile(
            @ApiParam(value = "File to upload") File in,
            @ApiParam(value = "URI given from \"/documents\" path for upload") @QueryParam("uri") String docUri,
            @Context HttpHeaders headers,
            @Context HttpServletRequest request) throws URISyntaxException {
        ResponseFormPOST postResponse = null;
        List<Status> statusList = new ArrayList();

        //check annotation existence
        if (!waitingAnnotFileCheck.containsKey(docUri)) {
            statusList.add(new Status("No waiting file", "Error", "No waiting file for the following uri : " + docUri));
            postResponse = new ResponseFormPOST(statusList);
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }

        if (headers != null && headers.getLength() <= 0) {
            statusList.add(new Status("File error", "Error", "File Size : " + headers.getLength() + " octets"));
            postResponse = new ResponseFormPOST(statusList);
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }

        //verify md5 checksum 
        String hash = getHash(in);
        if (hash != null && !waitingAnnotInformation.get(docUri).getChecksum().equals(hash)) {
            statusList.add(new Status("MD5 error", "Error", "Checksum MD5 doesn't match. Corrupted File."));
            postResponse = new ResponseFormPOST(statusList);
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }

        String media = waitingAnnotInformation.get(docUri).getDocumentType();
        media = media.substring(media.lastIndexOf("#") + 1, media.length());
        FileUploader jsch = new FileUploader();
        try {
            waitingAnnotFileCheck.put(docUri, Boolean.TRUE); // Preparing file insertion
            LOGGER.debug(jsch.getSFTPWorkingDirectory() + "/" + media);
            jsch.getChannelSftp().cd(jsch.getSFTPWorkingDirectory());
        } catch (SftpException e) {
            statusList.add(new Status("SftException", StatusCodeMsg.ERR, e.getMessage()));
            LOGGER.error(e.getMessage(), e);
        }

        final String serverFileName = ResourcesUtils.getUniqueID() + "." + waitingAnnotInformation.get(docUri).getExtension();
        final String serverFilePath = jsch.getSFTPWorkingDirectory() + "/" + serverFileName;

        boolean fileTransfered = jsch.fileTransfer(in, serverFileName);
        jsch.closeConnection();

        if (!fileTransfered) { // Error during file writing
            statusList.add(new Status("File upload error", "Error", "Problem during file upload. Try to submit it again " + docUri));
            postResponse = new ResponseFormPOST(statusList);
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }

        waitingAnnotInformation.get(docUri).setServerFilePath(serverFilePath);
        DocumentDaoSesame documentsDao = DAOFactory.getSESAMEDAOFactory().getDocumentsDaoSesame();
        if (request.getRemoteAddr() != null) {
            documentsDao.remoteUserAdress = request.getRemoteAddr();
        }
        documentsDao.user = userSession.getUser();
        final POSTResultsReturn insertAnnotationJSON = documentsDao.insert(Arrays.asList(waitingAnnotInformation.get(docUri)));

        postResponse = new ResponseFormPOST(insertAnnotationJSON.statusList);

        if (insertAnnotationJSON.getDataState()) { // Json state
            waitingAnnotFileCheck.remove(docUri);
            waitingAnnotInformation.remove(docUri);
            if (insertAnnotationJSON.getHttpStatus() == Response.Status.CREATED) {
                postResponse.getMetadata().setDatafiles((ArrayList) insertAnnotationJSON.createdResources);
                final URI newUri = new URI(uri.getPath());
                return Response.status(insertAnnotationJSON.getHttpStatus()).location(newUri).entity(postResponse).build();
            } else {
                return Response.status(insertAnnotationJSON.getHttpStatus()).entity(postResponse).build();
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseFormPOST()).build();
    }

    private String getHash(File in) {
        String hash = null;
        try {
            hash = MD5.asHex(MD5.getHash(in)); // Ex : 106fa487baa1728083747de1c6df73e9
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return hash;
    }

    @GET
    @Path("types")
    @ApiOperation(value = "Get all documents types",
            notes = "Retrieve all documents types ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all documents type")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentsType(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int limit,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page) {
        DocumentDaoSesame documentsDao = new DocumentDaoSesame();
        Status errorStatus = null;
        try {
            ArrayList<String> documentCategories = documentsDao.getDocumentsTypes();
            if (documentCategories.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ResponseFormGET()).build();
            }
            return Response.status(Response.Status.OK).entity(new ResponseFormDocumentType(limit, page, documentCategories, false)).build();
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            errorStatus = new Status("Error", StatusCodeMsg.ERR, ex.getMessage());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseFormGET(errorStatus)).build();
    }

    /**
     *
     * @param limit
     * @param page
     * @param uri
     * @param documentType
     * @param creator
     * @param language
     * @param title
     * @param creationDate
     * @param extension
     * @param concernedItem
     * @param status
     * @return Request result
     */
    @GET
    @ApiOperation(value = "Get all documents metadata corresponding to the searched params given",
            notes = "Retrieve all documents authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all documents ", response = DocumentMetadataDTO.class, responseContainer = "List")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentsMetadataBySearch(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int limit,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page,
            @ApiParam(value = "Search by URI", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_URI) @QueryParam("uri") String uri,
            @ApiParam(value = "Search by document type", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_TYPE) @QueryParam("documentType") String documentType,
            @ApiParam(value = "Search by creator", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_CREATOR) @QueryParam("creator") String creator,
            @ApiParam(value = "Search by language", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_LANGUAGE) @QueryParam("language") String language,
            @ApiParam(value = "Search by title", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_TITLE) @QueryParam("title") String title,
            @ApiParam(value = "Search by creation date", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_CREATION_DATE) @QueryParam("creationDate") String creationDate,
            @ApiParam(value = "Search by extension", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_EXTENSION) @QueryParam("extension") String extension,
            @ApiParam(value = "Search by concerned item", example = DocumentationAnnotation.EXAMPLE_EXPERIMENT_URI) @QueryParam("concernedItem") String concernedItem,
            @ApiParam(value = "Search by status", example = DocumentationAnnotation.EXAMPLE_DOCUMENT_STATUS) @QueryParam("status") String status) {

        DocumentDaoSesame documentDao = new DocumentDaoSesame();

        if (uri != null) {
            documentDao.uri = uri;
        }
        if (documentType != null) {
            documentDao.documentType = documentType;
        }
        if (creator != null) {
            documentDao.creator = creator;
        }
        if (language != null) {
            documentDao.language = language;
        }
        if (title != null) {
            documentDao.title = title;
        }
        if (creationDate != null) {
            documentDao.creationDate = creationDate;
        }
        if (extension != null) {
            documentDao.format = extension;
        }
        if (concernedItem != null) {
            documentDao.concernedItemsUris.add(concernedItem);
        }
        if (status != null) {
            documentDao.status = status;
        }

        documentDao.user = userSession.getUser();
        documentDao.setPage(page);
        documentDao.setPageSize(limit);

        return getDocumentsMetadata(documentDao);
    }

    /**
     *
     * @param documentURI document uri to download
     * @return response with the document if it exists
     */
    @GET
    @Path("{documentURI}")
    @ApiOperation(value = "Get a document (by receiving it's uri)",
            notes = "Retrieve the document corresponding to the uri given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve document")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDocumentByUri(@ApiParam(value = DocumentationAnnotation.DOCUMENT_URI_DEFINITION, required = true, example = DocumentationAnnotation.EXAMPLE_DOCUMENT_URI) @PathParam("documentURI") String documentURI) {
        //SILEX:conception
        // it's will be better to send directly this inputstream to mongodb instead of save it
        // to a directory and after send it
        if (documentURI == null) {
            final Status status = new Status("Access error", StatusCodeMsg.ERR, "Empty document URI");
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseFormGET(status)).build();
        }
        //\SILEX:conception
        return getFile(documentURI);

    }

    /**
     * @action modify document metatada information according to metadata send
     * @param documentsMetadata document metdata
     * @param context repository context
     * @return Response result of the request
     */
    @PUT
    @ApiOperation(value = "Update document metadata")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Document's metadata updated", response = ResponseFormPOST.class)
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 404, message = "Document not found")
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putDocumentMetadata(
            @ApiParam(value = "Json document metadata") ArrayList<DocumentMetadataDTO> documentsMetadata,
            @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;

        if (documentsMetadata != null && !documentsMetadata.isEmpty()) {
            DocumentDaoSesame documentDaoSesame = new DocumentDaoSesame();
            if (documentDaoSesame.remoteUserAdress != null) {
                documentDaoSesame.remoteUserAdress = context.getRemoteAddr();
            }
            documentDaoSesame.user = userSession.getUser();

            //Vérification des données et update de la BD
            POSTResultsReturn result = documentDaoSesame.checkAndUpdateList(documentsMetadata);

            if (result.getHttpStatus().equals(Response.Status.OK)) { //200 users modifiés
                postResponse = new ResponseFormPOST(result.statusList);
                return Response.status(result.getHttpStatus()).entity(postResponse).build();
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                    || result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status("Request error", StatusCodeMsg.ERR, "Empty document(s) to update"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }

    private Response noResultFound(ResponseFormDocumentMetadata getResponse, ArrayList<Status> insertStatusList) {
        insertStatusList.add(new Status("No results", StatusCodeMsg.INFO, "No results for the documents"));
        getResponse.setStatus(insertStatusList);
        return Response.status(Response.Status.NOT_FOUND).entity(getResponse).build();
    }

    /**
     * Get metatada from a user search
     *
     * @param documentDao DocumentDaoSesame
     * @return response to the user. Contains metatada list used to get search
     * results
     */
    private Response getDocumentsMetadata(DocumentDaoSesame documentDao) {
        ArrayList<Document> documentsMetadata;
        ArrayList<Status> statusList = new ArrayList<>();
        ResponseFormDocumentMetadata getResponse;

        documentDao.user = userSession.getUser();
        documentsMetadata = documentDao.allPaginate();
        if (documentsMetadata == null) {
            getResponse = new ResponseFormDocumentMetadata(0, 0, documentsMetadata, true);
            return noResultFound(getResponse, statusList);
        } else if (!documentsMetadata.isEmpty()) {
            getResponse = new ResponseFormDocumentMetadata(documentDao.getPageSize(), documentDao.getPage(), documentsMetadata, false);
            if (getResponse.getResult().dataSize() == 0) {
                return noResultFound(getResponse, statusList);
            } else {
                getResponse.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(getResponse).build();
            }
        } else {
            getResponse = new ResponseFormDocumentMetadata(0, 0, documentsMetadata, true);
            return noResultFound(getResponse, statusList);
        }
    }

    /**
     *
     * @param documentURI document uri to download
     * @return Response with the document if it exists
     */
    private Response getFile(String documentURI) {
        DocumentDaoMongo documentDaoMongo = new DocumentDaoMongo();
        File file = documentDaoMongo.getDocument(documentURI);

        if (file == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachement; filename=\"" + file.getName() + "\"")
                    .build();
        }
    }

    /**
     * Verify searched metadata
     *
     * @param headers request headers
     * @param documentsAnnotations documentsAnnotations represents json body
     * part
     * @return
     */
    @POST
    @Path("search")
    @ApiOperation(value = "Search document", notes = DocumentationAnnotation.USER_ONLY_NOTES)
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Metadata  retreived", response = DocumentMetadataDTO.class, responseContainer = "List")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                defaultValue = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ", example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDocumentsMetadataBySearch(@Context HttpHeaders headers,
            @ApiParam(value = "JSON Document metadata", required = true) DocumentMetadataSearchDTO documentsAnnotations) throws RepositoryException {
        if (documentsAnnotations != null) {
            DocumentDaoSesame documentDao = new DocumentDaoSesame();

            if (documentsAnnotations.getUri() != null) {
                documentDao.uri = documentsAnnotations.getUri();
            }
            if (documentsAnnotations.getDocumentType() != null) {
                documentDao.documentType = documentsAnnotations.getDocumentType();
            }
            if (documentsAnnotations.getCreator() != null) {
                documentDao.creator = documentsAnnotations.getCreator();
            }
            if (documentsAnnotations.getLanguage() != null) {
                documentDao.language = documentsAnnotations.getLanguage();
            }
            if (documentsAnnotations.getTitle() != null) {
                documentDao.title = documentsAnnotations.getTitle();
            }
            if (documentsAnnotations.getCreationDate() != null) {
                documentDao.creationDate = documentsAnnotations.getCreationDate();
            }

            if (documentsAnnotations.getComment() != null) {
                documentDao.comment = documentsAnnotations.getComment();
            }

            if (documentsAnnotations.getExtension() != null) {
                documentDao.format = documentsAnnotations.getExtension();
            }

            if (documentsAnnotations.getEndDate() != null) {
                documentDao.endDate = documentsAnnotations.getEndDate();
            }

            if (documentsAnnotations.getStartDate() != null) {
                documentDao.startDate = documentsAnnotations.getStartDate();
            }

            if (documentsAnnotations.getConcern() != null && !documentsAnnotations.getConcern().isEmpty()) {
                documentDao.concernedItemsUris = documentsAnnotations.getConcern();
            }

            if (documentsAnnotations.getConcernedItemType() != null) {
                documentDao.concernedItemType = documentsAnnotations.getConcernedItemType();
            }

            if (documentsAnnotations.getAdditionnalProperties() != null) {
                documentDao.additionnalProperties = documentsAnnotations.getAdditionnalProperties();
            }

            documentDao.user = userSession.getUser();

            documentDao.setPage(documentsAnnotations.getPage());
            documentDao.setPageSize(documentsAnnotations.getPageSize());

            return getDocumentsMetadata(documentDao);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseFormPOST()).build();
        }
    }

    @GET
    @Path("subClassProperties")
    @ApiOperation(value = "Get a document sub class properties",
            notes = "Get a document sub class properties ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all document sub class properties",
                response = DocumentProperty.class, responseContainer = "List")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                defaultValue = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ", example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentSubClassProperties(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int limit,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page,
            @ApiParam(value = DocumentationAnnotation.DOCUMENT_URI_CLASS, defaultValue = DocumentationAnnotation.EXAMPLE_DOCUMENT_TYPE) @QueryParam("documentClass") String documentClass
    ) {
        DocumentDaoSesame documentsDao = new DocumentDaoSesame();
        Status errorStatus = null;
        try {
            ArrayList<DocumentProperty> documentProperties = documentsDao.getDocumentsProperties(documentClass);
            if (documentProperties.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ResponseFormGET()).build();
            }
            return Response.status(Response.Status.OK).entity(new ResponseFormDocumentProperty(limit, page, documentProperties, false)).build();
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            errorStatus = new Status("Error", StatusCodeMsg.ERR, ex.getMessage());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseFormGET(errorStatus)).build();
    }

    @GET
    @Path("subClassProperty")
    @ApiOperation(value = "Get a document sub class property information",
            notes = "Get a document class sub property information")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all information about a specific document class sub property",
                response = DocumentProperty.class, responseContainer = "List")
        ,
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION)
        ,
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED)
        ,
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                dataType = "string", paramType = "header",
                value = DocumentationAnnotation.ACCES_TOKEN,
                defaultValue = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ", example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentSubClassProperty(
            @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) int limit,
            @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) int page,
            @ApiParam(value = DocumentationAnnotation.DOCUMENT_URI_PROPERTY, defaultValue = DocumentationAnnotation.EXAMPLE_DOCUMENT_PROPERTY) @QueryParam("documentProperty") String documentProperty,
            @QueryParam("getInstances") Boolean instanceBoolean // if true return instances linked to this property
    ) {
        DocumentDaoSesame documentsDao = new DocumentDaoSesame();
        Status errorStatus = null;
        try {
            ArrayList<DocumentProperty> documentProperties = documentsDao.getDocumentsProperty(documentProperty, instanceBoolean);
            if (documentProperties.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ResponseFormGET()).build();
            }
            return Response.status(Response.Status.OK).entity(new ResponseFormDocumentProperty(limit, page, documentProperties, false)).build();
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            errorStatus = new Status("Error", StatusCodeMsg.ERR, ex.getMessage());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ResponseFormGET(errorStatus)).build();
    }
}
