//******************************************************************************
//                                AnnotationDAOTest.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 22 oct. 2019
// Contact: renaud.colin@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************


package opensilex.service.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.manager.Rdf4jDAO;
import opensilex.service.dao.manager.Rdf4jDAOTest;
import opensilex.service.model.Annotation;
import opensilex.service.model.Event;
import opensilex.service.model.Experiment;
import opensilex.service.model.Project;
import opensilex.service.model.ScientificObject;
import opensilex.service.ontology.Oa;

public class AnnotationDAOTest extends Rdf4jDAOTest {
	
	/**
	 * The {@link Rdf4jDAO} to test
	 */
	private static AnnotationDAO annotationDao;
	private static List<Event> events;
	private static ScientificObject so;
	private static String userUri;

	@BeforeAll
	public static void setup() throws DAOPersistenceException, Exception {
		
		UserDAO userDao = new UserDAO();
		userUri = "http://www.opensilex.org/demo/id/agent/admin_phis"; 
		
		annotationDao =  new AnnotationDAO(userDao.findById(userUri));
		initDaoWithInMemoryStoreConnection(annotationDao);
		events = new LinkedList<>();
	}
		
	@BeforeEach
	protected void initDao() throws DAOPersistenceException, Exception {
		
		Project createdProject = createAndGetProject();
		Experiment xp = createAndGetExperiment(createdProject);
		so = createAndGetScientificObject(xp);
		events.clear();
		
		events.add(createAndGetEvent(so.getUri()));
	}
	
	
	@Test
	/**
	 * Try to delete an annotation about one event .
	 * So the event annotation should be deleted 
	 */
	void testDeleteAnnotation() throws DAOPersistenceException, Exception {
		
		long initialSize = annotationDao.getConnection().size();	
		
		Annotation a = createAndGetAnnotation(userUri,events.get(0).getUri());
		annotationDao.delete(Arrays.asList(a));
		
		assertEquals(annotationDao.getConnection().size(), initialSize);  
		assertFalse(annotationDao.existUri(a.getUri()));
	}
	
	@Test
	/**
	 * Try to delete an annotation about one event. This annotation also have severals annotation. 
	 * So the event annotation should not be deleted 
	 */
	void testDeleteAnnotation_with_one_super_annotation() throws DAOPersistenceException, Exception {
		
		long initialSize = annotationDao.getConnection().size();

		Annotation a  = createAndGetAnnotation(userUri,events.get(0).getUri());		
		Annotation a1 = createAndGetAnnotation(userUri,a.getUri()); // create annotation on the last created annotation
		
		annotationDao.delete(Arrays.asList(a));
		assertEquals(annotationDao.getConnection().size(), initialSize);  
		assertFalse(annotationDao.existUri(a.getUri()));
		assertFalse(annotationDao.existUri(a1.getUri()));
	}
	
	@Test
	/**
	 * Try to delete an annotation about one event. This annotation is annotated 
	 * by a annotation a1 which also have an annotation.
	 * A simple RDF representation would be 
	 * <annotation1,oa:target,event_1>, <annotation2,oa:target,annotation1>, ... , <annotation_k,oa:target,annotation_k-1>
	 *
	 * 
	 */
	void testDeleteAnnotationWithRecursiveAnnotationChain() throws DAOPersistenceException, Exception {
		
		long initialSize = annotationDao.getConnection().size();

		int k = 2;
		String userUri = "http://www.opensilex.org/demo/id/agent/admin_phis";
		
		ArrayList<String> bodyValues = new ArrayList<>();
		bodyValues.add("annotate an event");
		ArrayList<String> targets = new ArrayList<>();
		targets.add(events.get(0).getUri());
		
		Annotation a = new Annotation(null,DateTime.now(),userUri,bodyValues,Oa.INSTANCE_DESCRIBING.toString(),targets);		
		
		List<Annotation> annotationList = new ArrayList<>();
		annotationList.addAll(annotationDao.create(Arrays.asList(a))); // create the first annotation A and add it
		
		int nbTripleCreated = 6;
		for(int i=1;i<k+1;i++) {
			
			bodyValues.clear();
			bodyValues.add("annotate an annotation "+i);			
			targets.clear();
			targets.add(annotationList.get(i-1).getUri());
			
			Annotation ai = new Annotation(null,DateTime.now(),userUri,bodyValues,Oa.INSTANCE_DESCRIBING.toString(),targets);			
			annotationList.addAll(annotationDao.create(Arrays.asList(ai))); // add the created annotation to the list
			nbTripleCreated += 6;
			assertEquals(annotationDao.getConnection().size(), initialSize+nbTripleCreated); // we should have delete 6 triples for each k annotation	
		}
		annotationDao.delete(Arrays.asList(annotationList.get(0)));
		assertEquals(annotationDao.getConnection().size(), initialSize); // we should have delete 6 triples for each k annotation
		
	}
	
	@Test
	/**
	 * Try to delete an annotation "a" on an event "e".
	 * Then add an annotation a2 on "a" and on a scientific object "so". 
	 * 
	 * We delete a, so the relation between a and a2 must be deleted but a2 must still exists. 
	 * 
	 */
	void testDeleteAnnotationWithOneSuperAnnotationWithAnotherTarget() throws DAOPersistenceException, Exception {
		
		Annotation a = createAndGetAnnotation(userUri,events.get(0).getUri());
		events.add(createAndGetEvent(so.getUri())); // create a new event 
		Annotation a2 = createAndGetAnnotation(userUri,a.getUri(),events.get(1).getUri()); // annotation on a another annotation and on an event
		
		annotationDao.delete(Arrays.asList(a));
		assertFalse(annotationDao.existUri(a.getUri()));
		assertTrue(annotationDao.existUri(a2.getUri()));

	}



}
