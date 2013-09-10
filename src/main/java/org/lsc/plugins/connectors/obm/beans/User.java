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

import static org.lsc.plugins.connectors.obm.ModificationsItemsUtils.*;

import java.util.List;
import java.util.Map;

import org.lsc.LscDatasets;


public class User implements Identifiable {
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
	public boolean archived;
	public boolean hidden;
	
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
		login = getIfNotNull(modificationsItems, "login", login);
		lastname = getIfNotNull(modificationsItems, "lastname", lastname); 
		profile = getIfNotNull(modificationsItems, "profile", profile);
		
		firstname = getIfNotNull(modificationsItems, "firstname", firstname);
		commonname = getIfNotNull(modificationsItems, "commonname", commonname); 
		password = getIfNotNull(modificationsItems, "password", password);
		kind = getIfNotNull(modificationsItems, "kind", kind);
		title = getIfNotNull(modificationsItems, "title", title);
		description = getIfNotNull(modificationsItems, "description", description);
		company = getIfNotNull(modificationsItems, "company", company);
		service = getIfNotNull(modificationsItems, "service", service);
		direction = getIfNotNull(modificationsItems, "direction", direction);
		addresses = getIfNotNull(modificationsItems, "addresses", addresses);
		town = getIfNotNull(modificationsItems, "town", town);
		zipcode = getIfNotNull(modificationsItems, "zipcode", zipcode);
		business_zipcode = getIfNotNull(modificationsItems, "business_zipcode", business_zipcode);
		country = getIfNotNull(modificationsItems, "country", country);
		phones = getIfNotNull(modificationsItems, "phones", phones);
		mobile = getIfNotNull(modificationsItems, "mobile", mobile);
		faxes = getIfNotNull(modificationsItems, "faxes", faxes);
		mail_quota = getIfNotNull(modificationsItems, "mail_quota", mail_quota);
		mails = getIfNotNull(modificationsItems, "mails", mails);
		archived = getIfNotNull(modificationsItems, "archived", archived);
		hidden = getIfNotNull(modificationsItems, "hidden", hidden);

		timecreate = (timecreate != null) ? timecreate : DEFAULT_TIME;
		timeupdate = (timeupdate != null) ? timeupdate : DEFAULT_TIME;
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
		putIfNotNull(datasets, "archived", Boolean.toString(archived));
		putIfNotNull(datasets, "hidden", Boolean.toString(hidden));

		putIfNotNull(datasets, "mail_server", mail_server);
		putIfNotNull(datasets, "timecreate", timecreate);
		putIfNotNull(datasets, "timeupdate", timeupdate);
		putIfNotNull(datasets, "groups", groups);
		
		return datasets;
	}
	
	public String getId() {
		return id;
	}
}
