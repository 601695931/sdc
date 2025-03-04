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
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTypesInfo;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/v1/")

/****
 * 
 * UI oriented servlet - to return elements in specific format UI needs
 * 
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Api(value = "Element Servlet", description = "Element Servlet")
@Singleton
public class ElementServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(ElementServlet.class);
    private final ComponentsCleanBusinessLogic componentsCleanBusinessLogic;
    private final ElementBusinessLogic elementBusinessLogic;
    private final UserBusinessLogic userBusinessLogic;

    @Inject
    public ElementServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        ComponentsCleanBusinessLogic componentsCleanBusinessLogic,
        ElementBusinessLogic elementBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.componentsCleanBusinessLogic = componentsCleanBusinessLogic;
        this.elementBusinessLogic = elementBusinessLogic;
        this.userBusinessLogic = userBusinessLogic;
    }

    /*
     ******************************************************************************
     * NEW CATEGORIES category / \ subcategory subcategory / grouping
     ******************************************************************************/

    /*
     *
     *
     * CATEGORIES
     */
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all component categories
    @GET
    @Path("/categories/{componentType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve the list of all resource/service/product categories/sub-categories/groupings", httpMethod = "GET", notes = "Retrieve the list of all resource/service/product categories/sub-categories/groupings.", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns categories Ok"), @ApiResponse(code = 403, message = "Missing information"), @ApiResponse(code = 400, message = "Invalid component type"),
            @ApiResponse(code = 409, message = "Restricted operation"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getComponentCategories(@ApiParam(value = "allowed values are resources / services/ products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
            + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam(value = "componentType") final String componentType, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {

        try {
            Either<List<CategoryDefinition>, ResponseFormat> either = elementBusinessLogic.getAllCategories(componentType, userId);
            if (either.isRight()) {
                log.debug("No categories were found for type {}", componentType);
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Categories");
            log.debug("getComponentCategories failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve the all resource, service and product categories", httpMethod = "GET", notes = "Retrieve the all resource, service and product categories", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns categories Ok"), @ApiResponse(code = 403, message = "Missing information"),
            @ApiResponse(code = 409, message = "Restricted operation"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getAllCategories(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<UiCategories, ResponseFormat> either = elementBusinessLogic
                .getAllCategories(userId);
            if (either.isRight()) {
                log.debug("No categories were found");
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Categories");
            log.debug("getAllCategories failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }


    @POST
    @Path("/category/{componentType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create new component category", httpMethod = "POST", notes = "Create new component category")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Category created"), @ApiResponse(code = 400, message = "Invalid category data"), @ApiResponse(code = 403, message = "USER_ID header is missing"),
            @ApiResponse(code = 409, message = "Category already exists / User not permitted to perform the action"), @ApiResponse(code = 500, message = "General Error") })
    public Response createComponentCategory(
            @ApiParam(value = "allowed values are resources /services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam(value = "componentType") final String componentType,
            @ApiParam(value = "Category to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            CategoryDefinition category = RepresentationUtils.fromRepresentation(data, CategoryDefinition.class);

            Either<CategoryDefinition, ResponseFormat> createResourceCategory =
                elementBusinessLogic.createCategory(category, componentType, userId);
            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }

            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createResourceCategory.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete component category", httpMethod = "DELETE", notes = "Delete component category", response = Category.class)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Category deleted"), @ApiResponse(code = 403, message = "USER_ID header is missing"), @ApiResponse(code = 409, message = "User not permitted to perform the action"),
            @ApiResponse(code = 404, message = "Category not found"), @ApiResponse(code = 500, message = "General Error") })
    public Response deleteComponentCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId, @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<CategoryDefinition, ResponseFormat> createResourceCategory =
                elementBusinessLogic.deleteCategory(categoryUniqueId, componentType, userId);

            if (createResourceCategory.isRight()) {
                return buildErrorResponse(createResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create resource category");
            log.debug("createResourceCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    /*
     *
     *
     * SUBCATEGORIES
     *
     */

    @POST
    @Path("/category/{componentType}/{categoryId}/subCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create new component sub-category", httpMethod = "POST", notes = "Create new component sub-category for existing category")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Subcategory created"), @ApiResponse(code = 400, message = "Invalid subcategory data"), @ApiResponse(code = 403, message = "USER_ID header is missing"),
            @ApiResponse(code = 404, message = "Parent category wasn't found"), @ApiResponse(code = 409, message = "Subcategory already exists / User not permitted to perform the action"), @ApiResponse(code = 500, message = "General Error") })
    public Response createComponentSubCategory(
            @ApiParam(value = "allowed values are resources / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam(value = "componentType") final String componentType,
            @ApiParam(value = "Parent category unique ID", required = true) @PathParam(value = "categoryId") final String categoryId, @ApiParam(value = "Subcategory to be created. \ne.g. {\"name\":\"Resource-subcat\"}", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            SubCategoryDefinition subCategory = RepresentationUtils.fromRepresentation(data, SubCategoryDefinition.class);

            Either<SubCategoryDefinition, ResponseFormat> createSubcategory = elementBusinessLogic
                .createSubCategory(subCategory, componentType, categoryId, userId);
            if (createSubcategory.isRight()) {
                return buildErrorResponse(createSubcategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createSubcategory.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create sub-category");
            log.debug("createComponentSubCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete component category", httpMethod = "DELETE", notes = "Delete component category", response = Category.class)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Category deleted"), @ApiResponse(code = 403, message = "USER_ID header is missing"), @ApiResponse(code = 409, message = "User not permitted to perform the action"),
            @ApiResponse(code = 404, message = "Category not found"), @ApiResponse(code = 500, message = "General Error") })
    public Response deleteComponentSubCategory(@PathParam(value = "categoryUniqueId") final String categoryUniqueId, @PathParam(value = "subCategoryUniqueId") final String subCategoryUniqueId,
            @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<SubCategoryDefinition, ResponseFormat> deleteSubResourceCategory =
                elementBusinessLogic.deleteSubCategory(subCategoryUniqueId, componentType, userId);
            if (deleteSubResourceCategory.isRight()) {
                return buildErrorResponse(deleteSubResourceCategory.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component category");
            log.debug("deleteComponentSubCategory failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    /*
     * GROUPINGS
     */
    @POST
    @Path("/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create new component grouping", httpMethod = "POST", notes = "Create new component grouping for existing sub-category")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Grouping created"), @ApiResponse(code = 400, message = "Invalid grouping data"), @ApiResponse(code = 403, message = "USER_ID header is missing"),
            @ApiResponse(code = 404, message = "Parent category or subcategory were not found"), @ApiResponse(code = 409, message = "Grouping already exists / User not permitted to perform the action"),
            @ApiResponse(code = 500, message = "General Error") })
    public Response createComponentGrouping(@ApiParam(value = "allowed values are products", allowableValues = ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam(value = "componentType") final String componentType,
            @ApiParam(value = "Parent category unique ID", required = true) @PathParam(value = "categoryId") final String grandParentCategoryId,
            @ApiParam(value = "Parent sub-category unique ID", required = true) @PathParam(value = "subCategoryId") final String parentSubCategoryId, @ApiParam(value = "Subcategory to be created", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        try {
            GroupingDefinition grouping = RepresentationUtils.fromRepresentation(data, GroupingDefinition.class);

            Either<GroupingDefinition, ResponseFormat> createGrouping = elementBusinessLogic
                .createGrouping(grouping, componentType, grandParentCategoryId, parentSubCategoryId, userId);
            if (createGrouping.isRight()) {
                return buildErrorResponse(createGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createGrouping.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create grouping");
            log.debug("createComponentGrouping failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete component category", httpMethod = "DELETE", notes = "Delete component category", response = Category.class)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Category deleted"), @ApiResponse(code = 403, message = "USER_ID header is missing"), @ApiResponse(code = 409, message = "User not permitted to perform the action"),
            @ApiResponse(code = 404, message = "Category not found"), @ApiResponse(code = 500, message = "General Error") })
    public Response deleteComponentGrouping(@PathParam(value = "categoryUniqueId") final String grandParentCategoryUniqueId, @PathParam(value = "subCategoryUniqueId") final String parentSubCategoryUniqueId,
            @PathParam(value = "groupingUniqueId") final String groupingUniqueId, @PathParam(value = "componentType") final String componentType, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        try {
            Either<GroupingDefinition, ResponseFormat> deleteGrouping = elementBusinessLogic
                .deleteGrouping(groupingUniqueId, componentType, userId);
            if (deleteGrouping.isRight()) {
                return buildErrorResponse(deleteGrouping.right().value());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
            return buildOkResponse(responseFormat, null);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete component grouping");
            log.debug("deleteGrouping failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all tags
    @GET
    @Path("/tags")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all tags", httpMethod = "GET", notes = "Retrieve all tags", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns tags Ok"), @ApiResponse(code = 404, message = "No tags were found"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getTags(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getTags) Start handle request of {}", url);

        try {
            Either<List<Tag>, ActionStatus> either = elementBusinessLogic.getAllTags(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No tags were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Tags");
            log.debug("getAllTags failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all property scopes
    @GET
    @Path("/propertyScopes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all propertyScopes", httpMethod = "GET", notes = "Retrieve all propertyScopes", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns propertyScopes Ok"), @ApiResponse(code = 404, message = "No propertyScopes were found"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getPropertyScopes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getPropertyScopes) Start handle request of {}", url);

        try {
            Either<List<PropertyScope>, ActionStatus> either = elementBusinessLogic.getAllPropertyScopes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No property scopes were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property Scopes Categories");
            log.debug("getPropertyScopes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all artifact types
    @GET
    @Path("/artifactTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all artifactTypes", httpMethod = "GET", notes = "Retrieve all artifactTypes", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns artifactTypes Ok"), @ApiResponse(code = 404, message = "No artifactTypes were found"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getArtifactTypes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET - getArtifactTypes) Start handle request of {}", url);

        try {
            Either<List<ArtifactType>, ActionStatus> either = elementBusinessLogic.getAllArtifactTypes(userId);
            if (either.isRight() || either.left().value() == null) {
                log.debug("No artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {

                Integer defaultHeatTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultHeatArtifactTimeoutMinutes();
                ArtifactTypesInfo typesResponse = new ArtifactTypesInfo();
                typesResponse.setArtifactTypes(either.left().value());
                typesResponse.setHeatDefaultTimeout(defaultHeatTimeout);

                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), typesResponse);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Artifact Types");
            log.debug("getArtifactTypes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all artifact types
    @GET
    @Path("/configuration/ui")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all artifactTypes", httpMethod = "GET", notes = "Retrieve all artifactTypes", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns artifactTypes Ok"), @ApiResponse(code = 404, message = "No artifactTypes were found"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getConfiguration(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getConfiguration) Start handle request of {}", url);

        try {
            Either<List<ArtifactType>, ActionStatus> otherEither = elementBusinessLogic.getAllArtifactTypes(userId);
            Either<Integer, ActionStatus> defaultHeatTimeout = elementBusinessLogic.getDefaultHeatTimeout();
            Either<Map<String, Object>, ActionStatus> deploymentEither = elementBusinessLogic.getAllDeploymentArtifactTypes();
            Either<Map<String, String>, ActionStatus> resourceTypesMap = elementBusinessLogic.getResourceTypesMap();

            if (otherEither.isRight() || otherEither.left().value() == null) {
                log.debug("No other artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (deploymentEither.isRight() || deploymentEither.left().value() == null) {
                log.debug("No deployment artifact types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (defaultHeatTimeout.isRight() || defaultHeatTimeout.left().value() == null) {
                log.debug("heat default timeout was not found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else if (resourceTypesMap.isRight() || resourceTypesMap.left().value() == null) {
                log.debug("No resource types were found");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
            } else {
                Map<String, Object> artifacts = new HashMap<>();
                Map<String, Object> configuration = new HashMap<>();

                artifacts.put("other", otherEither.left().value());
                artifacts.put("deployment", deploymentEither.left().value());
                configuration.put("artifacts", artifacts);
                configuration.put("defaultHeatTimeout", defaultHeatTimeout.left().value());
                configuration.put("componentTypes", elementBusinessLogic.getAllComponentTypesParamNames());
                configuration.put("roles", elementBusinessLogic.getAllSupportedRoles());
                configuration.put("resourceTypes", resourceTypesMap.left().value());
                configuration.put("environmentContext", ConfigurationManager.getConfigurationManager().getConfiguration().getEnvironmentContext());
                configuration.put("gab", ConfigurationManager.getConfigurationManager().getConfiguration().getGabConfig());

                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), configuration);
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Artifact Types");
            log.debug("getArtifactTypes failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all followed resources and services
    @GET
    @Path("/followed")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all followed", httpMethod = "GET", notes = "Retrieve all followed", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns followed Ok"), @ApiResponse(code = 404, message = "No followed were found"), @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getFollowedResourcesServices(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Response res = null;
        User userData = null;
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);

            // Getting the user
            Either<User, ActionStatus> either = userBusinessLogic.getUser(userId, false);
            if (either.isRight()) {
                // Couldn't find or otherwise fetch the user
                return buildErrorResponse(getComponentsUtils().getResponseFormatByUserId(either.right().value(), userId));
            }

            if (either.left().value() != null) {
                userData = either.left().value();
                Either<Map<String, List<? extends Component>>, ResponseFormat> followedResourcesServices =
                    elementBusinessLogic.getFollowed(userData);
                if (followedResourcesServices.isRight()) {
                    log.debug("failed to get followed resources services ");
                    return buildErrorResponse(followedResourcesServices.right().value());
                }
                Object data = RepresentationUtils.toRepresentation(followedResourcesServices.left().value());
                res = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);
            } else {
                res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Followed Resources / Services Categories");
            log.debug("Getting followed resources/services failed with exception", e);
            res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return res;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all certified resources and services and their last version
    @GET
    @Path("/screen")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve catalog resources and services", httpMethod = "GET", notes = "Retrieve catalog resources and services", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns resources and services Ok"), @ApiResponse(code = 404, message = "No resources and services were found"), @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getCatalogComponents(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @QueryParam("excludeTypes") List<OriginTypeEnum> excludeTypes) {

        Response res = null;
        try {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {}", url);

			Either<Map<String, List<CatalogComponent>>, ResponseFormat> catalogData = elementBusinessLogic
          .getCatalogComponents(userId, excludeTypes);

            if (catalogData.isRight()) {
                log.debug("failed to get catalog data");
                return buildErrorResponse(catalogData.right().value());
            }
            Object data = RepresentationUtils.toRepresentation(catalogData.left().value());
            res = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), data);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Catalog Components");
            log.debug("Getting catalog components failed with exception", e);
            res = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return res;
    }

    @DELETE
    @Path("/inactiveComponents/{componentType}")
    public Response deleteMarkedResources(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;

        NodeTypeEnum nodeType = NodeTypeEnum.getByNameIgnoreCase(componentType);
        if (nodeType == null) {
            log.info("componentType is not valid: {}", componentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        List<NodeTypeEnum> componentsList = new ArrayList<>();
        componentsList.add(nodeType);
        try {
            Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponentsResult = componentsCleanBusinessLogic.cleanComponents(componentsList);
            Either<List<String>, ResponseFormat> cleanResult = cleanComponentsResult.get(nodeType);

            if (cleanResult.isRight()) {
                log.debug("failed to delete marked components of type {}", nodeType);
                response = buildErrorResponse(cleanResult.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), cleanResult.left().value());
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Marked Components");
            log.debug("delete marked components failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/ecompPortalMenu")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve ecomp portal menu - MOC", httpMethod = "GET", notes = "Retrieve ecomp portal menu", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Retrieve ecomp portal menu") })
    public Response getListOfCsars(@Context final HttpServletRequest request) {
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                "[{\"menuId\":1,\"column\":2,\"text\":\"Design\",\"parentMenuId\":null,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":11,\"column\":1,\"text\":\"ProductDesign\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":12,\"column\":2,\"text\":\"Service\",\"parentMenuId\":1,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":21,\"column\":1,\"text\":\"ViewPolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":90,\"column\":1,\"text\":\"4thLevelApp1aR16\",\"parentMenuId\":21,\"url\":\"http://google.com\",\"appid\":null,\"roles\":null}]},{\"menuId\":22,\"column\":2,\"text\":\"UpdatePolicies\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null,\"children\":[{\"menuId\":91,\"column\":1,\"text\":\"4thLevelApp1bR16\",\"parentMenuId\":22,\"url\":\"http://jsonlint.com/\",\"appid\":null,\"roles\":null}]},{\"menuId\":23,\"column\":3,\"text\":\"UpdateRules\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":24,\"column\":4,\"text\":\"CreateSignatures?\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null},{\"menuId\":25,\"column\":5,\"text\":\"Definedata\",\"parentMenuId\":12,\"url\":\"\",\"appid\":null,\"roles\":null}]}]}]");
    }

}
