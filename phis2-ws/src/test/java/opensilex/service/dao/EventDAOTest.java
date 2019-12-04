//******************************************************************************
//                                EventDAOTest.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 30 oct. 2019
// Contact: renaud.colin@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************


package opensilex.service.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.manager.Rdf4jDAOTest;
import opensilex.service.model.Annotation;
import opensilex.service.model.Event;
import opensilex.service.model.Experiment;
import opensilex.service.model.Project;
import opensilex.service.model.ScientificObject;

/**
 * 
 * @author renaud.colin@inra.fr
 *
 */
class EventDAOTest extends Rdf4jDAOTest{
	
	protected static EventDAO eventDao;
	protected ScientificObject so;
	protected Experiment xp;
	private static String userUri;
	
	@BeforeAll
	public static void setUp() throws Exception {
		UserDAO userDao = new UserDAO();
		userUri = "http://www.opensilex.org/demo/id/agent/admin_phis"; 
		
		eventDao =  new EventDAO(userDao.findById(userUri));
		initDaoWithInMemoryStoreConnection(eventDao);
	}
	
	@BeforeEach
	protected void initDao() throws DAOPersistenceException, Exception {
		Project createdProject = createAndGetProject();
		xp = createAndGetExperiment(createdProject);
		so = createAndGetScientificObject(xp);
	}

	@Test
	void test_delete_event_on_one_scientific_object() throws DAOPersistenceException, Exception {
		
		long initialSize = eventDao.getConnection().size();
		
		ArrayList<String> concernedUris = new ArrayList<>(Arrays.asList(so.getUri()));
		Event event = createAndGetEvent(concernedUris); // create one event on one ScientificObject		
		eventDao.delete(Arrays.asList(event));
		
		assertEquals(initialSize,eventDao.getConnection().size());
		assertFalse(eventDao.existUri(event.getUri()));
		assertTrue(eventDao.existUri(so.getUri()));
	}
	

	@Test
	void test_delete_event_on_multiple_scientific_object() throws DAOPersistenceException, Exception {
		
		int k = 3;
		ArrayList<String> soUris = new ArrayList<>(k);
		for(int i=0;i<k;i++) {
			soUris.add(createAndGetScientificObject(xp).getUri());
		}	
		long initialSize = eventDao.getConnection().size();
		
		Event event = createAndGetEvent(soUris);
		eventDao.delete(Arrays.asList(event));
		assertEquals(initialSize,eventDao.getConnection().size());
		
		assertFalse(eventDao.existUri(event.getUri()));
		for(String soUri : soUris) {
			assertTrue(eventDao.existUri(soUri));
		}
		
	}
	
	@Test
	void test_delete_event_with_one_annotation() throws DAOPersistenceException, Exception {
		
		long initialSize = eventDao.getConnection().size();
		
		ArrayList<String> concernedUris = new ArrayList<>(Arrays.asList(so.getUri()));
		Event event = createAndGetEvent(concernedUris); // create one event on one ScientificObject
		
		ArrayList<String> eventUris = new ArrayList<>(Arrays.asList(event.getUri()));
		Annotation a = createAndGetAnnotation(eventUris,userUri);
		eventDao.delete(Arrays.asList(event));
		
		assertFalse(eventDao.existUri(event.getUri()));
		assertFalse(eventDao.existUri(a.getUri()));
		assertEquals(initialSize,eventDao.getConnection().size());

	}
	
	@Test
	void test_delete_event_with_one_annotation_on_multiple_events() throws DAOPersistenceException, Exception {
		
		long initialSize = eventDao.getConnection().size();
		
		ArrayList<String> concernedUris = new ArrayList<>(Arrays.asList(so.getUri()));
		Event event = createAndGetEvent(concernedUris); // create one event on one ScientificObject
		Event event1 = createAndGetEvent(concernedUris);
		
		ArrayList<String> eventUris = new ArrayList<>(Arrays.asList(event.getUri(),event1.getUri()));

		Annotation a = createAndGetAnnotation(eventUris,userUri); // delete the first event, the annotation should not be removed
		eventDao.delete(Arrays.asList(event));
		
		assertFalse(eventDao.existUri(event.getUri()));
		assertTrue(eventDao.existUri(event1.getUri()));
		assertTrue(eventDao.existUri(a.getUri()));
		
		assertNotEquals(initialSize,eventDao.getConnection().size());
		
	}
	
	

}
