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

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.onap.sdc.tosca.datatypes.model;

public class PropertyDefinition extends DefinitionOfDataType {

  public PropertyDefinition() {
    setStatus(Status.SUPPORTED);
    setRequired(true);
  }

  @Override
  public PropertyDefinition clone() {
    DefinitionOfDataType definitionOfDataType = super.clone();
    PropertyDefinition propertyDefinition = new PropertyDefinition();
    propertyDefinition.set_default(definitionOfDataType.get_default());
    propertyDefinition.setConstraints(definitionOfDataType.getConstraints());
    propertyDefinition.setDescription(definitionOfDataType.getDescription());
    propertyDefinition.setEntry_schema(definitionOfDataType.getEntry_schema());
    propertyDefinition.setRequired(definitionOfDataType.getRequired());
    propertyDefinition.setType(definitionOfDataType.getType());
    propertyDefinition.setStatus(definitionOfDataType.getStatus());
    propertyDefinition.setValue(definitionOfDataType.getValue());
    return propertyDefinition;
  }


}
