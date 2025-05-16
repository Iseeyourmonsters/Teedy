package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserRegistrationDao;
import com.sismics.docs.core.dao.dto.UserRegistrationDto;
// Importing existing exception types
import com.sismics.docs.core.model.jpa.UserRegistration;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import jakarta.inject.Inject; // Using standard CDI Inject
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
 * Resource handling user registration operations.
 */
@Path("/userRegistration")
public class UserRegistrationResource extends BaseResource {
    /**
     * Logger instance for user registration resource.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationResource.class);

    /**
     * Injected Data Access Object for UserRegistration entities.
     * Managed by the application container (e.g., CDI, Spring).
     */
    @Inject
    private UserRegistrationDao userRegistrationDao;

    /**
     * Endpoint for submitting a new user registration request.
     *
     * @param usernameInput The desired username for the new user.
     * @param passwordInput The password chosen by the user.
     * @param emailInput    The user's email address.
     * @return A standard JAX-RS Response object.
     * @api {put} /userRegistration Register a new user
     * @apiName PutRegister
     * @apiParam {String} username Username
     * @apiParam {String} password Password
     * @apiParam {String} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (server) PrivateKeyError Error while generating a private key (Note: This specific error handling is not part of the current logic).
     * @apiError (client) AlreadyExistingUsername Login already used
     * @apiError (server) UnknownError Unknown server error
     * @apiVersion 1.5.0
     */
    @PUT
    public Response register
    (
            @FormParam("username") String usernameInput,
            @FormParam("password") String passwordInput,
            @FormParam("email") String emailInput
    ) {
        logger.debug("Processing registration request for username: {}", usernameInput);

        // Perform data validation on inputs
        String validatedUsername = ValidationUtil.validateLength(usernameInput, "username", 3, 50);
        ValidationUtil.validateUsername(validatedUsername, "username");

        String validatedPassword = ValidationUtil.validateLength(passwordInput, "password", 8, 50);

        String validatedEmail = ValidationUtil.validateLength(emailInput, "email", 1, 100);
        ValidationUtil.validateEmail(validatedEmail, "email");

        // Create a new UserRegistration entity with validated data
        UserRegistration newUserRegistration = new UserRegistration();
        newUserRegistration.setUsername(validatedUsername);
        newUserRegistration.setEmail(validatedEmail);
        newUserRegistration.setPassword(validatedPassword);
        // Setting status and date here as in original code, though DAO might override/default
        newUserRegistration.setStatus(Constants.DEFAULT_REGISTRATION_STATUS);
        newUserRegistration.setRegistrationDate(new Date());
        newUserRegistration.setAdminComment(null); // Set to null, DAO might set a default

        try {
            // Call the injected DAO to persist the registration request
            userRegistrationDao.create(newUserRegistration);
            logger.info("Successfully created user registration for {}", validatedUsername);
        } catch (Exception daoException) {
            // Catching generic Exception and checking message string as per original code logic.
            // This is less ideal than catching specific exceptions but adheres to constraints.
            if ("AlreadyExistingUsername".equals(daoException.getMessage())) {
                logger.warn("Registration failed: username {} already exists", validatedUsername);
                throw new ClientException("AlreadyExistingUsername", "Login already used", daoException);
            } else {
                logger.error("An unexpected server error occurred during registration", daoException);
                throw new ServerException("UnknownError", "Unknown server error", daoException);
            }
        }

        // Build the success response JSON
        JsonObjectBuilder successResponseBuilder = Json.createObjectBuilder()
                .add("status", "ok");

        return Response.ok().entity(successResponseBuilder.build()).build();
    }

