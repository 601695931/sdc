/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.impl.exceptions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.exception.ResponseFormat;

public class ComponentExceptionTest {

	private static final String[] PARAMS = {"param1", "param2"};

	@Test
	public void hasValidGettersForActionStatus() {
		ByActionStatusComponentException componentException = new ByActionStatusComponentException(
			ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, PARAMS);
		assertEquals(componentException.getActionStatus(), ActionStatus.AAI_ARTIFACT_GENERATION_FAILED);
		assertArrayEquals(componentException.getParams(), PARAMS);
	}


	@Test
	public void hasValidGettersForResponseFormat() {
		ResponseFormat responseFormat = new ResponseFormat();
		ByResponseFormatComponentException componentException = new ByResponseFormatComponentException(responseFormat);
		assertEquals(componentException.getResponseFormat(), responseFormat);
	}

}