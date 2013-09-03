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

import java.util.List;

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
import org.lsc.plugins.connectors.obm.beans.BatchId;
import org.lsc.plugins.connectors.obm.beans.User;
import org.lsc.plugins.connectors.obm.beans.UserListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObmDao {
	
	public static final String BASE_PATH = "/provisioning/v1/"; 
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ObmDao.class);

	private WebTarget writeOnlyClient;
	private WebTarget readOnlyClient;
	
	private static WebTarget batchPath;

	public ObmDao(String url, String domainUUID, String username, String password) {
		readOnlyClient = ClientBuilder.newClient()
				.register(new HttpBasicAuthFilter(username, password))
				.register(JacksonFeature.class)
				.target(url)
				.path(BASE_PATH)
				.path(domainUUID);
		
		WebTarget batchesPath = readOnlyClient.path("batches");
		
		String batchId = createBatch(batchesPath);
		
		writeOnlyClient = batchesPath.path(batchId);
		batchPath = writeOnlyClient;
	}
	
	private String createBatch(WebTarget batchesPath) {
		BatchId response = batchesPath.request().post(null, BatchId.class);
		return response.id;
	}
	
	public static void close() {
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
	
	public List<UserListItem> getUserList() throws ProcessingException, WebApplicationException {
		WebTarget target = readOnlyClient.path("users");
		LOGGER.debug("GETting user: " + target.getUri().toString());
		return target.request().get(new GenericType<List<UserListItem>>(){});
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
}
