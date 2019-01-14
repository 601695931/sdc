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


package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.components.utils.InterfaceOperationUtils;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("interfaceOperationBusinessLogic")
public class InterfaceOperationBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceOperationBusinessLogic.class);
    private static final String EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION =
            "Exception occurred during {}. Response is {}";
    private static final String DELETE_INTERFACE_OPERATION = "deleteInterfaceOperation";
    private static final String GET_INTERFACE_OPERATION = "getInterfaceOperation";
    private static final String CREATE_INTERFACE_OPERATION = "createInterfaceOperation";
    private static final String UPDATE_INTERFACE_OPERATION = "updateInterfaceOperation";

    @Autowired
    private ArtifactCassandraDao artifactCassandraDao;

    @Autowired
    private InterfaceOperationValidation interfaceOperationValidation;

    public Either<List<InterfaceDefinition>, ResponseFormat> deleteInterfaceOperation(String componentId,
            String interfaceId, List<String> operationsToDelete, User user, boolean lock) {
        validateUserExists(user.getUserId(), DELETE_INTERFACE_OPERATION, true);

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult =
                lockComponentResult(lock, storedComponent, DELETE_INTERFACE_OPERATION);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceOperationUtils
                                                                      .getInterfaceDefinitionFromComponentByInterfaceId(
                                                                              storedComponent, interfaceId);
            if (!optionalInterface.isPresent()) {
                return Either.right(
                        componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceId));
            }
            InterfaceDefinition interfaceDefinition = optionalInterface.get();

            Map<String, Operation> operationsCollection = new HashMap<>();
            for (String operationId : operationsToDelete) {
                Optional<Map.Entry<String, Operation>> optionalOperation =
                        InterfaceOperationUtils.getOperationFromInterfaceDefinition(interfaceDefinition, operationId);
                if (!optionalOperation.isPresent()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND,
                            storedComponent.getUniqueId()));
                }

                Operation storedOperation = optionalOperation.get().getValue();
                String artifactUuId = storedOperation.getImplementation().getArtifactUUID();
                CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUuId);
                if (cassandraStatus != CassandraOperationStatus.OK) {
                    ResponseFormat responseFormatByArtifactId = componentsUtils.getResponseFormatByArtifactId(
                            componentsUtils.convertFromStorageResponse(
                                    componentsUtils.convertToStorageOperationStatus(cassandraStatus)),
                            storedOperation.getImplementation().getArtifactDisplayName());
                    return Either.right(responseFormatByArtifactId);
                }

                operationsCollection.put(operationId, interfaceDefinition.getOperationsMap().get(operationId));
                interfaceDefinition.getOperations().remove(operationId);
            }

            Either<List<InterfaceDefinition>, StorageOperationStatus> deleteOperationEither =
                    interfaceOperation.updateInterfaces(storedComponent.getUniqueId(),
                            Collections.singletonList(interfaceDefinition));
            if (deleteOperationEither.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                        deleteOperationEither.right().value(), storedComponent.getComponentType())));
            }

            if (interfaceDefinition.getOperations().isEmpty()) {
                Either<String, StorageOperationStatus> deleteInterfaceEither = interfaceOperation.deleteInterface(
                        storedComponent.getUniqueId(), interfaceDefinition.getUniqueId());
                if (deleteInterfaceEither.isRight()) {
                    titanDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                            deleteInterfaceEither.right().value(), storedComponent.getComponentType())));
                }
            }

            titanDao.commit();
            interfaceDefinition.getOperations().putAll(operationsCollection);
            interfaceDefinition.getOperations().keySet().removeIf(key -> !(operationsToDelete.contains(key)));
            return Either.left(Collections.singletonList(interfaceDefinition));
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "delete", e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_DELETED));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> getComponentDetails(String componentId) {
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentStorageOperationStatusEither =
                toscaOperationFacade.getToscaElement(componentId);
        if (componentStorageOperationStatusEither.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(componentStorageOperationStatusEither.right().value())));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Boolean, ResponseFormat> lockComponentResult(boolean lock,
            org.openecomp.sdc.be.model.Component component, String action) {
        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(component.getUniqueId(), component, action);
            if (lockResult.isRight()) {
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }
        return Either.left(true);
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> getInterfaceOperation(String componentId,
            String interfaceId, List<String> operationsToGet, User user, boolean lock) {
        validateUserExists(user.getUserId(), GET_INTERFACE_OPERATION, true);

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult =
                lockComponentResult(lock, storedComponent, GET_INTERFACE_OPERATION);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        try {
            Optional<InterfaceDefinition> optionalInterface = InterfaceOperationUtils
                                                                      .getInterfaceDefinitionFromComponentByInterfaceId(
                                                                              storedComponent, interfaceId);
            if (!optionalInterface.isPresent()) {
                return Either.right(
                        componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT, interfaceId));
            }
            InterfaceDefinition interfaceDefinition = optionalInterface.get();

            for (String operationId : operationsToGet) {
                Optional<Map.Entry<String, Operation>> optionalOperation =
                        InterfaceOperationUtils.getOperationFromInterfaceDefinition(interfaceDefinition, operationId);
                if (!optionalOperation.isPresent()) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND,
                            storedComponent.getUniqueId()));
                }
            }

            titanDao.commit();
            interfaceDefinition.getOperations().keySet().removeIf(key -> !(operationsToGet.contains(key)));
            return Either.left(Collections.singletonList(interfaceDefinition));
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "get", e);
            titanDao.rollback();
            return Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND, componentId));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> createInterfaceOperation(String componentId,
            List<InterfaceDefinition> interfaceDefinitions, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, interfaceDefinitions, user, false,
                CREATE_INTERFACE_OPERATION, lock);
    }

    private Either<List<InterfaceDefinition>, ResponseFormat> createOrUpdateInterfaceOperation(String componentId,
            List<InterfaceDefinition> interfaceDefinitions, User user, boolean isUpdate, String errorContext,
            boolean lock) {
        validateUserExists(user.getUserId(), errorContext, true);

        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        Either<Map<String, InterfaceDefinition>, ResponseFormat> interfaceLifecycleTypes =
                getAllInterfaceLifecycleTypes();
        if (interfaceLifecycleTypes.isRight()) {
            return Either.right(interfaceLifecycleTypes.right().value());
        }

        try {
            List<InterfaceDefinition> interfacesCollection = new ArrayList<>();
            Map<String, Operation> operationsCollection = new HashMap<>();
            for (InterfaceDefinition inputInterfaceDefinition : interfaceDefinitions) {
                Optional<InterfaceDefinition> optionalInterface =
                        InterfaceOperationUtils.getInterfaceDefinitionFromComponentByInterfaceType(
                                storedComponent, inputInterfaceDefinition.getType());
                Either<Boolean, ResponseFormat> interfaceOperationValidationResponseEither =
                        interfaceOperationValidation
                                .validateInterfaceOperations(inputInterfaceDefinition, storedComponent,
                                        optionalInterface.orElse(null), interfaceLifecycleTypes.left().value(),
                                        isUpdate);
                if (interfaceOperationValidationResponseEither.isRight()) {
                    return Either.right(interfaceOperationValidationResponseEither.right().value());
                }

                Map<String, Operation> operationsToAddOrUpdate = inputInterfaceDefinition.getOperationsMap();
                operationsCollection.putAll(operationsToAddOrUpdate);
                inputInterfaceDefinition.getOperations().clear();

                Either<InterfaceDefinition, ResponseFormat> getInterfaceEither =
                        getOrCreateInterfaceDefinition(storedComponent, inputInterfaceDefinition,
                                optionalInterface.orElse(null));
                if (getInterfaceEither.isRight()) {
                    return Either.right(getInterfaceEither.right().value());
                }
                InterfaceDefinition interfaceDef = getInterfaceEither.left().value();

                updateOperationInputDefs(storedComponent, operationsToAddOrUpdate.values());

                for (Operation operation : operationsToAddOrUpdate.values()) {
                    if (!isUpdate) {
                        addOperationToInterface(interfaceDef, operation);
                    } else {
                        Optional<Map.Entry<String, Operation>> optionalOperation =
                                InterfaceOperationUtils.getOperationFromInterfaceDefinition(interfaceDef,
                                        operation.getUniqueId());
                        if (!optionalOperation.isPresent()) {
                            titanDao.rollback();
                            return Either.right(componentsUtils
                                                        .getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND,
                                                                storedComponent.getUniqueId()));
                        }

                        Operation storedOperation = optionalOperation.get().getValue();
                        String artifactUuId = storedOperation.getImplementation().getArtifactUUID();
                        Either<Long, CassandraOperationStatus> artifactCount =
                                artifactCassandraDao.getCountOfArtifactById(artifactUuId);
                        if (artifactCount.isLeft()) {
                            CassandraOperationStatus cassandraStatus =
                                    artifactCassandraDao.deleteArtifact(artifactUuId);
                            if (cassandraStatus != CassandraOperationStatus.OK) {
                                titanDao.rollback();
                                ResponseFormat responseFormatByArtifactId =
                                        componentsUtils.getResponseFormatByArtifactId(
                                                componentsUtils.convertFromStorageResponse(
                                                        componentsUtils.convertToStorageOperationStatus(
                                                                cassandraStatus)),
                                                storedOperation.getImplementation().getArtifactDisplayName());
                                return Either.right(responseFormatByArtifactId);
                            }
                        }
                        updateOperationOnInterface(interfaceDef, operation, artifactUuId);
                    }
                }
                interfacesCollection.add(interfaceDef);
            }

            Either<List<InterfaceDefinition>, StorageOperationStatus> addCreateOperationEither =
                    interfaceOperation.updateInterfaces(storedComponent.getUniqueId(), interfacesCollection);
            if (addCreateOperationEither.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                        addCreateOperationEither.right().value(), storedComponent.getComponentType())));
            }

            titanDao.commit();
            interfacesCollection.forEach(interfaceDefinition -> interfaceDefinition.getOperations().entrySet().removeIf(
                    entry -> !operationsCollection.values().stream().map(OperationDataDefinition::getName)
                                      .collect(Collectors.toList()).contains(entry.getValue().getName())));
            return Either.left(interfacesCollection);
        } catch (Exception e) {
            titanDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_INTERFACE_OPERATION, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<Map<String, InterfaceDefinition>, ResponseFormat> getAllInterfaceLifecycleTypes() {

        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> interfaceLifecycleTypes =
                interfaceLifecycleTypeOperation.getAllInterfaceLifecycleTypes();
        if (interfaceLifecycleTypes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INTERFACE_LIFECYCLE_TYPES_NOT_FOUND));
        }
        interfaceLifecycleTypes.left().value().values().forEach(id -> id.setOperations(
                id.getOperations().keySet().stream().collect(Collectors.toMap(key -> key.replaceFirst(
                        id.getUniqueId() + ".", ""), i -> id.getOperations().get(i)))));

        return Either.left(interfaceLifecycleTypes.left().value());
    }

    private Either<InterfaceDefinition, ResponseFormat> getOrCreateInterfaceDefinition(
            org.openecomp.sdc.be.model.Component component, InterfaceDefinition interfaceDefinition,
            InterfaceDefinition storedInterfaceDef) {
        if (storedInterfaceDef != null) {
            return Either.left(storedInterfaceDef);
        } else {
            interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
            interfaceDefinition.setToscaResourceName(interfaceDefinition.getType());
            Either<List<InterfaceDefinition>, StorageOperationStatus> interfaceCreateEither =
                    interfaceOperation.addInterfaces(component.getUniqueId(),
                            Collections.singletonList(interfaceDefinition));
            if (interfaceCreateEither.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(
                        interfaceCreateEither.right().value(), component.getComponentType())));
            }
            return Either.left(interfaceCreateEither.left().value().get(0));
        }
    }

    private void updateOperationInputDefs(org.openecomp.sdc.be.model.Component component,
            Collection<Operation> interfaceOperations) {
        interfaceOperations.stream().filter(operation -> Objects.nonNull(operation.getInputs())).forEach(
                operation -> operation.getInputs().getListToscaDataDefinition().forEach(
                        inp -> component.getInputs().stream().filter(in -> inp.getInputId().equals(in.getUniqueId()))
                                       .forEach(in -> {
                                           inp.setDefaultValue(in.getDefaultValue());
                                           inp.setValue(in.getValue());
                                           inp.setSchema(in.getSchema());
                                       })));
    }

    private void addOperationToInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation) {
        interfaceOperation.setUniqueId(UUID.randomUUID().toString());
        interfaceOperation.setImplementation(createArtifactDefinition(UUID.randomUUID().toString()));
        interfaceDefinition.getOperations()
                .put(interfaceOperation.getUniqueId(), new OperationDataDefinition(interfaceOperation));
    }

    private void updateOperationOnInterface(InterfaceDefinition interfaceDefinition, Operation interfaceOperation,
            String artifactUuId) {
        interfaceOperation.setImplementation(createArtifactDefinition(artifactUuId));
        interfaceDefinition.getOperations()
                .put(interfaceOperation.getUniqueId(), new OperationDataDefinition(interfaceOperation));
    }

    private ArtifactDefinition createArtifactDefinition(String artifactUuId) {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactUUID(artifactUuId);
        artifactDefinition.setUniqueId(artifactUuId);
        artifactDefinition.setArtifactType(ArtifactTypeEnum.WORKFLOW.getType());
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        return artifactDefinition;
    }

    public Either<List<InterfaceDefinition>, ResponseFormat> updateInterfaceOperation(String componentId,
            List<InterfaceDefinition> interfaceDefinitions, User user, boolean lock) {
        return createOrUpdateInterfaceOperation(componentId, interfaceDefinitions, user, true,
                UPDATE_INTERFACE_OPERATION, lock);
    }

}
