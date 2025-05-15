package com.sismics.docs.core.dao.dto;

import com.google.common.base.MoreObjects;

/**
 * User registration DTO.
 *
 * @author sicheng
 */
public class UserRegistrationDto {
    /**
     * User registration ID.
     */
    private String id;

    /**
     * User's username.
     */
    private String username;

    /**
     * User's password.
     */
    private String password;

    /**
     * User's email.
     */
    private String email;

    /**
     * Creation date.
     */
    private Long registrationDate;

    /**
     * Registration status.
     */
    private String status;

    /**
     * Admin comment.
     */
    private String adminComment;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Long getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(Long registrationDate) {
        this.registrationDate = registrationDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getAdminComment() {
        return adminComment;
    }
    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .add("registrationDate", registrationDate)
                .add("status", status)
                .add("adminComment", adminComment)
                .toString();
    }
}