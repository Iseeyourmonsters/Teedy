package com.sismics.docs.core.dao;
import com.sismics.docs.core.constant.Constants;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.UserRegistrationCriteria;
import com.sismics.docs.core.dao.dto.UserRegistrationDto;
import com.sismics.docs.core.model.jpa.UserRegistration;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.util.*;

/**
 * User registration DAO.
 *
 * @author sicheng
 */
public class UserRegistrationDao {
    /**
     * Create a new user registration.
     *
     * @param userRegistration the user registration
     * @return the ID of the created user registration
     */
    public String create(UserRegistration userRegistration) throws Exception {
        // Create the registration UUID
        userRegistration.setId(UUID.randomUUID().toString());

        // Checks for registration unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from UserRegistration r where r.username = :username");
        q.setParameter("username", userRegistration.getUsername());
        if (!q.getResultList().isEmpty()) {
            throw new Exception("AlreadyExistingRegistrationUsername");
        }

        // Create the user
        userRegistration.setRegistrationDate(new Date());
        userRegistration.setStatus(Constants.DEFAULT_REGISTRATION_STATUS);
        userRegistration.setAdminComment("default comment");
        em.persist(userRegistration);

        // Create audit log
        AuditLogUtil.create(userRegistration, AuditLogType.CREATE, userRegistration.getId());

        return userRegistration.getId();
    }

    /**
     * Update an existing user registration.
     *
     * @param userRegistration the user registration
     * @param id the ID of the user registration
     * @return the updated user registration
     */
    public UserRegistration update(UserRegistration userRegistration, String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the registration
        Query q = em.createQuery("select r from UserRegistration r where r.id = :id");
        q.setParameter("id", id);
        UserRegistration userRegistrationDb = (UserRegistration) q.getSingleResult();

        // Update the registration
        userRegistrationDb.setUsername(userRegistration.getUsername());
        userRegistrationDb.setEmail(userRegistration.getEmail());
        userRegistrationDb.setRegistrationDate(userRegistration.getRegistrationDate());
        userRegistrationDb.setStatus(userRegistration.getStatus());
        userRegistrationDb.setAdminComment(userRegistration.getAdminComment());

        // Create audit log
        AuditLogUtil.create(userRegistrationDb, AuditLogType.UPDATE, id);

        return userRegistration;
    }

    /**
     * Find user registrations by criteria.
     *
     * @param criteria the search criteria
     * @param sortCriteria the sort criteria
     * @return a list of user registrations
     */
    public List<UserRegistrationDto> findByCriteria(UserRegistrationCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select r.REG_ID as c0, r.REG_USERNAME as c1, REG_PASSWORD as c2, r.REG_EMAIL as c3, r.REG_DATE as c4, r.REG_STATUS as c5, r.REG_ADMIN_COMMENT as c6");
        sb.append(" from T_USER_REGISTRATION r ");

        // Add criteria
        if (criteria.getSearch() != null) {
            criteriaList.add("lower(r.REG_USERNAME) like lower(:search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        if (criteria.getUserRegistrationId() != null) {
            criteriaList.add("r.REG_ID = :userRegistrationId");
            parameterMap.put("userRegistrationId", criteria.getUserRegistrationId());
        }
        if (criteria.getUsername() != null) {
            criteriaList.add("r.REG_USERNAME = :username");
            parameterMap.put("username", "%" + criteria.getUsername() + "%");
        }
        if (criteria.getEmail() != null) {
            criteriaList.add("r.REG_EMAIL = :email");
            parameterMap.put("email", "%" + criteria.getEmail() + "%");
        }

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<UserRegistrationDto> userRegistrationDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
            userRegistrationDto.setId((String) o[i++]);
            userRegistrationDto.setUsername((String) o[i++]);
            userRegistrationDto.setPassword((String) o[i++]);
            userRegistrationDto.setEmail((String) o[i++]);
            userRegistrationDto.setRegistrationDate(((Timestamp) o[i++]).getTime());
            userRegistrationDto.setStatus((String) o[i++]);
            userRegistrationDto.setAdminComment((String) o[i++]);
            userRegistrationDtoList.add(userRegistrationDto);
        }

        return userRegistrationDtoList;
    }

    /**
     * Gets a user registration by its ID.
     *
     * @param id registration ID
     * @return UserRegistration
     */
    public UserRegistration getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(UserRegistration.class, id);
        } catch (Exception e) {
            return null;
        }
    }
}