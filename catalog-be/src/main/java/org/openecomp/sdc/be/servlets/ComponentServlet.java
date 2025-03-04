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
 */

package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.mixin.GroupCompositionMixin;
import org.openecomp.sdc.be.mixin.PolicyCompositionMixin;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Component Servlet", description = "Component Servlet")
@Controller
public class ComponentServlet extends BeGenericServlet {
    private static final String GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION = "getCertifiedNotAbstractComponents failed with exception";

	private static final String GET_CERTIFIED_NON_ABSTRACT = "Get Certified Non Abstract";

	private static final String FAILED_TO_GET_ALL_NON_ABSTRACT = "failed to get all non abstract {}";

	private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";

	private static final Logger log = Logger.getLogger(ComponentServlet.class);

    private final ComponentBusinessLogicProvider componentBusinessLogicProvider;

    @Inject
    public ComponentServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        ComponentBusinessLogicProvider componentBusinessLogicProvider) {
        super(userBusinessLogic, componentsUtils);
        this.componentBusinessLogicProvider = componentBusinessLogicProvider;
    }

    @GET
    @Path("/{componentType}/{componentUuid}/conformanceLevelValidation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate Component Conformance Level", httpMethod = "GET", notes = "Returns the result according to conformance level in BE config", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response conformanceLevelValidation(@PathParam("componentType") final String componentType, @PathParam("componentUuid") final String componentUuid, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        Response response;

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum != null) {
            ComponentBusinessLogic compBL = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            Either<Boolean, ResponseFormat> eitherConformanceLevel = compBL.validateConformanceLevel(componentUuid, componentTypeEnum, userId);
            if (eitherConformanceLevel.isRight()) {
                response = buildErrorResponse(eitherConformanceLevel.right().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), gson.toJson(eitherConformanceLevel.left().value()));
            }
        } else {
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        return response;
    }

    @GET
    @Path("/{componentType}/{componentId}/requirmentsCapabilities")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirements And Capabilities according to componentId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getRequirementAndCapabilities(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        Response response;

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum != null) {
            try {
                ComponentBusinessLogic compBL = componentBusinessLogicProvider.getInstance(componentTypeEnum);
                Either<CapReqDef, ResponseFormat> eitherRequirementsAndCapabilities = compBL.getRequirementsAndCapabilities(componentId, componentTypeEnum, userId);
                if (eitherRequirementsAndCapabilities.isRight()) {
                    response = buildErrorResponse(eitherRequirementsAndCapabilities.right().value());
                } else {
                    response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(eitherRequirementsAndCapabilities.left().value()));
                }
            } catch (Exception e) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Capabilities and requirements for " + componentId);
                log.debug("getRequirementAndCapabilities failed with exception", e);
                response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        } else {
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        return response;
    }

    @GET
    @Path("/{componentType}/latestversion/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirments And Capabilities according to componentId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getLatestVersionNotAbstractCheckoutComponents(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
            @QueryParam("componentUids") List<String> componentUids, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response = null;

        try {

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);

            log.debug("Received componentUids size is {}", componentUids == null ? 0 : componentUids.size());

            Either<List<Component>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponents(false, componentTypeEnum, internalComponentType, componentUids, userId);

            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }

    }

    @POST
    @Path("/{componentType}/latestversion/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirments And Capabilities according to componentId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getLatestVersionNotAbstractCheckoutComponentsByBody(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "Consumer Object to be created", required = true) List<String> data) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        Response response = null;

        try {

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            if (log.isDebugEnabled()) {
                log.debug("Received componentUids size is {}", data == null ? 0 : data.size());
            }

            Either<List<Component>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponents(false, componentTypeEnum, internalComponentType, data, userId);

            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());

            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return  buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);



        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }

    }

    @GET
    @Path("/{componentType}/latestversion/notabstract/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Component uid only", httpMethod = "GET", notes = "Returns componentId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getLatestVersionNotAbstractCheckoutComponentsIdesOnly(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "uid list", required = true) String data) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response = null;
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);

            Either<List<Component>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponentsMetadata(false, HighestFilterEnum.HIGHEST_ONLY, componentTypeEnum, internalComponentType, userId);
            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }

    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Component instances", httpMethod = "GET", notes = "Returns component instances", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getComponentInstancesFilteredByPropertiesAndInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
            @QueryParam("searchText") String searchText, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "uid" + " " + "list", required = true) String data) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        Response response = null;
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);

            Either<List<ComponentInstance>, ResponseFormat> actionResponse = businessLogic.getComponentInstancesFilteredByPropertiesAndInputs(componentId, userId);
            if (actionResponse.isRight()) {
                log.debug("failed to get all component instances filtered by properties and inputs", componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Instances filtered by properties & inputs" + componentType);
            log.debug("getComponentInstancesFilteredByPropertiesAndInputs failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }



    /**
     * This API is a generic api for ui - the api get a list of strings and return the data on the component according to to list.
     * for example: list of the string "properties, inputs" will return component with the list of properties and inputs.
     *
     * @param componentType
     * @param componentId
     * @param dataParamsToReturn
     * @param request
     * @param userId
     * @return
     */

    @GET
    @Path("/{componentType}/{componentId}/filteredDataByParams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Resource", httpMethod = "GET", notes = "Returns resource according to resourceId", response = Resource.class)
    @ResponseView(mixin = {GroupCompositionMixin.class, PolicyCompositionMixin.class})
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Resource not found") })
    public Response getComponentDataFilteredByParams(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @QueryParam("include") final List<String> dataParamsToReturn, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);

        Response response;

        try {
            String resourceIdLower = componentId.toLowerCase();
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);

            log.trace("get component with id {} filtered by ui params", componentId);
            Either<UiComponentDataTransfer, ResponseFormat> actionResponse = businessLogic.getComponentDataFilteredByParams(resourceIdLower, modifier, dataParamsToReturn);

            if (actionResponse.isRight()) {
                log.debug("failed to get component data filtered by ui params");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get component filtered by ui params");
            log.debug("get resource failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }


    @GET
    @Path("/{componentType}/{componentId}/filteredproperties/{propertyNameFragment}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve properties belonging to component instances of specific component by name and optionally resource type", httpMethod = "GET", notes = "Returns properties belonging to component instances of specific component by name and optionally resource type", response = Map.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getFilteredComponentInstanceProperties(
            @PathParam("componentType") final String componentType,
            @PathParam("componentId") final String componentId,
            @PathParam("propertyNameFragment") final String propertyNameFragment,
            @QueryParam("resourceType") List<String> resourceTypes,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        User user = new User();
        user.setUserId(userId);
        log.debug("User Id is {}" , userId);
        Response response;
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            Map<FilterKeyEnum, List<String>> filters = new EnumMap<>(FilterKeyEnum.class);
            List<String> propertyNameFragments = new ArrayList<>();
            propertyNameFragments.add(propertyNameFragment);
            filters.put(FilterKeyEnum.NAME_FRAGMENT, propertyNameFragments);
            if(CollectionUtils.isNotEmpty(resourceTypes)){
                filters.put(FilterKeyEnum.RESOURCE_TYPE, resourceTypes);
            }
            Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> actionResponse = businessLogic.getFilteredComponentInstanceProperties(componentId, filters, userId);
            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object resource = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Filtered Component Instance Properties");
            log.debug("Getting of filtered component instance properties failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }
}
