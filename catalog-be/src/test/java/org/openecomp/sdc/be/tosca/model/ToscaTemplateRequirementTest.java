/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.tosca.model;

import java.util.Map;

import org.junit.Test;


public class ToscaTemplateRequirementTest {

	private ToscaTemplateRequirement createTestSubject() {
		return new ToscaTemplateRequirement();
	}

	
	@Test
	public void testGetCapability() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	
	@Test
	public void testSetCapability() throws Exception {
		ToscaTemplateRequirement testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	
	@Test
	public void testGetNode() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		ToscaTemplateRequirement testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetRelationship() throws Exception {
		ToscaTemplateRequirement testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	
	@Test
	public void testSetRelationship() throws Exception {
		ToscaTemplateRequirement testSubject;
		String relationship = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationship(relationship);
	}

	
	@Test
	public void testToMap() throws Exception {
		ToscaTemplateRequirement testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toMap();
	}
}
