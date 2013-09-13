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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.client.WebTarget;

import mockit.Mocked;

import org.lsc.LscDatasetModification;
import org.lsc.LscDatasetModification.LscDatasetModificationType;
import org.lsc.configuration.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractObmDstServiceTest {
    
    private static transient Logger LOGGER = LoggerFactory.getLogger(AbstractObmDstServiceTest.class);

    static int MAX_TRY = 20;
    static int DELAY = 500;
    
	@Mocked TaskType task;
	
	protected void EndAndWaitAndCheckBatchStatus() throws Exception {
		ObmDao.close(task);
		WebTarget batchUrl = ObmDao.batchPathes.get(task);
		Batch batch = null;
		for (int i = 0; i < MAX_TRY; i++) {
            LOGGER.debug("Checking batch: " + batchUrl.getUri());
			batch = batchUrl.request().get(Batch.class);
			if (batch.isFinished()) {
				break;
			}
			Thread.sleep(DELAY);
		}
		if (!batch.isOk()) {
			throw new IllegalStateException("Error or delay depassed on batch:" + batchUrl.getUri());
		}
	}
	
	protected LscDatasetModification addAttribute(String key, Object value) {
		Collection<Object> values = Collections.singleton(value);
		return new LscDatasetModification(LscDatasetModificationType.ADD_VALUES, key, values);
	}
	
	protected LscDatasetModification replaceAttribute(String key, Set<Object> values) {
		return new LscDatasetModification(LscDatasetModificationType.REPLACE_VALUES, key, values);
	}
}
