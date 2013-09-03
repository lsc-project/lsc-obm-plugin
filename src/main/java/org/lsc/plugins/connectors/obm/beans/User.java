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

import org.lsc.LscDatasets;


public class User {
	public static final String DEFAULT_TIME =  "1970-01-01T00:00:00.000+0000";
	
	/* Mandatory */
	public String id;
	public String login;
	public String lastname;
	public String profile;
	
	/* Optional */
	public String firstname;
	public String commonname;
	public String password;
	public String kind;
	public String title;
	public String description;
	public String company;
	public String service;
	public String direction;
	public List<String> addresses;
	public String town;
	public String zipcode;
	public String business_zipcode;
	public String country;
	public List<String> phones;
	public String mobile;
	public List<String> faxes;
	public String mail_quota = "0";
	public List<String> mails;
	
	/* Read only in OBM */
	public String mail_server;
	public String timecreate = DEFAULT_TIME;
	public String timeupdate = DEFAULT_TIME;
	public List<Object> groups;
	
	
	public User() {}
	
	public User(String mainIdentifier, Map<String, List<Object>> modificationsItems) {
		id = mainIdentifier;
		modify(modificationsItems);
	}

	public void modify(Map<String, List<Object>> modificationsItems) {
		login = (!modificationsItems.containsKey("login")) ? login : modificationsItems.get("login").size() == 0 ? null : String.valueOf(modificationsItems.get("login").get(0)); 
		lastname = (!modificationsItems.containsKey("lastname")) ? lastname : modificationsItems.get("lastname").size() == 0 ? null : String.valueOf(modificationsItems.get("lastname").get(0)); 
		profile = (!modificationsItems.containsKey("profile")) ? profile : modificationsItems.get("profile").size() == 0 ? null : String.valueOf(modificationsItems.get("profile").get(0)); 
		
		firstname = (!modificationsItems.containsKey("firstname")) ? firstname : modificationsItems.get("firstname").size() == 0 ? null : String.valueOf(modificationsItems.get("firstname").get(0)); 
		commonname = (!modificationsItems.containsKey("commonname")) ? commonname : modificationsItems.get("commonname").size() == 0 ? null : String.valueOf(modificationsItems.get("commonname").get(0)); 
		password = (!modificationsItems.containsKey("password")) ? password : modificationsItems.get("password").size() == 0 ? null : String.valueOf(modificationsItems.get("password").get(0)); 
		kind = (!modificationsItems.containsKey("kind")) ? kind : modificationsItems.get("kind").size() == 0 ? null : String.valueOf(modificationsItems.get("kind").get(0)); 
		title = (!modificationsItems.containsKey("title")) ? title : modificationsItems.get("title").size() == 0 ? null : String.valueOf(modificationsItems.get("title").get(0)); 
		description = (!modificationsItems.containsKey("description")) ? description : modificationsItems.get("description").size() == 0 ? null : String.valueOf(modificationsItems.get("description").get(0)); 
		company = (!modificationsItems.containsKey("company")) ? company : modificationsItems.get("company").size() == 0 ? null : String.valueOf(modificationsItems.get("company").get(0)); 
		service = (!modificationsItems.containsKey("service")) ? service : modificationsItems.get("service").size() == 0 ? null : String.valueOf(modificationsItems.get("service").get(0)); 
		direction = (!modificationsItems.containsKey("direction")) ? direction : modificationsItems.get("direction").size() == 0 ? null : String.valueOf(modificationsItems.get("direction").get(0)); 
		addresses = (!modificationsItems.containsKey("addresses")) ? addresses : toStringList(modificationsItems.get("addresses")); 
		town = (!modificationsItems.containsKey("town")) ? town : modificationsItems.get("town").size() == 0 ? null : String.valueOf(modificationsItems.get("town").get(0)); 
		zipcode = (!modificationsItems.containsKey("zipcode")) ? zipcode : modificationsItems.get("zipcode").size() == 0 ? null : String.valueOf(modificationsItems.get("zipcode").get(0)); 
		business_zipcode = (!modificationsItems.containsKey("business_zipcode")) ? business_zipcode : modificationsItems.get("business_zipcode").size() == 0 ? null : String.valueOf(modificationsItems.get("business_zipcode").get(0)); 
		country = (!modificationsItems.containsKey("country")) ? country : modificationsItems.get("country").size() == 0 ? null : String.valueOf(modificationsItems.get("country").get(0)); 
		phones = (!modificationsItems.containsKey("phones")) ? phones : toStringList(modificationsItems.get("phones")); 
		mobile = (!modificationsItems.containsKey("mobile")) ? mobile : modificationsItems.get("mobile").size() == 0 ? null : String.valueOf(modificationsItems.get("mobile").get(0)); 
		faxes = (!modificationsItems.containsKey("faxes")) ? faxes : toStringList(modificationsItems.get("faxes")); 
		mail_quota = (!modificationsItems.containsKey("mail_quota")) ? mail_quota : modificationsItems.get("mail_quota").size() == 0 ? null : String.valueOf(modificationsItems.get("mail_quota").get(0)); 
		mails = (!modificationsItems.containsKey("mails")) ? mails : toStringList(modificationsItems.get("mails")); 

		mail_server = (!modificationsItems.containsKey("mail_server")) ? mail_server : modificationsItems.get("mail_server").size() == 0 ? null : String.valueOf(modificationsItems.get("mail_server").get(0));
		timecreate = (timecreate != null) ? timecreate : DEFAULT_TIME;
		timeupdate = (timeupdate != null) ? timeupdate : DEFAULT_TIME;
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
		datasets.put("login", login);
		datasets.put("lastname", lastname);
		datasets.put("profile", profile);
		
		putIfNotNull(datasets, "firstname", firstname);
		putIfNotNull(datasets, "commonname", commonname);
		putIfNotNull(datasets, "password", password);
		putIfNotNull(datasets, "kind", kind);
		putIfNotNull(datasets, "title", title);
		putIfNotNull(datasets, "description", description);
		putIfNotNull(datasets, "company", company);
		putIfNotNull(datasets, "service", service);
		putIfNotNull(datasets, "direction", direction);
		putIfNotNull(datasets, "addresses", addresses);
		putIfNotNull(datasets, "town", town);
		putIfNotNull(datasets, "zipcode", zipcode);
		putIfNotNull(datasets, "business_zipcode", business_zipcode);
		putIfNotNull(datasets, "country", country);
		putIfNotNull(datasets, "phones", phones);
		putIfNotNull(datasets, "mobile", mobile);
		putIfNotNull(datasets, "faxes", faxes);
		putIfNotNull(datasets, "mail_quota", mail_quota);
		putIfNotNull(datasets, "mails", mails);

		putIfNotNull(datasets, "mail_server", mail_server);
		putIfNotNull(datasets, "timecreate", timecreate);
		putIfNotNull(datasets, "timeupdate", timeupdate);
		putIfNotNull(datasets, "groups", groups);
		
		return datasets;
	}
	
	private void putIfNotNull(LscDatasets datasets, String key, Object value) {
		if (value != null) {
			datasets.put(key, value);
		}
	}
	
	public static List<String> getWritableAttributes() {
		return Arrays.asList("id", "login", "lastname", "profile", "firstname", "commonname", "password",
				"kind", "title", "description", "company", "service", "direction", "addresses", "town",
				"zipcode", "business_zipcode", "country", "phones", "mobile", "faxes", "mail_quota",
				"mail_server", "mails");
	}
}
