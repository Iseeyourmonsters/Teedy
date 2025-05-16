package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.Constants;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.UserRegistrationDto;
import com.sismics.docs.core.model.jpa.UserRegistration;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.*;

/**
 * User registration DAO.
 */
public class UserRegistrationDao {

    /**
     * Creates a new user registration record.
     *
     * @param userRegistration The user registration entity to persist.
     * @return The unique identifier of the newly created registration.
     * @throws Exception if a user with the same username already exists.
     */
    public String create(UserRegistration userRegistration) throws Exception {
        EntityManager entityManager = ThreadLocalContext.get().getEntityManager();
        // Checks for registration unicity
        Query q = entityManager.createQuery("select r from UserRegistration r where r.username = :username");
        String username = userRegistration.getUsername();
        q.setParameter("username", username);
        if (!q.getResultList().isEmpty()) {
            throw new Exception("The same username already exists: " + username);
        }

        // Create the registration UUID
        userRegistration.setId(UUID.randomUUID().toString());

        // Set initial values if not already set
        if (userRegistration.getRegistrationDate() == null)
            userRegistration.setRegistrationDate(new Date());
        if (userRegistration.getStatus() == null)
            userRegistration.setStatus(Constants.DEFAULT_REGISTRATION_STATUS);
        if (userRegistration.getAdminComment() == null || userRegistration.getAdminComment().isEmpty())
            userRegistration.setAdminComment("Registration submitted.");
        entityManager.persist(userRegistration);

        // Create audit log
        AuditLogUtil.create(userRegistration, AuditLogType.CREATE, userRegistration.getId());

        return userRegistration.getId();
    }

    /**
     * Update an existing user registration.
     *
     * @param newUser the user registration
     * @param id               the ID of the user registration
     * @return the updated user registration
     */
    public UserRegistration update(UserRegistration newUser, String id) {
        EntityManager entityManager = ThreadLocalContext.get().getEntityManager();

        // Get the registration
        Query q = entityManager.createQuery("select r from UserRegistration r where r.id = :id");
        q.setParameter("id", id);
        UserRegistration userRegistrationDb = (UserRegistration) q.getSingleResult();

        // Update the registration
        userRegistrationDb.setUsername(newUser.getUsername());
        userRegistrationDb.setEmail(newUser.getEmail());
        userRegistrationDb.setRegistrationDate(newUser.getRegistrationDate());
        userRegistrationDb.setStatus(newUser.getStatus());
        userRegistrationDb.setAdminComment(newUser.getAdminComment());

        // Create audit log
        AuditLogUtil.create(userRegistrationDb, AuditLogType.UPDATE, id);

        return newUser;
    }

    /**
     * Find all user registrations.
     *
     * @param sortCriteria the sort criteria
     * @return a list of user registrations
     */
    public List<UserRegistrationDto> findAllRegisters(SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(
                "select r.REG_ID as c0, r.REG_USERNAME as c1, REG_PASSWORD as c2, " +
                        "r.REG_EMAIL as c3, r.REG_DATE as c4, r.REG_STATUS as c5, " +
                        "r.REG_ADMIN_COMMENT as c6 from T_USER_REGISTRATION r "
                , parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> list = QueryUtil.getNativeQuery(queryParam).getResultList();

        return getUserRegistrationDtos(list);
    }

    /**
     * Patten change method
     * Change the database result list to userRegistrationDtoList.
     *
     * @param list database query result list.
     * @return a list of user registrations
     */
    @NotNull
    private static List<UserRegistrationDto> getUserRegistrationDtos(List<Object[]> list) {
        List<UserRegistrationDto> userRegistrationDtoList = new ArrayList<>();
        for (Object[] item : list) {
            UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                    (String) item[0], // id
                    (String) item[1], // username
                    (String) item[2], // password
                    (String) item[3], // email
                    ((Timestamp) item[4]).getTime(), // registrationDate
                    (String) item[5], // status
                    (String) item[6] // adminComment
            );
            userRegistrationDtoList.add(userRegistrationDto);
        }
        return userRegistrationDtoList;
    }

    /**
     * Find a user registration by its ID.
     *
     * @param id registration ID
     * @return UserRegistration
     */
    public UserRegistration findById(String id) {
        EntityManager entityManager = ThreadLocalContext.get().getEntityManager();
        try {
            return entityManager.find(UserRegistration.class, id);
        } catch (Exception e) {
            return null;
        }
    }
}