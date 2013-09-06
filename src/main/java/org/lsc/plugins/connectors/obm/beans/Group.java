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
package org.lsc.plugins.connectors.obm.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.lsc.LscDatasets;


@JsonIgnoreProperties({"usersToAdd", "usersToRemove", "groupsToAdd", "groupsToRemove"})
public class Group implements Identifiable {
	public static class Members {
		public List<User> users;
		public List<Group> subgroups;
	}
	
	public String id;
	public String description;
	public String name;
	public String email;
	
	public Members members;
	
	private List<String> usersToAdd = new ArrayList<String>();
	private List<String> usersToRemove = new ArrayList<String>();
	private List<String> groupsToAdd = new ArrayList<String>();
	private List<String> groupsToRemove = new ArrayList<String>();
	
	
	public Group() {}
	
	public Group(String mainIdentifier, Map<String, List<Object>> modificationsItems) {
		id = mainIdentifier;
		members = new Members();
		members.users = new ArrayList<User>();
		members.subgroups = new ArrayList<Group>();
		modify(modificationsItems);
	}

	public void modify(Map<String, List<Object>> modificationsItems) {
		description = (!modificationsItems.containsKey("description")) ? description : modificationsItems.get("description").size() == 0 ? null : String.valueOf(modificationsItems.get("description").get(0)); 
		name = (!modificationsItems.containsKey("name")) ? name : modificationsItems.get("name").size() == 0 ? null : String.valueOf(modificationsItems.get("name").get(0)); 
		email = (!modificationsItems.containsKey("email")) ? email : modificationsItems.get("email").size() == 0 ? null : String.valueOf(modificationsItems.get("email").get(0));
		
		if (modificationsItems.containsKey("users")) {
			List<String> currentUsers = memberListToIdList(members.users);
			List<String> newUsers = toStringList(modificationsItems.get("users"));
			usersToAdd = new ArrayList<String>(newUsers);
			usersToAdd.removeAll(currentUsers);
			usersToRemove = new ArrayList<String>(currentUsers);
			usersToRemove.removeAll(newUsers);
		}
		
		if (modificationsItems.containsKey("subgroups")) {
			List<String> currentGroups = memberListToIdList(members.subgroups);
			List<String> newGroups = toStringList(modificationsItems.get("subgroups"));
			groupsToAdd = new ArrayList<String>(newGroups);
			groupsToAdd.removeAll(currentGroups);
			groupsToRemove = new ArrayList<String>(currentGroups);
			groupsToRemove.removeAll(newGroups);
		}
	}

	private List<String> toStringList(List<Object> uncastedValues) {
		List<String> values = new ArrayList<String>(uncastedValues.size());
		for (Object object: uncastedValues) {
			values.add(String.valueOf(object));
		}
		return values;
	}

	public LscDatasets toDatasets() {
		LscDatasets datasets = new LscDatasets();
		
		datasets.put("id", id);
		putIfNotNull(datasets, "description", description);
		putIfNotNull(datasets, "name", name);
		putIfNotNull(datasets, "email", email);
		
		if (members != null) {
			if (members.users != null) {
				datasets.put("users", memberListToIdList(members.users));
			}
			if (members.subgroups != null) {
				datasets.put("subgroups", memberListToIdList(members.subgroups));
			}
		}
		
		return datasets;
	}
	
	private void putIfNotNull(LscDatasets datasets, String key, Object value) {
		if (value != null) {
			datasets.put(key, value);
		}
	}
	
	private List<String> memberListToIdList(List<? extends Identifiable> memberList) {
		List<String> memberIds = new ArrayList<String>(memberList.size());
		for (Identifiable member: memberList) {
			String userId = String.valueOf(member.getId());
			memberIds.add(userId);
		}
		return memberIds;
	}
	
	public static List<String> getWritableAttributes() {
		return Arrays.asList("id", "description", "name", "email", "users", "subgroups");
	}

	public String getId() {
		return id;
	}

	public List<String> getUsersToAdd() {
		return usersToAdd;
	}

	public List<String> getUsersToRemove() {
		return usersToRemove;
	}

	public List<String> getGroupsToAdd() {
		return groupsToAdd;
	}

	public List<String> getGroupsToRemove() {
		return groupsToRemove;
	}
}
