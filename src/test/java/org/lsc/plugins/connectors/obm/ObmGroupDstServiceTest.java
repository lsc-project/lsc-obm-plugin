/*
 ****************************************************************************
 * Ldap Synchronization Connector provides tools to synchronize
 * electronic identities from a list of data sources including
 * any database with a JDBC connector, another LDAP directory,
 * flat files...
 *
 *                  ==LICENSE NOTICE==
 * 
 * Copyright (c) 2008 - 2011 LSC Project 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of the LSC Project nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *                  ==LICENSE NOTICE==
 *
 *               (c) 2008 - 2013 LSC Project
 *         Sebastien Bahloul <seb@lsc-project.org>
 *         Thomas Chemineau <thomas@lsc-project.org>
 *         Jonathan Clarke <jon@lsc-project.org>
 *         Remy-Christophe Schermesser <rcs@lsc-project.org>
 *         Raphael Ouazana <raphael.ouazana@linagora.com>
 ****************************************************************************
 */
package org.lsc.plugins.connectors.obm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mockit.Injectable;
import mockit.NonStrict;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Test;
import org.lsc.LscDatasetModification;
import org.lsc.LscDatasets;
import org.lsc.LscModificationType;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.PluginConnectionType;
import org.lsc.configuration.PluginDestinationServiceType;
import org.lsc.configuration.ServiceType.Connection;
import org.lsc.exception.LscServiceCommunicationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.obm.generated.ObmGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ObmGroupDstServiceTest extends AbstractObmDstServiceTest {
    
    private static transient Logger LOGGER = LoggerFactory.getLogger(ObmGroupDstServiceTest.class);
    
    private static final String TEST_GROUP_ID = "1fc198b0-d6c5-a415-2d5d-9c00487870cc";
    private static final String TEST_USER_ID = "ba7bffaf-02ff-ab52-508f-2394c406c0a8";
    private static final String TEST_SUBGROUP_ID = "5bcde52d-220f-be28-a6cd-40e42e50a89b";
    
	private ObmGroupDstService instance;
	
	private void configuration() {
	       new NonStrictExpectations() {
	            @Injectable @NonStrict ObmGroupService obmGroupService;
	            @Injectable @NonStrict PluginDestinationServiceType pluginDestinationService;
	            @Injectable @NonStrict PluginConnectionType obmConnection;
	            @Injectable @NonStrict Connection connection;
	            {
	                obmGroupService.getDomainUUID(); result = "995c2df5-87e6-e4f2-0a3c-c9d6809e3b37";

	                obmConnection.getUrl(); result = "http://debian7-obm3-1.local:8086/";
	                obmConnection.getUsername(); result = "admin@debian7-obm3-1.local";
	                obmConnection.getPassword() ; result = "admin";
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
	        EndAndWaitAndCheckBatchStatus();
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
	        
	        EndAndWaitAndCheckBatchStatus();
            
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
	        Assert.assertEquals(TEST_USER_ID, testUserBean.getDatasetFirstValueById("users"));
	        Assert.assertEquals(1, testUserBean.getDatasetById("subgroups").size());
	        Assert.assertEquals(TEST_SUBGROUP_ID, testUserBean.getDatasetFirstValueById("subgroups"));
	        
	        EndAndWaitAndCheckBatchStatus();
            
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

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newDescriptionValue, testUserBean.getDatasetFirstValueById("description"));
			
			apply = instance.apply(descriptionModification(oldDescriptionValue));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
            
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
	
	@Test
	public void testModifyUserMembership() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			Set<Object> oldMembers = getBean(TEST_GROUP_ID).getDatasetById("users");
			
			Set<Object> newMembers = new HashSet<Object>(oldMembers);
			newMembers.add(TEST_USER_ID);
			
			boolean apply = instance.apply(memberModification("users", newMembers));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newMembers, testUserBean.getDatasetById("users"));
			
			apply = instance.apply(memberModification("users", oldMembers));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
            
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
	
	@Test
	public void testModifyGroupMembership() throws Exception {
		configuration();
		
		try {
			instance = new ObmGroupDstService(task);
			
			Set<Object> oldMembers = getBean(TEST_GROUP_ID).getDatasetById("subgroups");
			
			Set<Object> newMembers = new HashSet<Object>(oldMembers);
			newMembers.add(TEST_SUBGROUP_ID);
			
			boolean apply = instance.apply(memberModification("subgroups", newMembers));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmGroupDstService(task);

			IBean testUserBean = getBean(TEST_GROUP_ID);
			Assert.assertEquals(newMembers, testUserBean.getDatasetById("subgroups"));
			
			apply = instance.apply(memberModification("subgroups", oldMembers));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
            
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

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmGroupDstService(task);

			IBean testGroupBean = getBean(newGroupId);
			Assert.assertEquals(newGroupId, testGroupBean.getDatasetFirstValueById("id"));
			
			apply = instance.apply(deleteGroup(newGroupId));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
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

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmGroupDstService(task);

			IBean testGroupBean = getBean(newGroupId);
			Assert.assertEquals(newGroupId, testGroupBean.getDatasetFirstValueById("id"));
			Assert.assertEquals(TEST_USER_ID, testGroupBean.getDatasetFirstValueById("users"));
			
			apply = instance.apply(deleteGroup(newGroupId));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
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
				addAttribute("users", TEST_USER_ID)
				);
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}

}
