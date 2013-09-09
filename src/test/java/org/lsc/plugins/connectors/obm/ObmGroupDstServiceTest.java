package org.lsc.plugins.connectors.obm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.lsc.plugins.connectors.obm.generated.ObmGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ObmGroupDstServiceTest {
    
    private static transient Logger LOGGER = LoggerFactory.getLogger(ObmGroupDstServiceTest.class);
    
    private static final String TEST_GROUP_ID = "d7099646-40d9-0ff4-0878-ae1bae01e379";
    
	private ObmGroupDstService instance;
	
	@Mocked TaskType task;
	
	private void configuration() {
	       new NonStrictExpectations() {
	            @Injectable @NonStrict ObmGroupService obmGroupService;
	            @Injectable @NonStrict PluginDestinationServiceType pluginDestinationService;
	            @Injectable @NonStrict PluginConnectionType obmConnection;
	            @Injectable @NonStrict Connection connection;
	            {
	            	obmGroupService.getDomainUUID(); result = "525ee17e-f0b5-2d71-4b36-639665540528";

	                obmConnection.getUrl(); result = "http://10.69.0.254:8080/obm-sync";
	                obmConnection.getUsername(); result = "ad.min@obm19.lyn.lng";
	                obmConnection.getPassword() ; result = "secret";
	                connection.getReference(); result = obmConnection;
	                obmGroupService.getConnection(); result = connection;
	                task.getBean(); result = "org.lsc.beans.SimpleBean";
	                task.getPluginDestinationService(); result = pluginDestinationService;
	                List<Object> any = new ArrayList<Object>();
	                any.add(obmGroupService);
	                pluginDestinationService.getAny(); result = any;
	            }
	        };
	}

	@Test
	public void testConnection() throws Exception {
		configuration();

	    try {
	        instance = new ObmGroupDstService(task);
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
	        instance = new ObmGroupDstService(task);

	        Map<String, LscDatasets> listPivots = instance.getListPivots();
	        
	        Assert.assertNotNull(listPivots);
	        Assert.assertNotNull(listPivots.get(TEST_GROUP_ID));
	        Assert.assertEquals(TEST_GROUP_ID, listPivots.get(TEST_GROUP_ID).getStringValueAttribute("id"));
	        
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
	        instance = new ObmGroupDstService(task);
	        
	        IBean testUserBean = getBean(TEST_GROUP_ID);

	        Assert.assertNotNull(testUserBean);
	        Assert.assertEquals(TEST_GROUP_ID, testUserBean.getDatasetFirstValueById("id"));
	        Assert.assertEquals(1, testUserBean.getDatasetById("users").size());
	        Assert.assertEquals("56d09e41-4131-7508-9b76-0facc8cf76a0", testUserBean.getDatasetFirstValueById("users"));
	        Assert.assertEquals(1, testUserBean.getDatasetById("subgroups").size());
	        Assert.assertEquals("1a3ea81d-1cfe-90d4-94ce-e447e2df33b4", testUserBean.getDatasetFirstValueById("subgroups"));
	        
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
	public void testModifyDescription() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			String oldDescriptionValue = getBean(TEST_GROUP_ID).getDatasetFirstValueById("description");
			String newDescriptionValue = oldDescriptionValue + "-modified";
			
			boolean apply = instance.apply(descriptionModification(newDescriptionValue));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newDescriptionValue, testUserBean.getDatasetFirstValueById("description"));
			
			apply = instance.apply(descriptionModification(oldDescriptionValue));

			Assert.assertTrue(apply);

			ObmDao.close();
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	private LscModifications descriptionModification(Object newDescriptionValue) {
		LscModifications lm = new LscModifications(LscModificationType.UPDATE_OBJECT);
		lm.setMainIdentifer(TEST_GROUP_ID);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(addAttribute("description", newDescriptionValue));
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}
	
	private LscDatasetModification addAttribute(String key, Object value) {
		Collection<Object> values = Collections.singleton(value);
		return new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, key, values);
	}

	@Test
	public void testModifyUserMembership() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			Set<Object> oldMembers = getBean(TEST_GROUP_ID).getDatasetById("users");
			
			Set<Object> newMembers = new HashSet<Object>(oldMembers);
			newMembers.add("b4daa82c-2530-30d6-bb59-95276e324fe7");
			
			boolean apply = instance.apply(memberModification("users", newMembers));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newMembers, testUserBean.getDatasetById("users"));
			
			apply = instance.apply(memberModification("users", oldMembers));

			Assert.assertTrue(apply);

			ObmDao.close();
            
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(oldMembers, testUserBean.getDatasetById("users"));
			
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}

	private LscModifications memberModification(String key, Set<Object> newMembersSet) {
		LscModifications lm = new LscModifications(LscModificationType.UPDATE_OBJECT);
		lm.setMainIdentifer(TEST_GROUP_ID);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(replaceAttribute(key, newMembersSet));
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}
	
	private LscDatasetModification replaceAttribute(String key, Set<Object> values) {
		return new LscDatasetModification(LscDatasetModificationType.REPLACE_VALUES, key, values);
	}

	@Test
	public void testModifyGroupMembership() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			Set<Object> oldMembers = getBean(TEST_GROUP_ID).getDatasetById("subgroups");
			
			Set<Object> newMembers = new HashSet<Object>(oldMembers);
			newMembers.add("3e96d0da-e578-1cb1-7b9e-4f6c8a763fba");
			
			boolean apply = instance.apply(memberModification("subgroups", newMembers));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newMembers, testUserBean.getDatasetById("subgroups"));
			
			apply = instance.apply(memberModification("subgroups", oldMembers));

			Assert.assertTrue(apply);

			ObmDao.close();
            
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(oldMembers, testUserBean.getDatasetById("subgroups"));
			
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	@Test
	public void testAddThenDelete() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			String newGroupId = "2756f530-e228-45a1-abc7-8f07769227de";
			
			boolean apply = instance.apply(newGroup(newGroupId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			IBean testGroupBean = getBean(newGroupId);
			Assert.assertEquals(newGroupId, testGroupBean.getDatasetFirstValueById("id"));
			
			apply = instance.apply(deleteGroup(newGroupId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			testGroupBean = getBean(newGroupId);
			Assert.assertNull(testGroupBean);
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	private LscModifications newGroup(String groupId) {
		LscModifications lm = new LscModifications(LscModificationType.CREATE_OBJECT);
		lm.setMainIdentifer(groupId);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(
				addAttribute("name", "newgroup"),
				addAttribute("email", "newgroup")
				);
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}

	private LscModifications deleteGroup(String userId) {
		LscModifications lm = new LscModifications(LscModificationType.DELETE_OBJECT);
		lm.setMainIdentifer(userId);
		return lm;
	}

	@Test
	public void testAddWithUserThenDelete() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			String newGroupId = "2756f530-e228-45a1-abc7-8f07769227de";
			
			boolean apply = instance.apply(newGroupWithUser(newGroupId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			IBean testGroupBean = getBean(newGroupId);
			Assert.assertEquals(newGroupId, testGroupBean.getDatasetFirstValueById("id"));
			Assert.assertEquals("b4daa82c-2530-30d6-bb59-95276e324fe7", testGroupBean.getDatasetFirstValueById("users"));
			
			apply = instance.apply(deleteGroup(newGroupId));

			Assert.assertTrue(apply);

			ObmDao.close();
			
			System.out.println("Waiting 2s while batch is running...");
			Thread.sleep(2000);
			
			instance = new ObmGroupDstService(task);

			testGroupBean = getBean(newGroupId);
			Assert.assertNull(testGroupBean);
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}

	private LscModifications newGroupWithUser(String groupId) {
		LscModifications lm = new LscModifications(LscModificationType.CREATE_OBJECT);
		lm.setMainIdentifer(groupId);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(
				addAttribute("name", "newgroup"),
				addAttribute("email", "newgroup"),
				addAttribute("users", "b4daa82c-2530-30d6-bb59-95276e324fe7")
				);
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}

}
