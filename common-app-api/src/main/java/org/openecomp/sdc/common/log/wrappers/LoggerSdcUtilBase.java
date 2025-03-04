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

package org.openecomp.sdc.common.log.wrappers;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.List;
import java.util.StringTokenizer;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_ENTITY_TOO_LARGE;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_LENGTH_REQUIRED;
import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_PAYMENT_REQUIRED;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.net.HttpURLConnection.HTTP_REQ_TOO_LONG;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

/**
 * Created by dd4296 on 12/20/2017.
 * <p>
 * base class for metric and audit log logging
 * holding the specific logic for data extraction
 */
public class LoggerSdcUtilBase {

    private static final int SUCCESS_ERROR_CODE_LIMIT = 399;
    private static final int BUSINESS_PROCESS_ERROR_BOUNDRY = 499;
    protected static Logger log = LoggerFactory.getLogger(LoggerSdcUtilBase.class.getName());

    String getRequestIDfromHeaders(List<Object> requestHeader) {
        // this method gets list of type object.
        // toString method returns the RequestId with brackets.
        String requestHeaderString = requestHeader.toString();
        return requestHeaderString.replace("[", "").replace("]", "");
    }


    // this method translates http error code to ECOMP Logger Error code
    // this is a naive translation and is not a result of any documented format ECOMP specification
    protected EcompLoggerErrorCode convertHttpCodeToErrorCode(int httpResponseCode) {
        if (isSuccessError(httpResponseCode)) {
            return EcompLoggerErrorCode.SUCCESS;
        }

        if (isSchemaError(httpResponseCode)) {
            return EcompLoggerErrorCode.SCHEMA_ERROR;
        }
        if (isDataError(httpResponseCode)) {
            return EcompLoggerErrorCode.DATA_ERROR;
        }
        if (isPermissionsError(httpResponseCode)) {
            return EcompLoggerErrorCode.PERMISSION_ERROR;
        }
        if (isTimeoutOrAvailabilityError(httpResponseCode)) {
            return EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR;
        }
        if (isBusinessProcessError(httpResponseCode)) {
            return EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;
        }
        return EcompLoggerErrorCode.UNKNOWN_ERROR;
    }

    private boolean isTimeoutOrAvailabilityError(int httpResponseCode) {

        switch (httpResponseCode) {
            case HTTP_BAD_REQUEST:
            case HTTP_UNAUTHORIZED:
            case HTTP_NOT_FOUND:
            case HTTP_CLIENT_TIMEOUT:
            case HTTP_GONE:
                return true;
            default:
                return false;
        }

    }

    private boolean isPermissionsError(int httpResponseCode) {

        switch (httpResponseCode) {
            case HTTP_PAYMENT_REQUIRED:
            case HTTP_FORBIDDEN:
            case HTTP_BAD_METHOD:
            case HTTP_PROXY_AUTH:
                return true;

            default:
                return false;
        }
    }

    private boolean isDataError(int httpResponseCode) {

        switch (httpResponseCode) {
            case HTTP_NOT_ACCEPTABLE:
            case HTTP_LENGTH_REQUIRED:
            case HTTP_PRECON_FAILED:
            case HTTP_REQ_TOO_LONG:
            case HTTP_ENTITY_TOO_LARGE:
            case HTTP_UNSUPPORTED_TYPE:
                return true;

            default:
                return false;
        }
    }

    private boolean isSchemaError(int httpResponseCode) {
        return HTTP_CONFLICT == httpResponseCode;
    }

    private boolean isSuccessError(int httpResponseCode) {
        return httpResponseCode < SUCCESS_ERROR_CODE_LIMIT;
    }

    private boolean isBusinessProcessError(int httpResponseCode) {
        return httpResponseCode > BUSINESS_PROCESS_ERROR_BOUNDRY;
    }

    protected String getPartnerName(String userAgent, String userId, String url) {

        if (!StringUtils.isEmpty(userId)) {
            return userId;
        }

        String urlUser = getUserIdFromUrl(url);

        if (!StringUtils.isEmpty(urlUser)) {
            return urlUser;
        }

        String userAgentName = getUserIdFromUserAgent(userAgent);

        if (!StringUtils.isEmpty(userAgentName)) {
            return userAgentName;
        }

        return "";
    }

    private String getUserIdFromUserAgent(String userAgent) {
        if (userAgent != null && userAgent.length() > 0) {
            if (userAgent.toLowerCase().contains("firefox")) {
                return "fireFox_FE";
            }

            if (userAgent.toLowerCase().contains("msie")) {
                return "explorer_FE";
            }

            if (userAgent.toLowerCase().contains("chrome")) {
                return "chrome_FE";
            }

            return userAgent;
        }
        return null;
    }

    private String getUserIdFromUrl(String url) {
        if (url != null && url.toLowerCase().contains("user")) {
            StringTokenizer st = new StringTokenizer(url, "/");
            while (st.hasMoreElements()) {
                if ("user".equalsIgnoreCase(st.nextToken())) {
                    return st.nextToken();
                }
            }
        }
        return null;
    }

    protected String getUrl(ContainerRequestContext requestContext) {
        String url = "";

        try {
            if (requestContext.getUriInfo() != null && requestContext.getUriInfo().getRequestUri() != null) {
                url = requestContext.getUriInfo().getRequestUri().toURL().toString();
            }
        } catch (Exception ex) {
            log.error("failed to get url from request context ", ex);
        }

        return url;
    }

    protected String getServiceName(ContainerRequestContext requestContext) {
        return (requestContext.getUriInfo().getRequestUri().toString())
                .replace(requestContext.getUriInfo().getBaseUri().toString(), "/");
    }
}
