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

package org.openecomp.sdc.be.model;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;


public class UploadPropInfoTest {

	private UploadPropInfo createTestSubject() {
		return new UploadPropInfo();
	}

	
	@Test
	public void testGetGet_input() throws Exception {
		UploadPropInfo testSubject;
		List<GetInputValueDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGet_input();
	}

	
	@Test
	public void testSetGet_input() throws Exception {
		UploadPropInfo testSubject;
		List<GetInputValueDataDefinition> get_input = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGet_input(get_input);
	}

	
	@Test
	public void testGetValue() throws Exception {
		UploadPropInfo testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	
	@Test
	public void testSetValue() throws Exception {
		UploadPropInfo testSubject;
		Object value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		UploadPropInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		UploadPropInfo testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testIsPassword() throws Exception {
		UploadPropInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isPassword();
	}

	
	@Test
	public void testSetPassword() throws Exception {
		UploadPropInfo testSubject;
		boolean password = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setPassword(password);
	}
}
