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
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/user")
@Api(value = "User Administration", description = "User admininstarator operations")
@Singleton
public class UserAdminServlet extends BeGenericServlet {

    private static final String UTF_8 = "UTF-8";
	private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
	private static final String ROLE_DELIMITER = ",";
    private static final Logger log = Logger.getLogger(UserAdminServlet.class);
    private final UserBusinessLogic userBusinessLogic;

    @Inject
    public UserAdminServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils) {
        super(userBusinessLogic, componentsUtils);
        this.userBusinessLogic = userBusinessLogic;
    }

    /***************************************
     * API start
     *************************************************************/

    /* User by userId CRUD start */

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // retrieve all user details
    @GET
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "retrieve user details", httpMethod = "GET", notes = "Returns user details according to userId", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns user Ok"), @ApiResponse(code = 404, message = "User not found"), @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response get(@ApiParam(value = "userId of user to get", required = true) @PathParam("userId") final String userId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);

        try {
            Either<User, ActionStatus> either = userBusinessLogic.getUser(userId, false);

            if (either.isRight()) {
                return buildErrorResponse(getComponentsUtils().getResponseFormatByUserId(either.right().value(), userId));
            } else {
                if (either.left().value() != null) {
                    return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
                } else {
                    return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
                }
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get User");
            log.debug("get user failed with unexpected error: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/{userId}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "retrieve user role", notes = "Returns user role according to userId", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns user role Ok"), @ApiResponse(code = 404, message = "User not found"), @ApiResponse(code = 405, message = "Method Not Allowed"),
            @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getRole(@ApiParam(value = "userId of user to get", required = true) @PathParam("userId") final String userId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getRole) Start handle request of {}", url);

        try {
            Either<User, ActionStatus> either = userBusinessLogic.getUser(userId, false);
            if (either.isRight()) {
                return buildErrorResponse(getComponentsUtils().getResponseFormatByUserId(either.right().value(), userId));
            } else {
                if (either.left().value() != null) {
                    String roleJson = "{ \"role\" : \"" + either.left().value().getRole() + "\" }";
                    return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), roleJson);
                } else {
                    return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
                }
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get User Role");
            log.debug("Get user role failed with unexpected error: {}", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // update user role
    @POST
    @Path("/{userId}/role")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update user role", notes = "Update user role", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Update user OK"), @ApiResponse(code = 400, message = "Invalid Content."), @ApiResponse(code = 403, message = "Missing information/Restricted operation"),
            @ApiResponse(code = 404, message = "User not found"), @ApiResponse(code = 405, message = "Method Not Allowed"), @ApiResponse(code = 409, message = "User already exists"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response updateUserRole(@ApiParam(value = "userId of user to get", required = true) @PathParam("userId") final String userIdUpdateUser, @Context final HttpServletRequest request,
            @ApiParam(value = "json describe the update role", required = true) String data, @HeaderParam(value = Constants.USER_ID_HEADER) String modifierUserId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(modifierUserId);
        log.debug("modifier id is {}", modifierUserId);

        Response response = null;

        try {
            User updateInfoUser = getComponentsUtils().convertJsonToObject(data, modifier, User.class, AuditingActionEnum.UPDATE_USER).left().value();
            Either<User, ResponseFormat> updateUserResponse = userBusinessLogic.updateUserRole(modifier, userIdUpdateUser, updateInfoUser.getRole());

            if (updateUserResponse.isRight()) {
                log.debug("failed to update user role");
                response = buildErrorResponse(updateUserResponse.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updateUserResponse.left().value());
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update User Metadata");
            log.debug("Update User Role failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    /* User role CRUD end */

    /* New user CRUD start */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "add user", httpMethod = "POST", notes = "Provision new user", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "New user created"), @ApiResponse(code = 400, message = "Invalid Content."), @ApiResponse(code = 403, message = "Missing information"),
            @ApiResponse(code = 405, message = "Method Not Allowed"), @ApiResponse(code = 409, message = "User already exists"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response createUser(@Context final HttpServletRequest request, @ApiParam(value = "json describe the user", required = true) String newUserData, @HeaderParam(value = Constants.USER_ID_HEADER) String modifierAttId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(modifierAttId);
        log.debug("modifier id is {}", modifierAttId);

        Response response = null;

        try {
            User newUserInfo = getComponentsUtils().convertJsonToObject(newUserData, modifier, User.class, AuditingActionEnum.ADD_USER).left().value();
            Either<User, ResponseFormat> createUserResponse = userBusinessLogic.createUser(modifier, newUserInfo);

            if (createUserResponse.isRight()) {
                log.debug("failed to create user");
                response = buildErrorResponse(createUserResponse.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), createUserResponse.left().value());
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update User Metadata");
            log.debug("Create User failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    /* New user CRUD end */

    /* User authorization start */

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // User Authorization
    @GET
    @Path("/authorize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    @ApiOperation(value = "authorize", notes = "authorize user", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns user Ok"), @ApiResponse(code = 403, message = "Restricted Access"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response authorize(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @HeaderParam("HTTP_CSP_FIRSTNAME") String firstName, @HeaderParam("HTTP_CSP_LASTNAME") String lastName,
            @HeaderParam("HTTP_CSP_EMAIL") String email) {

        try {
            userId = userId != null ? URLDecoder.decode(userId, UTF_8) : null;
            firstName = firstName != null ? URLDecoder.decode(firstName, UTF_8) : null;
            lastName = lastName != null ? URLDecoder.decode(lastName, UTF_8) : null;
            email = email != null ? URLDecoder.decode(email, UTF_8) : null;
        } catch (UnsupportedEncodingException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Authorize User - decode headers");
            ResponseFormat errorResponseWrapper = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            log.error("#authorize - authorization decoding failed with error: ", e);
            return buildErrorResponse(errorResponseWrapper);
        }

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        User authUser = new User();
        authUser.setUserId(userId);
        authUser.setFirstName(firstName);
        authUser.setLastName(lastName);
        authUser.setEmail(email);
        log.debug("auth user id is {}", userId);

        Response response = null;
        try {
            Either<User, ResponseFormat> authorize = userBusinessLogic.authorize(authUser);

            if (authorize.isRight()) {
                log.debug("authorize user failed");
                response = buildErrorResponse(authorize.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), authorize.left().value());
            return response;

        } catch (Exception e) {
            log.debug("authorize user failed with unexpected error: {}", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    /* User authorization end */

    @GET
    @Path("/admins")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "retrieve all administrators", httpMethod = "GET", notes = "Returns all administrators", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns user Ok"), @ApiResponse(code = 405, message = "Method Not Allowed"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getAdminsUser(@Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);

        try {
            Either<List<User>, ResponseFormat> either = userBusinessLogic.getAllAdminUsers();

            if (either.isRight()) {
                log.debug("Failed to get all admin users");
                return buildErrorResponse(either.right().value());
            } else {
                if (either.left().value() != null) {
                    return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
                } else {
                    return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
                }
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Administrators");
            log.debug("get all admins failed with unexpected error: {}", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve the list of all active ASDC users or only group of users having specific roles.", httpMethod = "GET", notes = "Returns list of users with the specified roles, or all of users in the case of empty 'roles' header", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns users Ok"), @ApiResponse(code = 204, message = "No provisioned ASDC users of requested role"), @ApiResponse(code = 403, message = "Restricted Access"),
            @ApiResponse(code = 400, message = "Missing content"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response getUsersList(@Context final HttpServletRequest request, @ApiParam(value = "Any active user's USER_ID ") @HeaderParam(Constants.USER_ID_HEADER) final String userId,
            @ApiParam(value = "TESTER,DESIGNER,PRODUCT_STRATEGIST,OPS,PRODUCT_MANAGER,GOVERNOR, ADMIN OR all users by not typing anything") @QueryParam("roles") final String roles) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {}", url, userId);

        List<String> rolesList = new ArrayList<>();
        if (roles != null && !roles.trim().isEmpty()) {
            String[] rolesArr = roles.split(ROLE_DELIMITER);
            for (String role : rolesArr) {
                rolesList.add(role.trim());
            }
        }

        try {
            Either<List<User>, ResponseFormat> either = userBusinessLogic.getUsersList(userId, rolesList, roles);

            if (either.isRight()) {
                log.debug("Failed to get ASDC users");
                return buildErrorResponse(either.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), either.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get ASDC users");
            log.debug("get users failed with unexpected error: {}", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // delete user
    @DELETE
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete user", notes = "Delete user", response = User.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Update deleted OK"), @ApiResponse(code = 400, message = "Invalid Content."), @ApiResponse(code = 403, message = "Missing information"),
            @ApiResponse(code = 404, message = "User not found"), @ApiResponse(code = 405, message = "Method Not Allowed"), @ApiResponse(code = 409, message = "Restricted operation"), @ApiResponse(code = 500, message = "Internal Server Error") })
    public Response deActivateUser(@ApiParam(value = "userId of user to get", required = true) @PathParam("userId") final String userId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userIdHeader) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {}", url, userIdHeader);

        User modifier = new User();
        modifier.setUserId(userIdHeader);

        Response response = null;
        try {
            Either<User, ResponseFormat> deactiveUserResponse = userBusinessLogic.deActivateUser(modifier, userId);

            if (deactiveUserResponse.isRight()) {
                log.debug("Failed to deactivate user");
                response = buildErrorResponse(deactiveUserResponse.right().value());
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deactiveUserResponse.left().value());
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get ASDC users");
            log.debug("deactivate user failed with unexpected error: {}", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}
