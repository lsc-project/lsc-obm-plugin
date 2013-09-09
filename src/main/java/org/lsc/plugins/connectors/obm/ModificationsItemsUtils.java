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

import org.lsc.LscDatasets;
import org.lsc.plugins.connectors.obm.beans.Identifiable;

public class ModificationsItemsUtils {
	
	public static String getIfNotNull(Map<String, List<Object>> modificationsItems, String key, String value) {
		if (modificationsItems.containsKey(key)) {
			if (modificationsItems.get(key).size() == 0) {
				return null;
			} else {
				return String.valueOf(modificationsItems.get(key).get(0));
			}
		} else {
			return value;
		}
	}

	public static List<String> getIfNotNull(Map<String, List<Object>> modificationsItems, String key, List<String> value) {
		if (modificationsItems.containsKey(key)) {
			return toStringList(modificationsItems.get(key));
		} else {
			return value;
		}
	}

	public static boolean getIfNotNull(Map<String, List<Object>> modificationsItems, String key, boolean value) {
		if (modificationsItems.containsKey(key)) {
			if (modificationsItems.get(key).size() == 0) {
				return false;
			} else {
				return Boolean.valueOf(String.valueOf(modificationsItems.get(key).get(0)));
			}
		} else {
			return value;
		}
	}

	public static List<String> toStringList(List<Object> uncastedValues) {
		List<String> values = new ArrayList<String>(uncastedValues.size());
		for (Object object: uncastedValues) {
			values.add(String.valueOf(object));
		}
		return values;
	}

	public static void putIfNotNull(LscDatasets datasets, String key, Object value) {
		if (value != null) {
			datasets.put(key, value);
		}
	}
	
	public static List<String> memberListToIdList(List<? extends Identifiable> memberList) {
		List<String> memberIds = new ArrayList<String>(memberList.size());
		for (Identifiable member: memberList) {
			String userId = String.valueOf(member.getId());
			memberIds.add(userId);
		}
		return memberIds;
	}
	
}
