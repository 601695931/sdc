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

package org.openecomp.sdc.common.transaction.mngr;

import org.junit.Test;
import org.openecomp.sdc.common.transaction.api.ITransactionSdnc;
import org.openecomp.sdc.common.transaction.api.TransactionUtils.ActionTypeEnum;

public class TransactionManagerTest {

	private TransactionManager createTestSubject() {
		return new TransactionManager();
	}

	
	@Test
	public void testGetTransaction() throws Exception {
		TransactionManager testSubject;
		String userId = "";
		ActionTypeEnum actionType = null;
		ITransactionSdnc result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGenerateTransactionID() throws Exception {
		TransactionManager testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testResetTransactionId() throws Exception {
		TransactionManager testSubject;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testInit() throws Exception {
		TransactionManager testSubject;

		// default test
		testSubject = createTestSubject();
	}
}
