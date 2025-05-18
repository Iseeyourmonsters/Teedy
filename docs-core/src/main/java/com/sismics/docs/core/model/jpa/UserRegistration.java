package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration entity.
 */
@Entity
@Table(name = "T_USER_REGISTRATION")
public class UserRegistration implements Loggable{
    /**
     * User registration ID.
     */
    @Id
    @Column(name = "REG_ID", length = 36)
    private String id;

    /**
     * User's username.
     */
    @Column(name = "REG_USERNAME", length = 100, nullable = false)
    private String username;

    /**
     * User's email.
     */
    @Column(name = "REG_EMAIL", length = 100, nullable = false)
    private String email;

    /**
     * User's password.
     */
    @Column(name = "REG_PASSWORD", length = 100, nullable = false)
    private String password;

    /**
     * Registration date.
     */
    @Column(name = "REG_DATE", nullable = false)
    private Date RegistrationDate;

    /**
     * Registration status.
     */
    @Column(name = "REG_STATUS", length = 20, nullable = false)
    private String status;

    /**
     * Admin comment.
     */
    @Column(name = "REG_ADMIN_COMMENT", length = 500)
    private String adminComment;

    // Getters and setters
    public String getId() {
        return id;
    }
    public UserRegistration setId(String id) {
        this.id = id;
        return this;
    }
    public String getUsername() {
        return username;
    }
    public UserRegistration setUsername(String username) {
        this.username = username;
        return this;
    }
    public String getEmail() {
        return email;
    }
    public UserRegistration setEmail(String email) {
        this.email = email;
        return this;
    }
    public String getPassword() {
        return password;
    }
    public UserRegistration setPassword(String password) {
        this.password = password;
        return this;
    }
    public Date getRegistrationDate() {
        return RegistrationDate;
    }
    public void setRegistrationDate(Date registrationDate) {
        RegistrationDate = registrationDate;
    }
    public String getStatus() {
        return status;
    }
    public UserRegistration setStatus(String status) {
        this.status = status;
        return this;
    }
    public String getAdminComment() {
        return adminComment;
    }
    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    @Override
    public String toString() {
        return "UserRegistration = {" +
                " id=[" + id + "],"+
                " username=[" + username +  "],"+
                " email=[" + email + "],"+
                " password=[" + password + "],"+
                " registrationDate=[" + RegistrationDate +"],"+
                " status=[" + status + "],"+
                " adminComment=[" + adminComment +
                '}';
    }
    @Override
    public String toMessage() {
        return username;
    }

    /**
     * Loggable are soft deletable.
     *
     * @return deleteDate
     */
    @Override
    public Date getDeleteDate() {
        return null;
    }

}