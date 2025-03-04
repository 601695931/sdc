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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPARATOR_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_MANIFEST;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_PATH_FILE_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;

public class ValidatorFactoryTest {

    private String metaFile;
    private FileContentHandler handler;

    @Before
    public void setUp(){
        handler = new FileContentHandler();
        metaFile =
                "TOSCA-Meta-File-Version: 1.0\n" +
                "CSAR-Version: 1.1\n" +
                "Created-By: Bilal Iqbal\n";
    }

    @Test(expected = IOException.class)
    public void testGivenEmptyMetaFile_thenIOExceptionIsThrown() throws IOException{
        handler.addFile(TOSCA_META_PATH_FILE_NAME, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        ValidatorFactory.getValidator(handler);
    }

    @Test
    public void testGivenEmptyBlock0_thenONAPCsarValidatorIsReturned() throws IOException{
        handler.addFile(TOSCA_META_PATH_FILE_NAME, " ".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }


    @Test
    public void testGivenNonSOL004MetaDirectoryCompliantMetaFile_thenONAPCSARValidatorIsReturned() throws IOException{
        metaFile = metaFile +
                TOSCA_META_ENTRY_DEFINITIONS + SEPARATOR_MF_ATTRIBUTE + TOSCA_DEFINITION_FILEPATH;
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFile.getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }

    @Test
    public void testGivenSOL004MetaDirectoryCompliantMetafile_thenONAPCsarValidatorIsReturned() throws IOException{

        metaFile = metaFile +
                TOSCA_META_ENTRY_DEFINITIONS + SEPARATOR_MF_ATTRIBUTE + TOSCA_DEFINITION_FILEPATH + "\n"
                + TOSCA_META_ETSI_ENTRY_MANIFEST + SEPARATOR_MF_ATTRIBUTE + TOSCA_MANIFEST_FILEPATH + "\n"
                + TOSCA_META_ETSI_ENTRY_CHANGE_LOG + SEPARATOR_MF_ATTRIBUTE + TOSCA_CHANGELOG_FILEPATH + "\n";
        handler.addFile(TOSCA_META_PATH_FILE_NAME, metaFile.getBytes(StandardCharsets.UTF_8));

       assertEquals(SOL004MetaDirectoryValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }

    @Test
    public void testGivenMultiBlockMetadataWithSOL00CompliantMetaFile_thenSOL004MetaDirectoryValidatorReturned() throws IOException {

        handler.addFile(TOSCA_META_PATH_FILE_NAME, ValidatorUtil.getFileResource("/validation.files/metafile/metaFileWithMultipleBlocks.meta"));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(SOL004MetaDirectoryValidator.class, ValidatorFactory.getValidator(handler).getClass());

    }

}
