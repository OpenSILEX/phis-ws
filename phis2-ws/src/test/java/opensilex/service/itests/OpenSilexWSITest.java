//******************************************************************************
//                                DataSourceWSIT.java
// SILEX-PHIS
// Copyright © INRA 2020
// Creation date: Jan 27, 2020
// Contact: Expression userEmail is undefined on line 6, column 15 in file:///home/training/opensilex/phis-ws/phis2-ws/licenseheader.txt., anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.itests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.authentication.TokenResponseStructure;
import opensilex.service.model.ContactPostgreSQL;
import opensilex.service.model.Data;
import opensilex.service.model.Experiment;
import opensilex.service.model.Group;
import opensilex.service.model.Project;
import opensilex.service.model.ScientificObject;
import opensilex.service.model.User;
import opensilex.service.model.Variable;
import opensilex.service.resource.dto.ConcernedItemDTO;
import opensilex.service.resource.dto.MethodDTO;
import opensilex.service.resource.dto.TokenDTO;
import opensilex.service.resource.dto.TraitDTO;
import opensilex.service.resource.dto.UnitDTO;
import opensilex.service.resource.dto.UserDTO;
import opensilex.service.resource.dto.VariableDTO;
import opensilex.service.resource.dto.data.DataPostDTO;
import opensilex.service.resource.dto.data.FileDescriptionPostDTO;
import opensilex.service.resource.dto.experiment.ExperimentPostDTO;
import opensilex.service.resource.dto.group.GroupPostDTO;
import opensilex.service.resource.dto.project.ProjectPostDTO;
import opensilex.service.resource.dto.provenance.ProvenancePostDTO;
import opensilex.service.resource.dto.rdfResourceDefinition.PropertyPostDTO;
import opensilex.service.resource.dto.scientificObject.ScientificObjectPostDTO;
import opensilex.service.result.Result;
import opensilex.service.result.ResultForm;
import opensilex.service.view.brapi.Metadata;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 *
 * @author training
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OpenSilexWSITest extends InternalProviderIntegrationTestHelper {

    private static final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy_MM_dd__HH-mm-ss");
    private static final Long itTimeMillis = System.currentTimeMillis();
    private static final Timestamp startTimestamp = new Timestamp(itTimeMillis);
    private static final String timeStamp = dateFormater.format(startTimestamp);

    private static final String emailUser = "it-user_" + timeStamp + "@opensilex.org";
    private static final String firstNameUser = timeStamp + "_firstName_IT_User"; // in order to make the generated uri unique 
    private static final String passwordUser = "azerty";

    private static final String groupName = "group_IT_" + timeStamp;

    private static final String projectName = "Project_IT_" + timeStamp;
    private static final String projectShortName = "Proj_" + timeStamp;

    private static final String experimentCampain = "campain_IT_" + timeStamp;

    private static final String scientifiObjectLabel = "so_IT_" + timeStamp;

    private static final String traitLabel = "trait_" + timeStamp;

    private static final String methodLabel = "method_" + timeStamp;

    private static final String unitLabel = "unit_" + timeStamp;

    private static final String variableLabel = "variable_" + timeStamp;

    private static final String provenanceLabel = "provenance_" + timeStamp;

    private static final int datasetSize = 10;
    private static final int nbImages = 5;

    private static final String contactType_SCIENTIFIC_CONTACT = "http://www.opensilex.org/vocabulary/oeso/#ScientificContact"; // #ScientificSupervisor
    private static final String contactType_PROJECT_COORDINATOR = "http://www.opensilex.org/vocabulary/oeso/#ProjectCoordinator"; // 
    private static final String contactType_ADMINISTRATIVE_CONTACT = "http://www.opensilex.org/vocabulary/oeso/#AdministrativeContact";// 
    private static final String scientificObjectRdfType_PLANT = "http://www.opensilex.org/vocabulary/oeso#Plant";
    private static final String scientificObjectRdfType_PLOT = "http://www.opensilex.org/vocabulary/oeso#Plot";
    private static final String imageHemisphericalImageRdfType = "http://www.opensilex.org/vocabulary/oeso#HemisphericalImage";

    private static String TOKKEN;
    private static String itUserURI;
    private static String itGroupURI;
    private static String itProjectURI;//that will be generated in Itest _1_02_groups_01_POST_ITest
    private static String itExperimentURI;//that will be generated in Itest _3_02_experiments_01_POST_ITest
    private static String itScientificObjectURI;//that will be generated in Itest _4_02_scientificObject_01_POST_ITest
    private static String itTraitURI;//that will be generated in Itest 
    private static String itMethodURI;//that will be generated in Itest
    private static String itUnitURI;//that will be generated in Itest
    private static String itVariableURI;//that will be generated in Itest 
    private static String itProvenanceURI;//that will be generated in Itest 
    private static String itDatasetURI;//that will be generated in Itest    
    private static ArrayList<String> itImageURIs = new ArrayList<>();

    private static String itImageURI;//that will be generated in Itest 

    public ArrayList<Object> parseResultToObject(Result r, Class classs) {

        ArrayList<LinkedTreeMap> data = (ArrayList<LinkedTreeMap>) r.getData();
        ArrayList<Object> listObjects = new ArrayList<>();
        Gson gson = new Gson();
        for (LinkedTreeMap d : data) {
            JsonObject jsonObject = gson.toJsonTree(d).getAsJsonObject();
            String s = jsonObject.toString();
            ObjectMapper mapper = new ObjectMapper();
            Object obj = null;
            try {
                obj = mapper.readValue(s, classs);
                listObjects.add(obj);

            } catch (IOException ex) {
                Logger.getLogger(OpenSilexWSITest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return listObjects;
    }

    public void getToken() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setGrant_type("password");
        tokenDTO.setClient_id("123");
        tokenDTO.setUsername("admin@opensilex.org");
        tokenDTO.setPassword("21232f297a57a5a743894a0e4a801fc3");

        Entity<TokenDTO> tokenEntity = Entity.entity(tokenDTO, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/brapi/v1/token")
                .request()
                .post(tokenEntity);

        TokenResponseStructure token = response.readEntity(TokenResponseStructure.class);
        String accessToken = token.getAccess_token();

        if (!accessToken.equals("")) {
            this.TOKKEN = accessToken;
        } else {
            this.TOKKEN = "";
        }

    }

    @Test
    public void _0_01_getToken_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        this.getToken();
        System.out.println("-----------Token: " + this.TOKKEN);

        assertTrue("Token must be setted", this.TOKKEN.equals("")); // ITest should fail

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _1_01_users_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<UserDTO> users = new ArrayList<>();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(emailUser);

        String passwordITUser = passwordUser;
        String pwdHash = DigestUtils.md5Hex(passwordITUser);
        userDTO.setPassword(pwdHash);

        userDTO.setFirstName(firstNameUser);
        userDTO.setFamilyName("FamilyName_IT_User");
        userDTO.setAddress("address_IT_User");
        userDTO.setPhone("000000000");
        userDTO.setAffiliation("affiliation_IT_User");
        userDTO.setOrcid(null);
        userDTO.setAdmin("true");
        userDTO.setGroupsUris(new ArrayList<>());

        users.add(userDTO);
        Entity< ArrayList<UserDTO>> usersEntity = Entity.entity(users, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/users")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(usersEntity);

        System.out.println("--------------- Response: \n" + response);

        Integer statusCode = response.getStatus();
        assertTrue("Status code should be 201 (Created)", statusCode == 201);

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _1_01_users_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/users")
                .queryParam("email", emailUser)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, User.class);
        User usr = (User) usersList.get(0);
        String uriUser = usr.getUri();

        itUserURI = uriUser;

        assertTrue("Uri of the user should contain firstNameUser ", uriUser.contains(firstNameUser.toLowerCase()));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _1_02_groups_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<GroupPostDTO> groups = new ArrayList<>();
        GroupPostDTO groupDTO = new GroupPostDTO();

        groupDTO.setName(groupName);
        groupDTO.setLevel("Owner");
        groupDTO.setDescription("Description of group_IT_" + timeStamp);

        ArrayList<String> usersEmails = new ArrayList<>();
        usersEmails.add(emailUser);
        groupDTO.setUsersEmails(usersEmails);

        groups.add(groupDTO);
        Entity< ArrayList<GroupPostDTO>> groupEntity = Entity.entity(groups, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/groups")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(groupEntity);

        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();

        String uriGroup = dataFiles.get(0);
        assertTrue("URI of created group should contains groupName", (uriGroup.toLowerCase()).contains(groupName.toLowerCase()));

        Boolean contains = StringUtils.containsIgnoreCase(uriGroup, groupName);
        assertTrue("URI of created group should contains groupName", StringUtils.containsIgnoreCase(uriGroup, groupName));

        itGroupURI = uriGroup;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _1_02_groups_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/groups")
                .queryParam("name", groupName)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, Group.class);
        Group grp = (Group) usersList.get(0);
        String uriGrp = grp.getUri();

        assertTrue("Uri of the group should be the same as the one returned when it was created", uriGrp.equalsIgnoreCase(itGroupURI.toLowerCase()));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _2_projects_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<ProjectPostDTO> projects = new ArrayList<>();
        ProjectPostDTO projectDTO = new ProjectPostDTO();

        projectDTO.setName(projectName);
        projectDTO.setShortname(projectShortName);
//        projectDTO.setFinancialFunding("financial reference");
        projectDTO.setFinancialReference("financial reference");
        projectDTO.setDescription("This project is about ...");
        projectDTO.setStartDate("2015-07-07");
        projectDTO.setEndDate("2016-07-07");
        projectDTO.setKeywords(new ArrayList<>());
        projectDTO.setHomePage("http://example.com");

        //SCIENTIFIC_CONTACT
        projectDTO.setScientificContacts(Arrays.asList(new String[]{itUserURI}));

        //PROJECT_COORDINATOR
        projectDTO.setCoordinators(Arrays.asList(new String[]{itUserURI}));

        //ADMINISTRATIVE_CONTACT
        projectDTO.setAdministrativeContacts(Arrays.asList(new String[]{itUserURI}));

        projects.add(projectDTO);

        Entity< ArrayList<ProjectPostDTO>> groupEntity = Entity.entity(projects, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/projects")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(groupEntity);

        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedProject = dataFiles.get(0);

        assertTrue("URI of created project should contains projectShortName", StringUtils.containsIgnoreCase(uriGeneratedProject, projectShortName));

        itProjectURI = uriGeneratedProject;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _2_projects_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/projects")
                .queryParam("name", projectName)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, Project.class);
        Project proj = (Project) usersList.get(0);
        String uriProj = proj.getUri();

        assertTrue("URI of returned project should be the same as the one returned when it was created", StringUtils.equalsIgnoreCase(uriProj, itProjectURI));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _3_experiments_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<ExperimentPostDTO> experiments = new ArrayList<>();
        ExperimentPostDTO experimentDTO = new ExperimentPostDTO();

        experimentDTO.setAlias("XP_IT_" + timeStamp);
        experimentDTO.setStartDate("2015-07-07");
        experimentDTO.setEndDate("2015-08-07");
        experimentDTO.setField("field");
        experimentDTO.setCampaign(experimentCampain);
        experimentDTO.setPlace("place");
        experimentDTO.setComment("Comment IT_experiment_" + timeStamp);
        experimentDTO.setKeywords("keyword1 keyword2");
        experimentDTO.setObjective("Objective experiement_IT_" + timeStamp);
        experimentDTO.setCropSpecies("maize");

//        ArrayList<String> projectsUris = new ArrayList<String>();
//        projectsUris.add(itProjectURI);
        experimentDTO.setProjectsUris(new ArrayList<String>() {
            {
                add(itProjectURI);
            }
        });

        //SCIENTIFIC_CONTACT
        ArrayList<ContactPostgreSQL> contacts = new ArrayList<>();

        ContactPostgreSQL contact = new ContactPostgreSQL();
        contact.setEmail(emailUser);
        contact.setType(contactType_SCIENTIFIC_CONTACT);
        contacts.add(contact);

        //PROJECT_COORDINATOR
        ContactPostgreSQL projectCoordinator = new ContactPostgreSQL();
        projectCoordinator.setEmail(emailUser);
        projectCoordinator.setType(contactType_PROJECT_COORDINATOR);
        contacts.add(projectCoordinator);

        //ADMINISTRATIVE_CONTACT
        ContactPostgreSQL administrativeContact = new ContactPostgreSQL();
        administrativeContact.setEmail(emailUser);
        administrativeContact.setType(contactType_ADMINISTRATIVE_CONTACT);
        contacts.add(administrativeContact);

        experimentDTO.setContacts(contacts);
        experiments.add(experimentDTO);

        Entity< ArrayList<ExperimentPostDTO>> groupEntity = Entity.entity(experiments, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/experiments")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(groupEntity);

        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedExperiment = dataFiles.get(0);

        assertTrue("URI of created experiment should contains experimentCampain", StringUtils.containsIgnoreCase(uriGeneratedExperiment, experimentCampain));

        itExperimentURI = uriGeneratedExperiment;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _3_experiments_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/experiments")
                .queryParam("uri", itExperimentURI)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, Experiment.class);
        Experiment exp = (Experiment) usersList.get(0);
        String uriExp = exp.getUri();

        assertTrue("URI of returned experiment should be the same as the one returned when it was created", StringUtils.equalsIgnoreCase(uriExp, itExperimentURI));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _4_scientificOject_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<ScientificObjectPostDTO> scientificObjects = new ArrayList<>();
        ScientificObjectPostDTO scientificObjectPostDTO = new ScientificObjectPostDTO();

        ArrayList<PropertyPostDTO> properties = new ArrayList<PropertyPostDTO>();
        PropertyPostDTO propertyScientificObjectLabel = new PropertyPostDTO();
        propertyScientificObjectLabel.setRelation("http://www.w3.org/2000/01/rdf-schema#label");
        propertyScientificObjectLabel.setValue("alias_so_" + timeStamp);

        properties.add(propertyScientificObjectLabel);

        scientificObjectPostDTO.setProperties(properties);

        scientificObjectPostDTO.setExperiment(itExperimentURI);
        scientificObjectPostDTO.setRdfType(scientificObjectRdfType_PLOT);

        scientificObjects.add(scientificObjectPostDTO);
        Entity< ArrayList<ScientificObjectPostDTO>> scientificObjectsEntity = Entity.entity(scientificObjects, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/scientificObjects")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(scientificObjectsEntity);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();
        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedSO = dataFiles.get(0);

        assertTrue("URI of created scientificObject should contains /o", StringUtils.containsIgnoreCase(uriGeneratedSO, "/o"));

        itScientificObjectURI = uriGeneratedSO;

        postTestCaseTrace(nameofCurrMethod);
    }

    @Test
    public void _4_scientificOject_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response responseScientificObjects = target("/scientificObjects")
                .queryParam("uri", itScientificObjectURI)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();

        ResultForm resultForm = responseScientificObjects.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, ScientificObject.class);
        ScientificObject so = (ScientificObject) usersList.get(0);
        String uriScientificObject = so.getUri();

        assertTrue("URI of returned scientificObject should be the same as the one returned when it was created", StringUtils.equalsIgnoreCase(uriScientificObject, itScientificObjectURI));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_01_trait_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<TraitDTO> traits = new ArrayList<>();
        TraitDTO traitDTO = new TraitDTO();
        traitDTO.setLabel(traitLabel);
        traitDTO.setComment("comment trait " + timeStamp);

        traits.add(traitDTO);
        Entity< ArrayList<TraitDTO>> traitsEntity = Entity.entity(traits, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/traits")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(traitsEntity);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedTrait = dataFiles.get(0);

        assertTrue("URI of created trait should contains ", StringUtils.containsIgnoreCase(uriGeneratedTrait, "/traits/t"));

        itTraitURI = uriGeneratedTrait;

        postTestCaseTrace(nameofCurrMethod);
    }

    @Test
    public void _5_01_trait_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_02_method_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<MethodDTO> methods = new ArrayList<>();
        MethodDTO methodDTO = new MethodDTO();
        methodDTO.setLabel(methodLabel);
        methodDTO.setComment("comment method " + timeStamp);

        methods.add(methodDTO);
        Entity< ArrayList<MethodDTO>> methodsEntity = Entity.entity(methods, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/methods")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(methodsEntity);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedMethod = dataFiles.get(0);

        assertTrue("URI of created method should contains ", StringUtils.containsIgnoreCase(uriGeneratedMethod, "/methods/m"));

        itMethodURI = uriGeneratedMethod;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_02_method_02_GET_ITest() {
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_03_unit_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<UnitDTO> units = new ArrayList<>();
        UnitDTO unitDTO = new UnitDTO();
        unitDTO.setLabel(unitLabel);
        unitDTO.setComment("comment unit " + timeStamp);

        units.add(unitDTO);
        Entity< ArrayList<UnitDTO>> unitsEntity = Entity.entity(units, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/units")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(unitsEntity);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedUnit = dataFiles.get(0);

        assertTrue("URI of created unit should contains /units/u", StringUtils.containsIgnoreCase(uriGeneratedUnit, "/units/u"));

        itUnitURI = uriGeneratedUnit;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_03_unit_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_04_variables_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<VariableDTO> variables = new ArrayList<>();
        VariableDTO variableDTO = new VariableDTO();
        variableDTO.setLabel(variableLabel);
        variableDTO.setComment("comment variable " + timeStamp);
        variableDTO.setTrait(itTraitURI);
        variableDTO.setMethod(itMethodURI);
        variableDTO.setUnit(itUnitURI);

        variables.add(variableDTO);
        Entity< ArrayList<VariableDTO>> variablesEntity = Entity.entity(variables, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/variables")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(variablesEntity);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedVariable = dataFiles.get(0);

        assertTrue("URI of created variable should contains /variables/v", StringUtils.containsIgnoreCase(uriGeneratedVariable, "/variables/v"));

        itVariableURI = uriGeneratedVariable;

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _5_04_variables_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/variables")
                .queryParam("uri", itVariableURI)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();

        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);
        Result result = resultForm.getResult();

        ArrayList<Object> usersList = parseResultToObject(result, Variable.class);
        Variable variable = (Variable) usersList.get(0);
        String uriVariable = variable.getUri();

        assertTrue("URI of returned variable should be the same as the one returned when it was created", StringUtils.equalsIgnoreCase(uriVariable, itVariableURI));

        postTestCaseTrace(nameofCurrMethod);

    }
    /*
    @Test
    public void _6_01_provenance_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<ProvenancePostDTO> provenances = new ArrayList<>();
        ProvenancePostDTO provenancePostDTO = new ProvenancePostDTO();

        provenancePostDTO.setLabel(provenanceLabel);
        provenancePostDTO.setComment("comment provenance " + timeStamp);

        provenances.add(provenancePostDTO);
        Entity< ArrayList<ProvenancePostDTO>> provenancesEntity = Entity.entity(provenances, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/provenances")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(provenancesEntity);
        
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        String uriGeneratedProvenance = dataFiles.get(0);

        assertTrue("URI of created provenance should contains /provenance/", StringUtils.containsIgnoreCase(uriGeneratedProvenance, "/provenance/"));

        itProvenanceURI = uriGeneratedProvenance;

        postTestCaseTrace(nameofCurrMethod);
    }

    @Test
    public void _6_01_provenance_02_GET_ITest() {

    }

    @Test
    public void _6_02_dataset_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        ArrayList<DataPostDTO> data = new ArrayList<>();

        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0200");

        for (int i = 0; i < datasetSize; i++) {
            Timestamp timestamp = new Timestamp(itTimeMillis + i * 1000);
            String timeStampStr = dateFormater.format(timestamp);

            DataPostDTO dataPostDTO = new DataPostDTO();
            dataPostDTO.setProvenanceUri(itProvenanceURI);
            dataPostDTO.setObjectUri(itScientificObjectURI);
            dataPostDTO.setVariableUri(itVariableURI);
            dataPostDTO.setValue(10 * Math.random());
            dataPostDTO.setDate(timeStampStr);

            data.add(dataPostDTO);

        }

        Entity< ArrayList<DataPostDTO>> dataEntity = Entity.entity(data, MediaType.APPLICATION_JSON_TYPE);

        Response response = target("/data")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .post(dataEntity);
        
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Metadata metadata = resultForm.getMetadata();
        List<String> dataFiles = metadata.getDatafiles();
        int dataFilesSize = dataFiles.size();

        assertTrue("dataFilesSize should equals to datasetSize", dataFilesSize == datasetSize);

        postTestCaseTrace(nameofCurrMethod);
    }

    @Test
    public void _6_02_dataset_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/data")
                .queryParam("variable", itVariableURI)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();

        ArrayList<Object> dataList = parseResultToObject(result, Data.class);
        Data data0 = (Data) dataList.get(0);
        String provenanceUriData0 = data0.getProvenanceUri();

        assertTrue("provenanceUri of the first data should be equals to itProvenanceUri", StringUtils.equalsIgnoreCase(provenanceUriData0, itProvenanceURI));

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _7_image_01_POST_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        FileDescriptionPostDTO fileDescriptionPostDTO = new FileDescriptionPostDTO();
        fileDescriptionPostDTO.setRdfType(imageHemisphericalImageRdfType);
        fileDescriptionPostDTO.setDate("2020-02-14T12:31:59+0200");
        fileDescriptionPostDTO.setProvenanceUri(itProvenanceURI);

        ArrayList<ConcernedItemDTO> concernedItems = new ArrayList<>();

        ConcernedItemDTO concernedItem = new ConcernedItemDTO();
        concernedItem.setUri(itScientificObjectURI);
        concernedItem.setTypeURI(scientificObjectRdfType_PLOT);

        concernedItems.add(concernedItem);

        fileDescriptionPostDTO.setConcernedItems(concernedItems);

        ObjectMapper mapper = new ObjectMapper();
        String fileDescriptionStr = null;
        try {
            fileDescriptionStr = mapper.writeValueAsString(fileDescriptionPostDTO);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(OpenSilexWSITest.class.getName()).log(Level.SEVERE, null, ex);
        }

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("images/image.jpg").getFile());
        FileDataBodyPart filePart = new FileDataBodyPart("file", file);
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart
                .field("description", fileDescriptionStr)
                .bodyPart(filePart);

        for (int i = 0; i < nbImages; i++) {
            Response response = target("/data/file")
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                    .post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA));
            
            System.out.println("--------------- Response: \n" + response);

            ResultForm resultForm = response.readEntity(ResultForm.class);

            Metadata metadata = resultForm.getMetadata();
            List<String> dataFiles = metadata.getDatafiles();
            String uriInsertedImage = dataFiles.get(0);

            assertTrue("URI of created image should contains /id/dataFile/", StringUtils.containsIgnoreCase(uriInsertedImage, "/id/dataFile/"));

            itImageURIs.add(uriInsertedImage);
        }

        postTestCaseTrace(nameofCurrMethod);

    }

    @Test
    public void _7_image_02_GET_ITest() {

        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        preTestCaseTrace(nameofCurrMethod);

        Response response = target("/data/file/search")
                .queryParam("rdfType", imageHemisphericalImageRdfType)
                .queryParam("provenance", itProvenanceURI)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.TOKKEN)
                .get();
        
        System.out.println("--------------- Response: \n" + response);

        ResultForm resultForm = response.readEntity(ResultForm.class);

        Result result = resultForm.getResult();
        int resultDataSize = result.getData().size();

        assertTrue("resultDataSize should = nbImages", resultDataSize == nbImages);

        postTestCaseTrace(nameofCurrMethod);

    }
    */

}

