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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;

import java.util.HashMap;
import java.util.Map;

public class ResourceMetadataDataTest {

	private ResourceMetadataData createTestSubject() {
		return new ResourceMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ResourceMetadataData(new ResourceMetadataDataDefinition());
		new ResourceMetadataData(new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		ResourceMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}