    /**
     * Provides a list of all user registrations.
     * Requires user authentication.
     *
     * @param sortColumnInput The index representing the column to sort results by.
     * @param ascInput        Boolean flag: true for ascending sort, false for descending.
     * @return A Response containing a JSON array of registration details.
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
     */
    @GET
    @Path("/list")
    public Response list
    (
            @QueryParam("sort_column") Integer sortColumnInput,
            @QueryParam("asc") Boolean ascInput
    ) {
        // Verify user authentication status
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        logger.debug("Retrieving list of user registrations with sort column: {} and ascending: {}", sortColumnInput, ascInput);

        // Create sorting criteria object
        SortCriteria sortParameters = new SortCriteria(sortColumnInput, ascInput);

        // Fetch registration DTOs using the injected DAO's findByCriteria method.
        // Pass null for criteria to get all records as intended by this list endpoint.
        List<UserRegistrationDto> fetchedRegistrationDtoList = userRegistrationDao.findAllRegisters(sortParameters);

        // Build the JSON array for the response
        JsonArrayBuilder registrationsJsonArrayBuilder = Json.createArrayBuilder();
        for (UserRegistrationDto registrationDto : fetchedRegistrationDtoList) {
            JsonObjectBuilder registrationJsonObjectBuilder = Json.createObjectBuilder()
                    .add("id", registrationDto.getId())
                    .add("username", registrationDto.getUsername())
                    // Security Improvement: Do NOT expose the password in the response
                    // .add("password", registrationDto.getPassword())
                    .add("email", registrationDto.getEmail());

            // Include optional fields if available in the DTO
            if (registrationDto.getRegistrationDate() != 0) // Assuming 0 indicates unset/default long timestamp
                registrationJsonObjectBuilder.add("registration_date", registrationDto.getRegistrationDate());
            if (registrationDto.getStatus() != null)
                registrationJsonObjectBuilder.add("status", registrationDto.getStatus());
            if (registrationDto.getAdminComment() != null)
                registrationJsonObjectBuilder.add("admin_comment", registrationDto.getAdminComment());

            registrationsJsonArrayBuilder.add(registrationJsonObjectBuilder);
        }

        logger.debug("Successfully retrieved {} user registrations.", fetchedRegistrationDtoList.size());

        // Construct the final response JSON object
        JsonObjectBuilder responseJsonObjectBuilder = Json.createObjectBuilder()
                .add("registrations", registrationsJsonArrayBuilder); // Note: Changed key from "requests" to "registrations" to match API success description

        return Response.ok().entity(responseJsonObjectBuilder.build()).build();
    }

    /**
     * Approves a previously submitted user registration.
     * Requires user authentication.
     *
     * @param registrationIdToApprove The unique identifier of the registration to approve.
     * @return A standard JAX-RS Response object.
     * @api {put} /approve Approve registration
     * @apiName approveRegistration
     * @apiParam {String} id ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error (Implicitly handled by DAO/persistence layer or NotFound check).
     * @apiError (client) UserRegistrationNotFound User registration not found.
     * @apiError (server) UnknownError Unknown server error.
     * @apiVersion 1.5.0
     */
    @PUT
    @Path("/approveRegistration")
    public Response approveRegistration
    (
            @FormParam("id") String registrationIdToApprove
    ) {
        logger.debug("Initiating approval process for registration ID: {}", registrationIdToApprove);

        // Ensure the user is authenticated
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Retrieve the target registration using the injected DAO
        // Assuming the DAO method is findById as per the latest code provided
        UserRegistration targetRegistration = userRegistrationDao.findById(registrationIdToApprove);

        // Check if the registration exists
        if (targetRegistration == null) {
            logger.warn("Attempted to approve non-existent registration ID: {}", registrationIdToApprove);
            throw new ClientException("UserRegistrationNotFound", "User registration with the provided ID was not found.");
        }

        try {
            // Update the status of the retrieved entity
            targetRegistration.setStatus(Constants.APPROVED_REGISTRATION_STATUS);

            // Call the injected DAO to persist the update.
            // The update method signature in the refactored DAO matches (entity, id).
            userRegistrationDao.update(targetRegistration, registrationIdToApprove);

            logger.info("User registration {} approved successfully", targetRegistration.getUsername());
        } catch (Exception daoException) {
            // Catching generic Exception as per original code logic and constraints.
            logger.error("An unexpected server error occurred during registration approval", daoException);
            throw new ServerException("UnknownError", "Unknown server error during approval process", daoException);
        }

        // Build the success response JSON
        JsonObjectBuilder approvalSuccessResponseBuilder = Json.createObjectBuilder()
                .add("status", "ok");

        return Response.ok().entity(approvalSuccessResponseBuilder.build()).build();
    }
}