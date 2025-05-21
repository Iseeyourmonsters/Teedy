package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.AuditLogDao;
import com.sismics.docs.core.dao.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.dto.AuditLogDto;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.sql.Timestamp;

/**
 * Audit log REST resources.
 * 
 * @author bgamard
 */
@Path("/auditlog")
public class AuditLogResource extends BaseResource {
    /**
     * Returns the list of all logs for a document or user.
     *
     * @api {get} /auditlog Get audit logs
     * @apiDescription If no document ID is provided, logs for the current user will be returned.
     * @apiName GetAuditlog
     * @apiGroup Auditlog
     * @apiParam {String} [document] Document ID
     * @apiSuccess {String} total Total number of logs
     * @apiSuccess {Object[]} logs List of logs
     * @apiSuccess {String} logs.id ID
     * @apiSuccess {String} logs.username Username
     * @apiSuccess {String} logs.target Entity ID
     * @apiSuccess {String="Acl","Comment","Document","File","Group","Tag","User","RouteModel","Route"} logs.class Entity type
     * @apiSuccess {String="CREATE","UPDATE","DELETE"} logs.type Type
     * @apiSuccess {String} logs.message Message
     * @apiSuccess {Number} logs.create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response list(@QueryParam("document") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // On a document or a user?
        PaginatedList<AuditLogDto> paginatedList = PaginatedLists.create(20, 0);
        SortCriteria sortCriteria = new SortCriteria(1, false);
        AuditLogCriteria criteria = new AuditLogCriteria();
        if (Strings.isNullOrEmpty(documentId)) {
            // Search logs for a user
            criteria.setUserId(principal.getId());
            criteria.setAdmin(SecurityUtil.skipAclCheck(getTargetIdList(null)));
        } else {
            // Check ACL on the document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
                throw new NotFoundException();
            }
            criteria.setDocumentId(documentId);
        }
        
        // Search the logs
        AuditLogDao auditLogDao = new AuditLogDao();
        auditLogDao.findByCriteria(paginatedList, criteria, sortCriteria);
        
        // Assemble the results
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (AuditLogDto auditLogDto : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("id", auditLogDto.getId())
                    .add("username", auditLogDto.getUsername())
                    .add("target", auditLogDto.getEntityId())
                    .add("class", auditLogDto.getEntityClass())
                    .add("type", auditLogDto.getType().name())
                    .add("message", JsonUtil.nullable(auditLogDto.getMessage()))
                    .add("create_date", auditLogDto.getCreateTimestamp()));
        }

        // Send the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("logs", logs)
                .add("total", paginatedList.getResultCount());
        return Response.ok().entity(response.build()).build();
    }

    @GET
    @Path("/uploadStats")
    public Response getUploadStats() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AuditLogDao auditLogDao = new AuditLogDao();
        var results = auditLogDao.countUploadsByType();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] row : results) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("type", (String) row[0])
                    .add("total_count", ((Number) row[1]).intValue())
                    .add("unique_users", ((Number) row[2]).intValue())
                    .add("first_upload", ((Timestamp) row[3]).getTime())
                    .add("last_upload", ((Timestamp) row[4]).getTime()));
        }

        return Response.ok(arrayBuilder.build()).build();
    }

    @GET
    @Path("/activityTimeline")
    public Response getActivityTimeline() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AuditLogDao auditLogDao = new AuditLogDao();
        var results = auditLogDao.getActivityTimeline();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] row : results) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("user_id", (String) row[0])
                    .add("username", (String) row[1])
                    .add("entity_type", (String) row[2])
                    .add("activity_type", (String) row[3])
                    .add("timestamp", ((Timestamp) row[4]).getTime())
                    .add("message", JsonUtil.nullable((String) row[5])));
        }

        return Response.ok(arrayBuilder.build()).build();
    }

    @GET
    @Path("/userActivityStats")
    public Response getUserActivityStats() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AuditLogDao auditLogDao = new AuditLogDao();
        var results = auditLogDao.getUserActivityStats();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] row : results) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("username", (String) row[0])
                    .add("total_activities", ((Number) row[1]).intValue())
                    .add("creations", ((Number) row[2]).intValue())
                    .add("updates", ((Number) row[3]).intValue())
                    .add("deletions", ((Number) row[4]).intValue())
                    .add("first_activity", ((Timestamp) row[5]).getTime())
                    .add("last_activity", ((Timestamp) row[6]).getTime()));
        }

        return Response.ok(arrayBuilder.build()).build();
    }

    @GET
    @Path("/userUploadCount")
    public Response getUserUploadCount() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AuditLogDao auditLogDao = new AuditLogDao();
        var results = auditLogDao.countUploadsByUser();  // 返回 List<Object[]>，每项是 [username, count]

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        System.out.println();
        System.out.println("getUserUploadCount called");
        for (Object[] row : results) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("username", (String) row[0])
                    .add("count", ((Number) row[1]).intValue()));
            System.out.println(row[0] + " " + row[1]);
        }

        return Response.ok(arrayBuilder.build()).build();
    }

    @GET
    @Path("/fileTypeStats")
    public Response getFileTypeStats() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        AuditLogDao auditLogDao = new AuditLogDao();
        var results = auditLogDao.getFileTypeStats();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] row : results) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("type", (String) row[0])
                    .add("count", ((Number) row[1]).intValue())
                    .add("total_size", ((Number) row[2]).longValue()));
        }

        return Response.ok(arrayBuilder.build()).build();
    }
}
