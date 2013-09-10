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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.lsc.configuration.TaskType;
import org.lsc.plugins.connectors.obm.beans.BatchId;
import org.lsc.plugins.connectors.obm.beans.Group;
import org.lsc.plugins.connectors.obm.beans.ListItem;
import org.lsc.plugins.connectors.obm.beans.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObmDao {
	
	public static final String BASE_PATH = "/provisioning/v1/"; 
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ObmDao.class);

	private WebTarget writeOnlyClient;
	private WebTarget readOnlyClient;
	
	private static Map<TaskType, WebTarget> batchPathes = new HashMap<TaskType, WebTarget>();


	public ObmDao(String url, String domainUUID, String username, String password, TaskType task) {
		readOnlyClient = ClientBuilder.newClient()
				.register(new HttpBasicAuthFilter(username, password))
				.register(JacksonFeature.class)
				.target(url)
				.path(BASE_PATH)
				.path(domainUUID);
		
		WebTarget batchesPath = readOnlyClient.path("batches");
		
		String batchId = createBatch(batchesPath);
		
		writeOnlyClient = batchesPath.path(batchId);
		batchPathes.put(task, writeOnlyClient);
	}
	
	private String createBatch(WebTarget batchesPath) {
		BatchId response = batchesPath.request().post(null, BatchId.class);
		return response.id;
	}
	
	public static void close(TaskType task) {
		if (!batchPathes.containsKey(task)) {
			LOGGER.error("No registered batch for the TaskType " + task.getName());
			return;
		}
		WebTarget batchPath = batchPathes.get(task);
		LOGGER.debug("Commiting batch on: " + batchPath.getUri().toString());
		Response response = batchPath.request().put(Entity.json(""));
		response.close();
		if (checkResponse(response)) {
			LOGGER.info("Batch is running. Please check if it is successful: " + batchPath.getUri().toString());
		} else {
			LOGGER.error(String.format("Error %d (%s) while running batch: %s",
					response.getStatus(),
					response.getStatusInfo(),
					batchPath.getUri().toString()));
		}
	}

	private static boolean checkResponse(Response response) {
		return Status.Family.familyOf(response.getStatus()) == Status.Family.SUCCESSFUL;
	}
	
	public User getUser(String mainIdentifier) throws ProcessingException, WebApplicationException {
		WebTarget target = readOnlyClient.path("users").path(mainIdentifier);
		LOGGER.debug("GETting user: " + target.getUri().toString());
		return target.request().get(User.class);
	}
	
	public List<ListItem> getUserList() throws ProcessingException, WebApplicationException {
		return getList("users");
	}
	
	private List<ListItem> getList(String path) throws ProcessingException, WebApplicationException {
		WebTarget target = readOnlyClient.path(path);
		LOGGER.debug("GETting " + path + ":" + target.getUri().toString());
		return target.request().get(new GenericType<List<ListItem>>(){});
	}
	
	public boolean modifyUser(User user) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("users").path(user.id);
		LOGGER.debug("PUTting user: " + target.getUri().toString());
		Response response = target.request().put(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("PUT is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying user: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	public boolean createUser(User user) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("users");
		LOGGER.debug("POSTing user: " + target.getUri().toString());
		Response response = target.request().post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("POST is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while creating user: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	public boolean deleteUser(String mainIdentifier) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("users").path(mainIdentifier).queryParam("expunge", "true");
		LOGGER.debug("DELETing user: " + target.getUri().toString());
		Response response = target.request().delete();
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("DELETE is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while deleting user: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	public List<ListItem> getGroupList() throws ProcessingException, WebApplicationException {
		return getList("groups");
	}
	
	public Group getGroup(String mainIdentifier) throws ProcessingException, WebApplicationException {
		WebTarget target = readOnlyClient.path("groups").path(mainIdentifier).queryParam("expandDepth", "1");
		LOGGER.debug("GETting group: " + target.getUri().toString());
		return target.request().get(Group.class);
	}

	public boolean createGroup(Group group) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("groups");
		LOGGER.debug("POSTing group: " + target.getUri().toString());
		Response response = target.request().post(Entity.entity(group, MediaType.APPLICATION_JSON_TYPE));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("POST is successful");
			return modifyGroupMembership(target.path(group.id), group);
		} else {
			LOGGER.error(String.format("Error %d (%s) while creating group: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	public boolean modifyGroup(Group group) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("groups").path(group.id);
		LOGGER.debug("PUTting group: " + target.getUri().toString());
		Response response = target.request().put(Entity.entity(group, MediaType.APPLICATION_JSON_TYPE));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("PUT is successful");
			return modifyGroupMembership(target, group);
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying group: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	private boolean modifyGroupMembership(WebTarget groupTarget, Group group) {
		for (String id: group.getUsersToAdd()) {
			if (!addUserToGroup(groupTarget, id)) {
				return false;
			}
		}
		for (String id: group.getUsersToRemove()) {
			if (!removeUserFromGroup(groupTarget, id)) {
				return false;
			}
		}
		for (String id: group.getGroupsToAdd()) {
			if (!addSubGroupToGroup(groupTarget, id)) {
				return false;
			}
		}
		for (String id: group.getGroupsToRemove()) {
			if (!removeSubGroupFromGroup(groupTarget, id)) {
				return false;
			}
		}
		return true;
	}

	private boolean addUserToGroup(WebTarget groupTarget, String userId) {
		WebTarget target = groupTarget.path("users").path(userId);
		LOGGER.debug("PUTting user in group: " + target.getUri().toString());
		Response response = target.request().put(Entity.json(""));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("PUT is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying group membership: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	private boolean removeUserFromGroup(WebTarget groupTarget, String userId) {
		WebTarget target = groupTarget.path("users").path(userId);
		LOGGER.debug("DELETing user in group: " + target.getUri().toString());
		Response response = target.request().delete();
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("DELETE is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying group membership: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	private boolean addSubGroupToGroup(WebTarget groupTarget, String subGroupId) {
		WebTarget target = groupTarget.path("subgroups").path(subGroupId);
		LOGGER.debug("PUTting group in group: " + target.getUri().toString());
		Response response = target.request().put(Entity.json(""));
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("PUT is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying group membership: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	private boolean removeSubGroupFromGroup(WebTarget groupTarget, String subGroupId) {
		WebTarget target = groupTarget.path("subgroups").path(subGroupId);
		LOGGER.debug("DELETing group in group: " + target.getUri().toString());
		Response response = target.request().delete();
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("DELETE is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while modifying group membership: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}

	public boolean deleteGroup(String mainIdentifier) throws ProcessingException {
		WebTarget target = writeOnlyClient.path("groups").path(mainIdentifier);
		LOGGER.debug("DELETing group: " + target.getUri().toString());
		Response response = target.request().delete();
		response.close();
		if (checkResponse(response)) {
			LOGGER.debug("DELETE is successful");
			return true;
		} else {
			LOGGER.error(String.format("Error %d (%s) while deleting group: %s",
					response.getStatus(),
					response.getStatusInfo(),
					target.getUri().toString()));
			return false;
		}
	}
	
}
