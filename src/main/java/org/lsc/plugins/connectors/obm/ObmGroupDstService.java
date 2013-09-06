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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import org.lsc.LscDatasets;
import org.lsc.LscModifications;
import org.lsc.beans.IBean;
import org.lsc.configuration.PluginConnectionType;
import org.lsc.configuration.TaskType;
import org.lsc.exception.LscServiceCommunicationException;
import org.lsc.exception.LscServiceConfigurationException;
import org.lsc.exception.LscServiceException;
import org.lsc.plugins.connectors.obm.beans.Group;
import org.lsc.plugins.connectors.obm.beans.ListItem;
import org.lsc.plugins.connectors.obm.generated.ObmGroupService;
import org.lsc.plugins.connectors.obm.generated.ObmService;
import org.lsc.service.IWritableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObmGroupDstService implements IWritableService {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ObmGroupDstService.class);
	/**
	 * Preceding the object feeding, it will be instantiated from this class.
	 */
	private Class<IBean> beanClass;

	private ObmService service;
	private PluginConnectionType connexion;

	private ObmDao obmDao;
	
	/**
	 * Create the service
	 * @param task the task in which the source service settings will be used
	 * @throws LscServiceConfigurationException never thrown
	 */
	@SuppressWarnings("unchecked")
	public ObmGroupDstService(final TaskType task) throws LscServiceConfigurationException, LscServiceCommunicationException {
		try {
	        if (task.getPluginDestinationService().getAny() == null || task.getPluginDestinationService().getAny().size() != 1 || !((task.getPluginDestinationService().getAny().get(0) instanceof ObmGroupService))) {
	            throw new LscServiceConfigurationException("Unable to identify the obm service configuration " + "inside the plugin source node of the task: " + task.getName());
	        }
	        
        	service = (ObmService) task.getPluginDestinationService().getAny().get(0);
			beanClass = (Class<IBean>) Class.forName(task.getBean());
			connexion = (PluginConnectionType) service.getConnection().getReference();
			
			obmDao = new ObmDao(connexion.getUrl(), service.getDomainUUID(), connexion.getUsername(), connexion.getPassword());
			
			checkEndBatchHook(task);
			
		} catch (ClassNotFoundException e) {
			throw new LscServiceConfigurationException(e);
		} catch (ProcessingException e) {
			throw new LscServiceCommunicationException("Can't create batch", e);
		}
	}
	
	private void checkEndBatchHook(TaskType task) {
		String expectedSyncHook = ObmDao.class.getCanonicalName() + ".close";
		if (expectedSyncHook.equals(task.getSyncHook())) {
			LOGGER.debug("SyncHook is correctly configured to: " + expectedSyncHook);
		} else {
			LOGGER.warn("Please set syncHook to " + expectedSyncHook + " instead of " + task.getSyncHook());
		}
	}
	
	@Override
	public IBean getBean(String pivotName, LscDatasets pivotAttributes, boolean fromSameService)
			throws LscServiceException {
		LOGGER.debug(String.format("Call to getBean(%s, %s, %b)", pivotName, pivotAttributes, fromSameService));
		if (pivotAttributes.getAttributesNames().size() < 1) {
			return null;
		}
		String pivotAttribute = pivotAttributes.getAttributesNames().get(0);
		String id = pivotAttributes.getStringValueAttribute(pivotAttribute);
		if (id == null) {
			return null;
		}
		try {
			Group group = obmDao.getGroup(id);
			return groupToBean(group);
		} catch (ProcessingException e) {
			LOGGER.error(String.format("ProcessingException while getting bean %s/%s (%s)",
					pivotName, id, e));
			LOGGER.debug(e.toString(), e);
			throw new LscServiceCommunicationException(e);
		} catch (NotFoundException e) {
			LOGGER.debug(String.format("%s/%s not found", pivotName, id));
			return null;
		} catch (WebApplicationException e) {
			LOGGER.error(String.format("WebApplicationException while getting bean %s/%s (%s)",
					pivotName, id, e));
			LOGGER.debug(e.toString(), e);
			throw new LscServiceException(e);
		} catch (InstantiationException e) {
			LOGGER.error("Bad class name: " + beanClass.getName() + "(" + e + ")");
			LOGGER.debug(e.toString(), e);
			throw new LscServiceException(e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Bad class name: " + beanClass.getName() + "(" + e + ")");
			LOGGER.debug(e.toString(), e);
			throw new LscServiceException(e);
		}
	}

	private IBean groupToBean(Group group) throws InstantiationException, IllegalAccessException {
		IBean bean = beanClass.newInstance();
		bean.setMainIdentifier(group.id);
		bean.setDatasets(group.toDatasets());
		return bean;
	}
	
	@Override
	public Map<String, LscDatasets> getListPivots()
			throws LscServiceException {
		
		try {
			List<ListItem> groupList = obmDao.getGroupList();

			Map<String, LscDatasets> listPivots = new HashMap<String, LscDatasets>();
			for (ListItem group: groupList) {
				listPivots.put(group.id, group.toDatasets());
			}
			return listPivots;
		} catch (ProcessingException e) {
			LOGGER.error(String.format("ProcessingException while getting pivot list (%s)", e));
			LOGGER.debug(e.toString(), e);
			throw new LscServiceCommunicationException(e);
		} catch (WebApplicationException e) {
			LOGGER.error(String.format("WebApplicationException while getting pivot list (%s)", e));
			LOGGER.debug(e.toString(), e);
			throw new LscServiceException(e);
		}
	}

	@Override
	public boolean apply(LscModifications lm) throws LscServiceException {
		try {
			switch(lm.getOperation()) {
				case CHANGE_ID:
					LOGGER.warn("Trying to change ID of an OBM group, impossible operation, ignored.");
					// Silently return without doing anything
					return true;
				case CREATE_OBJECT:
					LOGGER.debug("Creating OBM group: " + lm.getMainIdentifier());
					return obmDao.createGroup(new Group(lm.getMainIdentifier(), lm.getModificationsItemsByHash()));
				case UPDATE_OBJECT:
					LOGGER.debug("Getting OBM group for update: " + lm.getMainIdentifier());
					Group group = obmDao.getGroup(lm.getMainIdentifier());
					LOGGER.debug("Modifying OBM group: " + lm.getMainIdentifier() + " with: " + lm.getModificationsItemsByHash());
					group.modify(lm.getModificationsItemsByHash());
					return obmDao.modifyGroup(group);
				case DELETE_OBJECT:
					LOGGER.debug("Deleting OBM group: " + lm.getMainIdentifier());
					return obmDao.deleteGroup(lm.getMainIdentifier());
				default:
					LOGGER.error(String.format("Unknown operation %s", lm.getOperation()));
					return false;
			}
		} catch (ProcessingException e) {
			LOGGER.error(String.format("ProcessingException while writing (%s)", e));
			LOGGER.debug(e.toString(), e);
			return false;
		}
		
	}

	@Override
	public List<String> getWriteDatasetIds() {
		return service.getWritableAttributes().getString();
	}
}
