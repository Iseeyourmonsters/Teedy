package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.criteria.*;
import com.sismics.docs.core.dao.dto.*;
import com.sismics.docs.core.model.jpa.*;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * User registration resource.
 *
 * @author sicheng
 */
@Path("/userRegistration")
public class UserRegistrationResource extends BaseResource{
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationResource.class);

    /**
     * Create a new registration request.
     *
     * @api {put} /userRegistration Register a new user
     * @apiName PutRegister
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (server) PrivateKeyError Error while generating a private key
     * @apiError (client) AlreadyExistingUsername Login already used
     * @apiError (server) UnknownError Unknown server error
     * @apiVersion 1.5.0
     *
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @PUT
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {

        log.debug("inside register, username: " + username + ", password: " + password + ", email: " + email);

        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        ValidationUtil.validateEmail(email, "email");

        // Create the user registration
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setUsername(username);
        userRegistration.setEmail(email);
        userRegistration.setPassword(password);
        userRegistration.setStatus(Constants.DEFAULT_REGISTRATION_STATUS);
        userRegistration.setRegistrationDate(new Date());
        userRegistration.setAdminComment(null);

        // Create the userRegistrationDao
        UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
        try {
            userRegistrationDao.create(userRegistration);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all registrations.
     *
     * @api {get} /userRegistration/list Get registration list
     * @apiName GetUserList
     * @apiGroup User
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiSuccess {Object[]} registrations List of registrations
     * @apiSuccess {String} requests.id ID
     * @apiSuccess {String} requests.username Username
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {Number} requests.registration_date Registration date (timestamp)
     * @apiSuccess {String} registration_date.admin_comment Admin comment
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    @Path("/list")
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JsonArrayBuilder requests = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
        List<UserRegistrationDto> userRegistrationDtoList = userRegistrationDao.findByCriteria(new UserRegistrationCriteria(), sortCriteria);
        for (UserRegistrationDto userRegistrationDto : userRegistrationDtoList) {
            log.debug("register username in dto: " + userRegistrationDto.getUsername());
            requests.add(Json.createObjectBuilder()
                    .add("id", userRegistrationDto.getId())
                    .add("username", userRegistrationDto.getUsername())
                    .add("password", userRegistrationDto.getPassword())
                    .add("email", userRegistrationDto.getEmail())
                    .add("registration_date", userRegistrationDto.getRegistrationDate())
                    .add("status", userRegistrationDto.getStatus())
                    .add("admin_comment", userRegistrationDto.getAdminComment()));
        }

        log.debug("fetch list good");

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Approve a registration.
     *
     * @api {put} /approve Approve registration
     * @apiName approveRegistration
     * @apiParam {String} id ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (server) UnknownError Unknown server error
     * @apiVersion 1.5.0
     *
     * @param id ID of the registration to update
     * @return Response
     */
    @PUT
    @Path("/approveRegistration")
    public Response approveRegistration(
            @FormParam("id") String id) {
        log.debug("inside approveRegistration, id: " + id);
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Update the user registration
        UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
        UserRegistration userRegistration = userRegistrationDao.getById(id);
        if (userRegistration == null) {
            throw new ClientException("UserRegistrationNotFound", "User registration not found");
        }

        try {
            userRegistration.setStatus(Constants.APPROVED_REGISTRATION_STATUS);
            userRegistrationDao.update(userRegistration, id);
            log.info("User registration approved: " + userRegistration.getUsername());
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Unknown server error", e);
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}