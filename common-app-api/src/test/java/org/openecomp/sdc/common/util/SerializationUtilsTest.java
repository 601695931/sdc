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

package org.openecomp.sdc.common.util;

import fj.data.Either;
import org.junit.Test;

public class SerializationUtilsTest {

	private SerializationUtils createTestSubject() {
		return new SerializationUtils();
	}

	@Test
	public void testSerialize() throws Exception {
		Object object = null;
		Either<byte[], Boolean> result;

		// default test
		result = SerializationUtils.serialize(object);
	}

	@Test
	public void testDeserialize() throws Exception {
		byte[] bytes = new byte[] { ' ' };
		Either<Object, Boolean> result;

		// default test
		result = SerializationUtils.deserialize(bytes);
	}

	@Test
	public void testSerializeExt() throws Exception {
		Object object = null;
		Either<byte[], Boolean> result;

		// default test
		result = SerializationUtils.serializeExt(object);
	}

}
