package opensilex.service.utils;

import opensilex.service.dao.RadiometricTargetDAO;
import opensilex.service.dao.UserDAO;
import opensilex.service.dao.exception.DAOPersistenceException;
import opensilex.service.dao.manager.Rdf4jDAOTest;
import opensilex.service.model.RadiometricTarget;
import opensilex.service.model.RdfResourceDefinition;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UriGeneratorTest extends Rdf4jDAOTest {

  	private static String userUri;
  	private static UserDAO userDao;

    @BeforeAll
    public static void setUp() throws Exception {
        userDao = new UserDAO();
		userUri = "http://www.opensilex.org/demo/id/agent/admin_phis";
    }

    @Test
    public void testRadiometricTargetGenerate() throws Exception {
        RadiometricTargetDAO radioDao = new RadiometricTargetDAO(userDao.findById(userUri));
        initDaoWithInMemoryStoreConnection(radioDao);

        RadiometricTarget t1 = new RadiometricTarget();
        t1.setLabel("rt0");

        radioDao.checkAndInsert(Arrays.asList(t1));
        assertFalse(StringUtils.isEmpty(t1.getUri()));

        List<RadiometricTarget> targetList = new ArrayList<>();
        for(int i=1 ;i<5 ; i++){
            RadiometricTarget target = new RadiometricTarget();
            target.setLabel("rt"+i);
            targetList.add(target);
        }

        radioDao.checkAndInsert(targetList);
        HashSet<String> uris = new HashSet<>();
        targetList.forEach(target -> uris.add(target.getUri()));

        assertEquals(uris.size(),targetList.size()); // check that all uris are uniques
    }
}