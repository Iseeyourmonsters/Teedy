package com.sismics.docs.core.dao.criteria;

/**
 * User registration criteria.
 *
 * @author sicheng
 */
public class UserRegistrationCriteria {

    /**
     * Search query.
     */
    private String search;

    /**
     * User registration ID.
     */
    private String userRegistrationId;

    /**
     * User's username.
     */
    private String username;

    /**
     * User's email.
     */
    private String email;

    public String getSearch() {
        return search;
    }

    public UserRegistrationCriteria setSearch(String search) {
        this.search = search;
        return this;
    }

    public String getUserRegistrationId() {
        return userRegistrationId;
    }

    public UserRegistrationCriteria setUserRegistrationId(String userRegistrationId) {
        this.userRegistrationId = userRegistrationId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRegistrationCriteria setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRegistrationCriteria setEmail(String email) {
        this.email = email;
        return this;
    }
}