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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("inputsBusinessLogic")
public class InputsBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_INPUT = "CreateInput";
    private static final String UPDATE_INPUT = "UpdateInput";

    private static final Logger log = Logger.getLogger(InputsBusinessLogic.class);
    private static final String FAILED_TO_FOUND_COMPONENT_ERROR = "Failed to found component {}, error: {}";
    private static final String GET_PROPERTIES_BY_INPUT = "get Properties by input";
    private static final String FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR = "Failed to found input {} under component {}, error: {}";
    private static final String GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP = "Going to execute rollback on create group.";
    private static final String GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP = "Going to execute commit on create group.";

    private final PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;

    @Autowired
    public InputsBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
        PropertyDeclarationOrchestrator propertyDeclarationOrchestrator,
        ComponentInstanceBusinessLogic componentInstanceBusinessLogic, DataTypeBusinessLogic dataTypeBusinessLogic,
        ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.propertyDeclarationOrchestrator = propertyDeclarationOrchestrator;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
    }

    /**
     * associate inputs to a given component with paging
     *
     * @param userId
     * @param componentId
     * @return
     */
    public Either<List<InputDefinition>, ResponseFormat> getInputs(String userId, String componentId) {

        validateUserExists(userId, "get Inputs", false);

        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
        List<InputDefinition> inputs = component.getInputs();

        return Either.left(inputs);

    }

    public Either<List<ComponentInstanceInput>, ResponseFormat> getComponentInstanceInputs(String userId, String componentId, String componentInstanceId) {

        validateUserExists(userId, "get Inputs", false);
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreComponentInstancesInputs(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();

        if(!ComponentValidations.validateComponentInstanceExist(component, componentInstanceId)){
            ActionStatus actionStatus = ActionStatus.COMPONENT_INSTANCE_NOT_FOUND;
            log.debug("Failed to found component instance inputs {}, error: {}", componentInstanceId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
		Map<String, List<ComponentInstanceInput>> ciInputs =
				Optional.ofNullable(component.getComponentInstancesInputs()).orElse(Collections.emptyMap());

		// Set Constraints on Input
		MapUtils.emptyIfNull(ciInputs).values()
				.forEach(inputs -> ListUtils.emptyIfNull(inputs)
						.forEach(input -> input.setConstraints(setInputConstraint(input))));
        return Either.left(ciInputs.getOrDefault(componentInstanceId, Collections.emptyList()));
    }

    /**
     * associate properties to a given component instance input
     *
     * @param instanceId
     * @param userId
     * @param inputId
     * @return
     */

    public Either<List<ComponentInstanceProperty>, ResponseFormat> getComponentInstancePropertiesByInputId(String userId, String componentId, String instanceId, String inputId) {
        validateUserExists(userId, GET_PROPERTIES_BY_INPUT, false);
        String parentId = componentId;
        org.openecomp.sdc.be.model.Component component;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);

        if(!instanceId.equals(inputId)){


            Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);

            if(getComponentEither.isRight()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, parentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));

            }
            component = getComponentEither.left().value();
            Optional<ComponentInstance> ciOp = component.getComponentInstances().stream().filter(ci ->ci.getUniqueId().equals(instanceId)).findAny();
            if(ciOp.isPresent()){
                parentId = ciOp.get().getComponentUid();
            }

        }

        filters.setIgnoreInputs(false);

        filters.setIgnoreComponentInstancesProperties(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(parentId, filters);

        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        component = getComponentEither.left().value();

        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if(!op.isPresent()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, parentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }

        return Either.left(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId));

    }

    private Either<String,ResponseFormat> updateInputObjectValue(InputDefinition currentInput, InputDefinition newInput, Map<String, DataTypeDefinition> dataTypes) {
        String innerType = null;
        String propertyType = currentInput.getType();
        ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
        log.debug("The type of the property {} is {}", currentInput.getUniqueId(), propertyType);

        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
            SchemaDefinition def = currentInput.getSchema();
            if (def == null) {
                log.debug("Schema doesn't exists for property of type {}", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE)));
            }
            innerType = propDef.getType();
        }
        // Specific Update Logic

        Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, newInput.getDefaultValue(), true, innerType, dataTypes);

        String newValue = currentInput.getDefaultValue();
        if (isValid.isRight()) {
            Boolean res = isValid.right().value();
            if (Boolean.FALSE.equals(res)) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                    JanusGraphOperationStatus.ILLEGAL_ARGUMENT))));
            }
        } else {
            Object object = isValid.left().value();
            if (object != null) {
                newValue = object.toString();
            }
        }
        return Either.left(newValue);
    }

    private InputDefinition getInputFromInputsListById(List<InputDefinition> componentsOldInputs, InputDefinition input) {
        return componentsOldInputs.stream().filter(in -> in.getUniqueId().equals(input.getUniqueId())).findFirst().orElse(null);
    }

    public Either<List<InputDefinition>, ResponseFormat> updateInputsValue(ComponentTypeEnum componentType, String componentId, List<InputDefinition> inputs, String userId, boolean shouldLockComp, boolean inTransaction) {

        List<InputDefinition> returnInputs = new ArrayList<>();
        Either<List<InputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        try {
            validateUserExists(userId, "get input", false);

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreUsers(false);
			componentParametersView.setIgnoreProperties(false);
			componentParametersView.setIgnoreComponentInstancesProperties(false);
			componentParametersView.setIgnoreComponentInstances(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentType, componentParametersView);

            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();

            if (shouldLockComp) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, UPDATE_INPUT);
                if (lockComponent.isRight()) {
                    result = Either.right(lockComponent.right().value());
                    return result;
                }
            }

            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
            if (canWork.isRight()) {
                result = Either.right(canWork.right().value());
                return result;
            }

			//Validate value and Constraint of input
			Either<Boolean, ResponseFormat> constraintValidatorResponse = validateInputValueConstraint(inputs);
			if (constraintValidatorResponse.isRight()) {
				log.error("Failed validation value and constraint of property: {}",
						constraintValidatorResponse.right().value());
				return Either.right(constraintValidatorResponse.right().value());
			}

            Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
            if (allDataTypes.isRight()) {
                result = Either.right(allDataTypes.right().value());
                return result;
            }

            Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
            List<InputDefinition> componentsOldInputs = Optional.ofNullable(component.getInputs()).orElse(Collections.emptyList());
            for (InputDefinition newInput: inputs) {
                InputDefinition currInput = getInputFromInputsListById(componentsOldInputs, newInput);
                if (currInput == null) {
                    ActionStatus actionStatus = ActionStatus.COMPONENT_NOT_FOUND;
                    log.debug("Failed to found newInput {} under component {}, error: {}", newInput.getUniqueId(), componentId, actionStatus);
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus));
                    return result;
                }
                Either<String, ResponseFormat> updateInputObjectValue = updateInputObjectValue(currInput, newInput, dataTypes);
                if ( updateInputObjectValue.isRight()) {
                    return Either.right(updateInputObjectValue.right().value());
                }
                String newValue = updateInputObjectValue.left().value();
                currInput.setValue(newValue);
                currInput.setDefaultValue(newValue);
                currInput.setOwnerId(userId);
                Either<InputDefinition, StorageOperationStatus> status = toscaOperationFacade.updateInputOfComponent(component, currInput);
                if(status.isRight()){
                    ActionStatus actionStatus = componentsUtils.convertFromStorageResponseForResourceInstanceProperty(status.right().value());
                    result = Either.right(componentsUtils.getResponseFormat(actionStatus, ""));
                    return result;
                } else {
                    returnInputs.add(status.left().value());
                }
            }
            result = Either.left(returnInputs);
            return result;
        } finally {
                if (!inTransaction) {
                    if (result == null || result.isRight()) {
                        log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP);
                        janusGraphDao.rollback();
                    } else {
                        log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP);
                        janusGraphDao.commit();
                    }
                }
                // unlock resource
                if (shouldLockComp && component != null) {
                    graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
                }
            }
    }

    private Either<Boolean, ResponseFormat> validateInputValueConstraint(List<InputDefinition> inputs) {
		PropertyValueConstraintValidationUtil propertyValueConstraintValidationUtil =
				PropertyValueConstraintValidationUtil.getInstance();
		List<InputDefinition> inputDefinitions = new ArrayList<>();
		for (InputDefinition inputDefinition : inputs) {
			InputDefinition inputDef = new InputDefinition();
			inputDefinition.setDefaultValue(inputDefinition.getDefaultValue());
			inputDefinition.setInputPath(inputDefinition.getSubPropertyInputPath());
			inputDefinition.setType(inputDefinition.getType());
			if (Objects.nonNull(inputDefinition.getParentPropertyType())) {
				ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
				propertyDefinition.setType(inputDefinition.getParentPropertyType());

				inputDefinition.setProperties(Collections.singletonList(propertyDefinition));
			}

			inputDefinitions.add(inputDef);
		}

		return propertyValueConstraintValidationUtil.validatePropertyConstraints(inputDefinitions, applicationDataTypeCache);
	}

    public Either<List<ComponentInstanceInput>, ResponseFormat> getInputsForComponentInput(String userId, String componentId, String inputId) {
        validateUserExists(userId, GET_PROPERTIES_BY_INPUT, false);
        org.openecomp.sdc.be.model.Component component = null;
        ComponentParametersView filters = new ComponentParametersView();
        filters.disableAll();
        filters.setIgnoreComponentInstances(false);
        filters.setIgnoreInputs(false);
        filters.setIgnoreComponentInstancesInputs(false);
        filters.setIgnoreProperties(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);

        if(getComponentEither.isRight()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));

        }
        component = getComponentEither.left().value();

        Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
        if(!op.isPresent()){
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
            log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, componentId, actionStatus);
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }

        return Either.left(componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId));

    }

    @Override
    public Either<List<InputDefinition>, ResponseFormat> declareProperties(String userId, String componentId,
            ComponentTypeEnum componentTypeEnum, ComponentInstInputsMap componentInstInputsMap) {

        return createMultipleInputs(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
    }

    public Either<List<InputDefinition>, ResponseFormat> createMultipleInputs(String userId, String componentId, ComponentTypeEnum componentType, ComponentInstInputsMap componentInstInputsMapUi, boolean shouldLockComp, boolean inTransaction) {

        Either<List<InputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        try {
            validateUserExists(userId, GET_PROPERTIES_BY_INPUT, false);

            component = getAndValidateComponentForCreate(userId, componentId, componentType, shouldLockComp);

            result =  propertyDeclarationOrchestrator.declarePropertiesToInputs(component, componentInstInputsMapUi)
                    .left()
                    .bind(inputsToCreate -> prepareInputsForCreation(userId, componentId, inputsToCreate))
                    .right()
					.map(componentsUtils::getResponseFormat);

            return result;

        } catch (ByResponseFormatComponentException e) {
            log.error("#createMultipleInputs: Exception thrown: ", e);
            result = Either.right(e.getResponseFormat());
            return result;
        } finally {

            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP);
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }

        }
    }

    /**
     * Creates a list input with a data type which has properties specified.
     *
     * @param userId User ID
     * @param componentId Component ID
     * @param componentType Component type
     * @param componentListInput Properties to be declared and input to be created
     * @param shouldLockComp true if the component should be locked
     * @param inTransaction true if already in transaction
     */
    public Either<List<InputDefinition>, ResponseFormat> createListInput(String userId, String componentId,
        ComponentTypeEnum componentType, ComponentInstListInput componentListInput, boolean shouldLockComp,
        boolean inTransaction) {

        Either<List<InputDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        log.trace("#createListInput: enter");

        try {
            /* check if user exists */
            validateUserExists(userId, GET_PROPERTIES_BY_INPUT, false);

            component = getAndValidateComponentForCreate(userId, componentId, componentType, shouldLockComp);

            InputDefinition listInput = componentListInput.getListInput();
            DataTypeDefinition dataType =
                prepareDataTypeForListInput(componentListInput.getComponentInstInputsMap(), listInput);
            Map<String, DataTypeDefinition> dataTypesMap = new HashMap<>();
            dataTypesMap.put(dataType.getName(), dataType);
            if (log.isDebugEnabled()) {
                log.debug("#createListInput: dataTypesMap={}", ReflectionToStringBuilder.toString(dataTypesMap));
            }

            Either<List<DataTypeDefinition>, StorageOperationStatus> dataTypeResult =
                toscaOperationFacade.addDataTypesToComponent(dataTypesMap, componentId);
            if (dataTypeResult.isRight()) {
                log.debug("#createListInput: DataType creation failed.");
                throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(dataTypeResult.right().value()));
            }

            // create list input
            listInput.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, listInput.getName()));
            listInput.setInstanceUniqueId(
                propertyDeclarationOrchestrator.getPropOwnerId(componentListInput.getComponentInstInputsMap()));
            listInput.setIsDeclaredListInput(true);
            Map<String, InputDefinition> listInputMap = new HashMap<>();
            listInputMap.put(listInput.getName(), listInput);
            result = createListInputsInGraph(listInputMap, dataTypesMap, component);
            if (result.isRight()) {
                log.debug("#createListInput: createListInputsInGraph failed.");
                throw new ByResponseFormatComponentException(result.right().value());
            }

            // update properties
            result = propertyDeclarationOrchestrator
                .declarePropertiesToListInput(component, componentListInput.getComponentInstInputsMap(), listInput)
                .right().map(err -> componentsUtils.getResponseFormat(err))
                .left().map(Arrays::asList);

            log.trace("#createListInput: leave");

            return result;

        } catch (ByResponseFormatComponentException e) {
            log.error("#createListInput: Exception thrown", e);
            result = Either.right(e.getResponseFormat());
            return result;
        } finally {

            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP);
                    janusGraphDao.commit();
                }
            }
            // unlock resource
            if (shouldLockComp && component != null) {
                graphLockOperation.unlockComponent(componentId, componentType.getNodeType());
            }
        }
    }

    private ComponentParametersView getBaseComponentParametersView() {
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreInputs(false);
        componentParametersView.setIgnoreComponentInstances(false);
        componentParametersView.setIgnoreComponentInstancesInputs(false);
        componentParametersView.setIgnoreComponentInstancesProperties(false);
        componentParametersView.setIgnorePolicies(false);
        componentParametersView.setIgnoreGroups(false);
        componentParametersView.setIgnoreUsers(false);
        return componentParametersView;
    }

    private org.openecomp.sdc.be.model.Component getAndValidateComponentForCreate(
        String userId, String componentId, ComponentTypeEnum componentType, boolean shouldLockComp
    ) {

        ComponentParametersView componentParametersView = getBaseComponentParametersView();

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither =
            // get Component Object
            validateComponentExists(componentId, componentType, componentParametersView)
            .left().bind(component -> {
                if (shouldLockComp) {
                    // lock the component
                    return lockComponent(component, CREATE_INPUT).left().map(result -> component);
                }
                return Either.left(component);
            }).left().bind(component -> validateCanWorkOnComponent(component, userId).left().map(result -> component));
        if (componentEither.isRight()) {
            throw new ByResponseFormatComponentException(componentEither.right().value());
        }
        return componentEither.left().value();
    }

    private DataTypeDefinition prepareDataTypeForListInput(ComponentInstInputsMap inputsMap, InputDefinition input) {
        // Confirm if type is list
        if (StringUtils.isEmpty(input.getType()) || !input.getType().equals(ToscaPropertyType.LIST.getType())) {
            log.debug("#prepareDataTypeForListInput: Type of input is not list.");
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE));
        }

        // Confirm schema type is not empty
        String desiredTypeName = input.getSchemaType();
        if (StringUtils.isEmpty(desiredTypeName)) {
            log.debug("#prepareDataTypeForListInput: Schema type of list input is empty.");
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE));
        }

        DataTypeDefinition dataType = new DataTypeDefinition();
        List<ComponentInstancePropInput> propInputs = inputsMap.resolvePropertiesToDeclare().getRight();
        dataType.setName(desiredTypeName);
        dataType.setDerivedFromName(ToscaPropertyType.Root.getType());
        // Copy properties from inputsMap
        dataType.setProperties(propInputs.stream().map(PropertyDefinition::new).collect(Collectors.toList()));
        return dataType;
    }

    private  Either<List<InputDefinition>, StorageOperationStatus> prepareInputsForCreation(String userId, String cmptId, List<InputDefinition> inputsToCreate) {
        Map<String, InputDefinition> inputsToPersist = MapUtil.toMap(inputsToCreate, InputDefinition::getName);
        assignOwnerIdToInputs(userId, inputsToPersist);
		inputsToPersist.values()
				.forEach(input -> input.setConstraints(componentInstanceBusinessLogic.setInputConstraint(input)));

        return toscaOperationFacade.addInputsToComponent(inputsToPersist, cmptId)
                .left()
                .map(persistedInputs -> inputsToCreate);
    }

    private void assignOwnerIdToInputs(String userId, Map<String, InputDefinition> inputsToCreate) {
        inputsToCreate.values().forEach(inputDefinition -> inputDefinition.setOwnerId(userId));
    }

    public Either<List<InputDefinition>, ResponseFormat> createInputsInGraph(Map<String, InputDefinition> inputs, org.openecomp.sdc.be.model.Component component) {

        List<InputDefinition> resourceProperties = component.getInputs();
        Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
        if (allDataTypes.isRight()) {
            return Either.right(allDataTypes.right().value());
        }

        Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

        for (Map.Entry<String, InputDefinition> inputDefinition : inputs.entrySet()) {
            String inputName = inputDefinition.getKey();
            inputDefinition.getValue().setName(inputName);

            Either<InputDefinition, ResponseFormat> preparedInputEither = prepareAndValidateInputBeforeCreate(inputDefinition.getValue(), dataTypes);
            if(preparedInputEither.isRight()){
                return Either.right(preparedInputEither.right().value());
            }

        }
        if (resourceProperties != null) {
            Map<String, InputDefinition> generatedInputs = resourceProperties.stream().collect(Collectors.toMap(PropertyDataDefinition::getName, i -> i));
            Either<Map<String, InputDefinition>, String> mergeEither = ToscaDataDefinition.mergeDataMaps(generatedInputs, inputs);
            if(mergeEither.isRight()){
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, mergeEither.right().value()));
            }
            inputs = mergeEither.left().value();
        }

        Either<List<InputDefinition>, StorageOperationStatus> associateInputsEither = toscaOperationFacade.createAndAssociateInputs(inputs, component.getUniqueId());
        if(associateInputsEither.isRight()){
            log.debug("Failed to create inputs under component {}. Status is {}", component.getUniqueId(), associateInputsEither.right().value());
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(associateInputsEither.right().value())));
        }
        return Either.left(associateInputsEither.left().value());
    }

    private Either<List<InputDefinition>, ResponseFormat> createListInputsInGraph(Map<String, InputDefinition> inputs,
        Map<String, DataTypeDefinition> privateDataTypes, org.openecomp.sdc.be.model.Component component) {

        log.trace("#createListInputsInGraph: enter");
        Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(
            applicationDataTypeCache);
        if (allDataTypes.isRight()) {
            return Either.right(allDataTypes.right().value());
        }

        Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
        dataTypes.putAll(privateDataTypes);

        for (Map.Entry<String, InputDefinition> inputDefinition : inputs.entrySet()) {
            String inputName = inputDefinition.getKey();
            inputDefinition.getValue().setName(inputName);

            Either<InputDefinition, ResponseFormat> preparedInputEither =
                prepareAndValidateInputBeforeCreate(inputDefinition.getValue(), dataTypes);
            if (preparedInputEither.isRight()) {
                return Either.right(preparedInputEither.right().value());
            }
        }

        Either<List<InputDefinition>, StorageOperationStatus> addInputsEither = toscaOperationFacade
            .addInputsToComponent(inputs, component.getUniqueId());
        if (addInputsEither.isRight()) {
            log.debug("#createListInputsInGraph: Failed to create inputs under component {}. Status is {}",
                component.getUniqueId(), addInputsEither.right().value());
            return Either.right(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(addInputsEither.right().value())));
        }
        log.trace("#createListInputsInGraph: leave");
        return Either.left(addInputsEither.left().value());
    }

    /**
     * Delete input from service
     *
     * @param componentId
     * @param userId
     * @param inputId
     * @return
     */
    public Either<InputDefinition, ResponseFormat> deleteInput(String componentId, String userId, String inputId) {

        Either<InputDefinition, ResponseFormat> deleteEither = null;
        if (log.isDebugEnabled()) {
            log.debug("Going to delete input id: {}", inputId);
        }

        validateUserExists(userId, "Delete input", true);

        ComponentParametersView componentParametersView = getBaseComponentParametersView();
        componentParametersView.setIgnoreInterfaces(false);
        componentParametersView.setIgnoreDataType(false);
        componentParametersView.setIgnoreProperties(false);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentEither =
            toscaOperationFacade.getToscaElement(componentId, componentParametersView);
        if (componentEither.isRight()) {
            deleteEither = Either.right(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(componentEither.right().value())));
            return deleteEither;
        }
        org.openecomp.sdc.be.model.Component component = componentEither.left().value();

        // Validate inputId is child of the component
        Optional<InputDefinition> optionalInput = component.getInputs().stream().
                // filter by ID
                        filter(input -> input.getUniqueId().equals(inputId)).
                // Get the input
                        findAny();
        if (!optionalInput.isPresent()) {
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.INPUT_IS_NOT_CHILD_OF_COMPONENT, inputId, componentId));
        }

        InputDefinition inputForDelete = optionalInput.get();

        // Lock component
        Either<Boolean, ResponseFormat> lockResultEither =
            lockComponent(componentId, component, "deleteInput");
        if (lockResultEither.isRight()) {
            ResponseFormat responseFormat = lockResultEither.right().value();
            deleteEither = Either.right(responseFormat);
            return deleteEither;
        }

        // Delete input operations
        try {
            StorageOperationStatus status =
                toscaOperationFacade.deleteInputOfResource(component, inputForDelete.getName());
            if (status != StorageOperationStatus.OK) {
                log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
                deleteEither = Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(status), component.getName()));
                return deleteEither;
            }

            if (BooleanUtils.isTrue(inputForDelete.getIsDeclaredListInput())){
                deleteEither = deleteListInput(componentId, inputId, component, inputForDelete, status);
                return deleteEither;
            }

            StorageOperationStatus storageOperationStatus =
                propertyDeclarationOrchestrator.unDeclarePropertiesAsInputs(component, inputForDelete);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.debug("Component id: {} update properties declared as input for input id: {} failed", componentId, inputId);
                deleteEither = Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(status), component.getName()));
                return deleteEither;
            }

            deleteEither = Either.left(inputForDelete);
            return deleteEither;
        } finally {
            if (deleteEither == null || deleteEither.isRight()) {
                log.debug("Component id: {} delete input id: {} failed", componentId, inputId);
                janusGraphDao.rollback();
            } else {
                log.debug("Component id: {} delete input id: {} success", componentId, inputId);
                janusGraphDao.commit();
            }
            unlockComponent(deleteEither, component);
        }
    }

    private Either<InputDefinition, ResponseFormat> deleteListInput(String componentId, String inputId,
        org.openecomp.sdc.be.model.Component component,
        InputDefinition inputForDelete, StorageOperationStatus status) {
        // the input is created by 'Declare List'.
        // need to 1. undeclare properties, 2. delete input, 3. delete private data type

        StorageOperationStatus storageOperationStatus =
            propertyDeclarationOrchestrator.unDeclarePropertiesAsListInputs(component, inputForDelete);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            log.debug("Component id: {} update properties declared as input for input id: {} failed", componentId, inputId);
            return Either.right(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(status), component.getName()));
        }
        Either<DataTypeDefinition, StorageOperationStatus> deleteResult =
            dataTypeBusinessLogic.deletePrivateDataType(component, inputForDelete.getSchemaType());
        if (deleteResult.isRight()) {
            log.debug("Component id: {} delete datatype name: {} failed", componentId, inputForDelete.getSchemaType());
            return Either.right(componentsUtils.getResponseFormat(
                componentsUtils.convertFromStorageResponse(deleteResult.right().value()), component.getName()));
        }
        log.trace("deleteInput: deletePrivateDataType (OK)");
        return Either.left(inputForDelete);
    }

    private Either<InputDefinition, ResponseFormat> prepareAndValidateInputBeforeCreate(InputDefinition newInputDefinition, Map<String, DataTypeDefinition> dataTypes) {

        // validate input default values
        Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newInputDefinition, dataTypes);
        if (defaultValuesValidation.isRight()) {
            return Either.right(defaultValuesValidation.right().value());
        }
        // convert property
        ToscaPropertyType type = getType(newInputDefinition.getType());
        if (type != null && newInputDefinition != null) {
            PropertyValueConverter converter = type.getConverter();
            // get inner type
            SchemaDefinition schema = newInputDefinition.getSchema();
            String innerType = null;
            if (schema != null) {
                PropertyDataDefinition prop = schema.getProperty();
                if (prop != null) {
                    innerType = prop.getType();
                }
            }
            String convertedValue;
            if (newInputDefinition.getDefaultValue() != null) {
                convertedValue = converter.convert(newInputDefinition.getDefaultValue(), innerType, dataTypes);
                newInputDefinition.setDefaultValue(convertedValue);
            }
        }
        return Either.left(newInputDefinition);
    }

    public Either<InputDefinition, ResponseFormat> getInputsAndPropertiesForComponentInput(String userId, String componentId, String inputId, boolean inTransaction) {
        Either<InputDefinition, ResponseFormat> result = null;
        try {

            validateUserExists(userId, GET_PROPERTIES_BY_INPUT, false);
            ComponentParametersView filters = new ComponentParametersView();
            filters.disableAll();
            filters.setIgnoreComponentInstances(false);
            filters.setIgnoreInputs(false);
            filters.setIgnoreComponentInstancesInputs(false);
            filters.setIgnoreComponentInstancesProperties(false);
            filters.setIgnoreProperties(false);
            Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentEither = toscaOperationFacade.getToscaElement(componentId, filters);
            if(getComponentEither.isRight()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_COMPONENT_ERROR, componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));

            }
            org.openecomp.sdc.be.model.Component component = getComponentEither.left().value();
            Optional<InputDefinition> op = component.getInputs().stream().filter(in -> in.getUniqueId().equals(inputId)).findFirst();
            if(!op.isPresent()){
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getComponentEither.right().value());
                log.debug(FAILED_TO_FOUND_INPUT_UNDER_COMPONENT_ERROR, inputId, componentId, actionStatus);
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }

            InputDefinition resObj = op.get();

            List<ComponentInstanceInput> inputCIInput = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, inputId) ;

            resObj.setInputs(inputCIInput);


            List<ComponentInstanceProperty> inputProps = componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, inputId) ;

            resObj.setProperties(inputProps);


            result = Either.left(resObj);

            return result;

        } finally {

            if (!inTransaction) {

                if (result == null || result.isRight()) {
                    log.debug(GOING_TO_EXECUTE_ROLLBACK_ON_CREATE_GROUP);
                    janusGraphDao.rollback();
                } else {
                    log.debug(GOING_TO_EXECUTE_COMMIT_ON_CREATE_GROUP);
                    janusGraphDao.commit();
                }

            }

        }

    }

}
