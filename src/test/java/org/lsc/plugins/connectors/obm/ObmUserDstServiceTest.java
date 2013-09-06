package org.lsc.plugins.connectors.obm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrict;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Test;
import org.lsc.LscDatasetModification;
import org.lsc.LscDatasetModification.LscDatasetModificationType;
import org.lsc.LscDatasets;
import org.lsc.LscModificationType;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.PluginConnectionType;
import org.lsc.configuration.PluginDestinationServiceType;
import org.lsc.configuration.ServiceType.Connection;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceCommunicationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.obm.generated.ObmUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ObmUserDstServiceTest {
    
    private static transient Logger LOGGER = LoggerFactory.getLogger(ObmUserDstServiceTest.class);
    
    private static final String TEST_USER_ID = "56d09e41-4131-7508-9b76-0facc8cf76a0";
    
	private ObmUserDstService instance;
	
	@Mocked TaskType task;
	
	private void configuration() {
	       new NonStrictExpectations() {
	            @Injectable @NonStrict ObmUserService obmUserService;
	            @Injectable @NonStrict PluginDestinationServiceType pluginDestinationService;
	            @Injectable @NonStrict PluginConnectionType obmConnection;
	            @Injectable @NonStrict Connection connection;
	            {
	                obmUserService.getDomainUUID(); result = "525ee17e-f0b5-2d71-4b36-639665540528";

	                obmConnection.getUrl(); result = "http://10.69.0.254:8080/obm-sync";
	                obmConnection.getUsername(); result = "ad.min@obm19.lyn.lng";
	                obmConnection.getPassword() ; result = "secret";
	                connection.getReference(); result = obmConnection;
	                obmUserService.getConnection(); result = connection;
	                task.getBean(); result = "org.lsc.beans.SimpleBean";
	                task.getPluginDestinationService(); result = pluginDestinationService;
	                List<Object> any = new ArrayList<Object>();
	                any.add(obmUserService);
	                pluginDestinationService.getAny(); result = any;
	            }
	        };
	}

	@Test
	public void testConnection() throws Exception {
		configuration();

	    try {
	        instance = new ObmUserDstService(task);
	        ObmDao.close();
            LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	@Test
	public void testgetList() throws Exception {
		configuration();

	    try {
	        instance = new ObmUserDstService(task);

	        Map<String, LscDatasets> listPivots = instance.getListPivots();
	        
	        Assert.assertNotNull(listPivots);
	        Assert.assertNotNull(listPivots.get(TEST_USER_ID));
	        Assert.assertEquals(TEST_USER_ID, listPivots.get(TEST_USER_ID).getStringValueAttribute("id"));
	        
	        ObmDao.close();
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}

	@Test
	public void testgetOne() throws Exception {
		configuration();

	    try {
	        instance = new ObmUserDstService(task);
	        
	        IBean testUserBean = getBean(TEST_USER_ID);

	        Assert.assertNotNull(testUserBean);
	        Assert.assertEquals(TEST_USER_ID, testUserBean.getDatasetFirstValueById("id"));
	        
	        ObmDao.close();
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}

	private IBean getBean(String id) throws LscServiceException {
		LscDatasets dataSets = new LscDatasets();
		dataSets.put("id", id);
		IBean testUserBean = instance.getBean(id, dataSets, true);
		return testUserBean;
	}
	
	@Test
	public void testModify() throws Exception {
		configuration();
		
		try {
			instance = new ObmUserDstService(task);
			
			String oldTelephoneValue = getBean(TEST_USER_ID).getDatasetFirstValueById("mobile");
			String newTelephoneValue = oldTelephoneValue + " 42424242";
			
			boolean apply = instance.apply(mobileModification(newTelephoneValue));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmUserDstService(task);

			IBean testUserBean = getBean(TEST_USER_ID);
			Assert.assertEquals(newTelephoneValue, testUserBean.getDatasetFirstValueById("mobile"));
			
			apply = instance.apply(mobileModification(oldTelephoneValue));

			Assert.assertTrue(apply);

			ObmDao.close();
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	private LscModifications mobileModification(Object newMobileValue) {
		LscModifications lm = new LscModifications(LscModificationType.UPDATE_OBJECT);
		lm.setMainIdentifer(TEST_USER_ID);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(addAttribute("mobile", newMobileValue));
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}
	
	private LscDatasetModification addAttribute(String key, Object value) {
		Collection<Object> values = Collections.singleton(value);
		return new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, key, values);
	}

	@Test
	public void testAddThenDelete() throws Exception {
		configuration();
		
		try {
			instance = new ObmUserDstService(task);
			
			String newUserId = "d07cb27d-af09-481e-9601-8ef952bbe87f";
			
			boolean apply = instance.apply(newUser(newUserId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmUserDstService(task);

			IBean testUserBean = getBean(newUserId);
			Assert.assertEquals(newUserId, testUserBean.getDatasetFirstValueById("id"));
			
			apply = instance.apply(deleteUser(newUserId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmUserDstService(task);

			testUserBean = getBean(newUserId);
			Assert.assertNull(testUserBean);
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	private LscModifications newUser(String userId) {
		LscModifications lm = new LscModifications(LscModificationType.CREATE_OBJECT);
		lm.setMainIdentifer(userId);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(
				addAttribute("login", "new.user"),
				addAttribute("password", "newPassword"),
				addAttribute("lastname", "user"),
				addAttribute("firstname", "new"),
				addAttribute("profile", "user"),
				addAttribute("country", "fr")
				);
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}

	private LscModifications deleteUser(String userId) {
		LscModifications lm = new LscModifications(LscModificationType.DELETE_OBJECT);
		lm.setMainIdentifer(userId);
		return lm;
	}

}
