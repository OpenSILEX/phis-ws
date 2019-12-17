//******************************************************************************
//                            UriGeneratorTest.java
// SILEX-PHIS
// Copyright © INRAE 2019
// Creation date: 17 December 2019
// Contact: renaud.colin@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************

package opensilex.service.utils;

import opensilex.service.dao.AnnotationDAO;
import opensilex.service.dao.EventDAO;
import opensilex.service.dao.RadiometricTargetDAO;
import opensilex.service.dao.UserDAO;
import opensilex.service.dao.manager.Rdf4jDAOTest;
import opensilex.service.model.*;
import opensilex.service.ontology.Oa;
import opensilex.service.ontology.Oeev;
import opensilex.service.ontology.Time;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This class check that for any object URI generated with a method from {@link UriGenerator}
 * the URI(s) are unique and not empty.
 * @author renaud.colin@inra.fr
 */

class UriGeneratorTest extends Rdf4jDAOTest {

    private static String userUri;
    private static UserDAO userDao;

    @BeforeAll
    public static void setUp() throws Exception {
        userDao = new UserDAO();
        userUri = "http://www.opensilex.org/demo/id/agent/admin_phis";
    }

    /**
     * Check if all generated URI(s) are unique and non empty
     *
     * @param uris : a list of URI
     * @param objects : a list of Object
     */
    protected void checkUris(Set<String> uris, Collection<?> objects){

        // check that each target uri has been filled
        uris.forEach( uri -> assertFalse(StringUtils.isEmpty( uri ) ));
        // check that all uris are uniques
        // if the size of the collections are not equals, then some URI(s) has not been added to to the URI set
        assertEquals(uris.size(),objects.size());
    }

    @Test
    public void testRadiometricTargetGenerate() throws Exception {

        RadiometricTargetDAO radioDao = new RadiometricTargetDAO(userDao.findById(userUri));
        initDaoWithInMemoryStoreConnection(radioDao);

        RadiometricTarget t1 = new RadiometricTarget();
        t1.setLabel("rt0");

        radioDao.checkAndInsert(Arrays.asList(t1));
        assertFalse(StringUtils.isEmpty(t1.getUri()));

        List<RadiometricTarget> targets = new ArrayList<>();
        for(int i=1 ;i<5 ; i++){
            RadiometricTarget target = new RadiometricTarget();
            target.setLabel("rt"+i);
            targets.add(target);
        }

        radioDao.checkAndInsert(targets);
        Set<String> uris = targets.stream().map(RdfResourceDefinition::getUri).collect(Collectors.toSet());
        checkUris(uris,targets);
    }

    @Test
    public void testEventGenerate() throws Exception{

        Project testProject = createAndGetProject();
        Experiment xp = createAndGetExperiment(testProject);
        ScientificObject so = createAndGetScientificObject(xp);

        int nbEvent = 5;
        List<Event> events = new ArrayList<>(nbEvent);
        for (int i = 0; i < nbEvent; i++) {
            Event event = new Event(null, Oeev.Event.getURI(),Arrays.asList(so.getUri()),new DateTime(), new ArrayList<>(1),new ArrayList<>(1));
            events.add(event);
        }
        EventDAO eventDAO = new EventDAO(userDao.findById(userUri));
        eventDAO.create(events);

        Set<String> uris = events.stream().map(RdfResourceDefinition::getUri).collect(Collectors.toSet());
        checkUris(uris,events);
    }

    @Test
    public void testAnnotationGenerate() throws Exception{

        Project testProject = createAndGetProject();
        Experiment xp = createAndGetExperiment(testProject);
        ScientificObject so = createAndGetScientificObject(xp);
        Event event = createAndGetEvent(so.getUri());

        int nbAnnotation = 5;
        List<Annotation> annotations = new ArrayList<>(nbAnnotation);

        ArrayList<String> bodyValues = new ArrayList<>();
        bodyValues.add("annotate an event");
        ArrayList<String> annotationTargetList = new ArrayList<>();
        annotationTargetList.add(event.getUri());

        for (int i = 0; i < nbAnnotation; i++) {
            Annotation annotation =  new Annotation(null,DateTime.now(),userUri,bodyValues, Oa.INSTANCE_DESCRIBING.toString(),annotationTargetList);
            annotations.add(annotation);
        }
        AnnotationDAO annotationDAO = new AnnotationDAO(userDao.findById(userUri));
        annotationDAO.create(annotations);

        Set<String> uris = annotations.stream().map(Annotation::getUri).collect(Collectors.toSet());
        checkUris(uris,annotations);
    }

    @Test
    public void testInstantGenerate() throws Exception{

        int nbInstant = 5;

        Set<String> uris = new HashSet<>();
        for (int i = 0; i < nbInstant; i++) {
            String instantUri = UriGenerator.generateNewInstanceUri(Time.Instant.toString(), null, null);
            uris.add(instantUri);
        }

        // if the size are not equals, then it's means that there exist one duplicate URI, which is a fail
        assertEquals(uris.size(),nbInstant);
    }

}
