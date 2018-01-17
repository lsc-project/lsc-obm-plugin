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
import java.util.List;
import java.util.Map;

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
import org.lsc.plugins.connectors.obm.generated.ObmUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ObmUserDstServiceTest extends AbstractObmDstServiceTest {
    
    private static transient Logger LOGGER = LoggerFactory.getLogger(ObmUserDstServiceTest.class);
    
    private static final String TEST_USER_ID = "ba7bffaf-02ff-ab52-508f-2394c406c0a8";
    
	private ObmUserDstService instance;
	
	private void configuration() {
	       new NonStrictExpectations() {
	            @Injectable @NonStrict ObmUserService obmUserService;
	            @Injectable @NonStrict PluginDestinationServiceType pluginDestinationService;
	            @Injectable @NonStrict PluginConnectionType obmConnection;
	            @Injectable @NonStrict Connection connection;
	            {
	                obmUserService.getDomainUUID(); result = "995c2df5-87e6-e4f2-0a3c-c9d6809e3b37";

	                obmConnection.getUrl(); result = "http://debian7-obm3-1.local:8086/";
	                obmConnection.getUsername(); result = "admin@debian7-obm3-1.local";
	                obmConnection.getPassword() ; result = "admin";
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
	        instance = new ObmUserDstService(task);

	        Map<String, LscDatasets> listPivots = instance.getListPivots();
	        
	        Assert.assertNotNull(listPivots);
	        Assert.assertNotNull(listPivots.get(TEST_USER_ID));
	        Assert.assertEquals(TEST_USER_ID, listPivots.get(TEST_USER_ID).getStringValueAttribute("id"));
	        
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
	        instance = new ObmUserDstService(task);
	        
	        IBean testUserBean = getBean(TEST_USER_ID);

	        Assert.assertNotNull(testUserBean);
	        Assert.assertEquals(TEST_USER_ID, testUserBean.getDatasetFirstValueById("id"));
	        
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
	public void testModifyMobile() throws Exception {
		testAttributeMofication("mobile");
	}

	private void testAttributeMofication(String attributeId) throws Exception {
		configuration();
		
		try {
			instance = new ObmUserDstService(task);
			
			String oldValue = getBean(TEST_USER_ID).getDatasetFirstValueById(attributeId);
			String newValue = oldValue + " 42424242";
			
			boolean apply = instance.apply(modification(attributeId, newValue));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmUserDstService(task);

			IBean testUserBean = getBean(TEST_USER_ID);
			Assert.assertEquals(newValue, testUserBean.getDatasetFirstValueById(attributeId));
			
			apply = instance.apply(modification(attributeId, oldValue));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
            
	        LOGGER.info("Test successful !");
	    } catch(LscServiceCommunicationException e) {
	        LOGGER.info("OBM server unavailable. Test exited successfully !");
	    }
	}
	
	private LscModifications modification(String attributeId, Object newValue) {
		LscModifications lm = new LscModifications(LscModificationType.UPDATE_OBJECT);
		lm.setMainIdentifer(TEST_USER_ID);
		List<LscDatasetModification> attrsMod = Lists.newArrayList(addAttribute(attributeId, newValue));
		lm.setLscAttributeModifications(attrsMod);
		return lm;
	}
	
	@Test
	public void testAddThenDelete() throws Exception {
		configuration();
		
		try {
			instance = new ObmUserDstService(task);
			
			String newUserId = "d07cb27d-af09-481e-9601-8ef952bbe87f";
			
			boolean apply = instance.apply(newUser(newUserId));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
			instance = new ObmUserDstService(task);

			IBean testUserBean = getBean(newUserId);
			Assert.assertEquals(newUserId, testUserBean.getDatasetFirstValueById("id"));
			
			apply = instance.apply(deleteUser(newUserId));

			Assert.assertTrue(apply);

			EndAndWaitAndCheckBatchStatus();
			
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
				addAttribute("profile", "user")
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
