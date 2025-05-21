package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.dto.AuditLogDto;
import com.sismics.docs.core.model.jpa.AuditLog;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.*;

/**
 * Audit log DAO.
 * 
 * @author bgamard
 */
public class AuditLogDao {
    /**
     * Creates a new audit log.
     * 
     * @param auditLog Audit log
     * @return New ID
     */
    public String create(AuditLog auditLog) {
        // Create the UUID
        auditLog.setId(UUID.randomUUID().toString());
        
        // Create the audit log
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        auditLog.setCreateDate(new Date());
        em.persist(auditLog);
        
        return auditLog.getId();
    }
    
    /**
     * Searches audit logs by criteria.
     * 
     * @param paginatedList List of audit logs (updated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     */
    public void findByCriteria(PaginatedList<AuditLogDto> paginatedList, AuditLogCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        
        StringBuilder baseQuery = new StringBuilder("select l.LOG_ID_C c0, l.LOG_CREATEDATE_D c1, u.USE_USERNAME_C c2, l.LOG_IDENTITY_C c3, l.LOG_CLASSENTITY_C c4, l.LOG_TYPE_C c5, l.LOG_MESSAGE_C c6 from T_AUDIT_LOG l ");
        baseQuery.append(" join T_USER u on l.LOG_IDUSER_C = u.USE_ID_C ");
        List<String> queries = Lists.newArrayList();
        
        // Adds search criteria
        if (criteria.getDocumentId() != null) {
            // ACL on document is not checked here, rights have been checked before
            queries.add(baseQuery + " where l.LOG_IDENTITY_C = :documentId ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select f.FIL_ID_C from T_FILE f where f.FIL_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select c.COM_ID_C from T_COMMENT c where c.COM_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select a.ACL_ID_C from T_ACL a where a.ACL_SOURCEID_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select r.RTE_ID_C from T_ROUTE r where r.RTE_IDDOCUMENT_C = :documentId) ");
            parameterMap.put("documentId", criteria.getDocumentId());
        }
        
        if (criteria.getUserId() != null) {
            if (criteria.isAdmin()) {
                // For admin users, display all logs except ACL logs
                queries.add(baseQuery + " where l.LOG_CLASSENTITY_C != 'Acl' ");
            } else {
                // Get all logs originating from the user, not necessarly on owned items
                // Filter out ACL logs
                queries.add(baseQuery + " where l.LOG_IDUSER_C = :userId and l.LOG_CLASSENTITY_C != 'Acl' ");
                parameterMap.put("userId", criteria.getUserId());
            }
        }
        
        // Perform the search
        QueryParam queryParam = new QueryParam(Joiner.on(" union ").join(queries), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        // Assemble results
        List<AuditLogDto> auditLogDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            AuditLogDto auditLogDto = new AuditLogDto();
            auditLogDto.setId((String) o[i++]);
            auditLogDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            auditLogDto.setUsername((String) o[i++]);
            auditLogDto.setEntityId((String) o[i++]);
            auditLogDto.setEntityClass((String) o[i++]);
            auditLogDto.setType(AuditLogType.valueOf((String) o[i++]));
            auditLogDto.setMessage((String) o[i++]);
            auditLogDtoList.add(auditLogDto);
        }

        paginatedList.setResultList(auditLogDtoList);
    }

    public List<Object[]> countUploadsByType() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql = "SELECT " +
                "l.LOG_CLASSENTITY_C, " +
                "COUNT(l.LOG_ID_C) as total_count, " +
                "COUNT(DISTINCT l.LOG_IDUSER_C) as unique_users, " +
                "MIN(l.LOG_CREATEDATE_D) as first_upload, " +
                "MAX(l.LOG_CREATEDATE_D) as last_upload " +
                "FROM T_AUDIT_LOG l " +
                "WHERE l.LOG_TYPE_C = 'CREATE' and l.LOG_CLASSENTITY_C != 'Acl' " +
                "AND l.LOG_CLASSENTITY_C NOT IN ('User', 'UserRegistration') " +
                "GROUP BY l.LOG_CLASSENTITY_C " +
                "ORDER BY total_count DESC";
        
        List<Object[]> result = em.createNativeQuery(sql).getResultList();
        return result;
    }

    /**
     * Get activity timeline data for Gantt chart
     * @return List of activity data including start time, end time, and type
     */
    public List<Object[]> getActivityTimeline() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql = "SELECT " +
                "l.LOG_IDUSER_C, " +
                "u.USE_USERNAME_C, " +
                "l.LOG_CLASSENTITY_C, " +
                "l.LOG_TYPE_C, " +
                "l.LOG_CREATEDATE_D, " +
                "l.LOG_MESSAGE_C " +
                "FROM T_AUDIT_LOG l " +
                "JOIN T_USER u ON l.LOG_IDUSER_C = u.USE_ID_C " +
                "WHERE l.LOG_TYPE_C IN ('CREATE', 'UPDATE', 'DELETE') " +
                "ORDER BY l.LOG_CREATEDATE_D DESC";
        
        return em.createNativeQuery(sql).getResultList();
    }

    /**
     * Get user activity statistics
     * @return List of user activity data
     */
    public List<Object[]> getUserActivityStats() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql = "SELECT " +
                "u.USE_USERNAME_C, " +
                "COUNT(DISTINCT l.LOG_ID_C) as total_activities, " +
                "COUNT(DISTINCT CASE WHEN l.LOG_TYPE_C = 'CREATE' THEN l.LOG_ID_C END) as creations, " +
                "COUNT(DISTINCT CASE WHEN l.LOG_TYPE_C = 'UPDATE' THEN l.LOG_ID_C END) as updates, " +
                "COUNT(DISTINCT CASE WHEN l.LOG_TYPE_C = 'DELETE' THEN l.LOG_ID_C END) as deletions, " +
                "MIN(l.LOG_CREATEDATE_D) as first_activity, " +
                "MAX(l.LOG_CREATEDATE_D) as last_activity " +
                "FROM T_AUDIT_LOG l " +
                "JOIN T_USER u ON l.LOG_IDUSER_C = u.USE_ID_C " +
                "GROUP BY u.USE_USERNAME_C " +
                "ORDER BY total_activities DESC";
        
        return em.createNativeQuery(sql).getResultList();
    }

    public List<Object[]> countUploadsByUser() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql = "SELECT u.USE_USERNAME_C, COUNT(l.LOG_ID_C) " +
                "FROM T_AUDIT_LOG l " +
                "JOIN T_USER u ON l.LOG_IDUSER_C = u.USE_ID_C " +
                "WHERE l.LOG_TYPE_C = 'CREATE' and l.LOG_CLASSENTITY_C != 'Acl'" +
                "GROUP BY u.USE_USERNAME_C";
        List<Object[]> result = em.createNativeQuery(sql).getResultList();
        System.out.println("查询结果数量: " + result.size());
        for (Object[] row : result) {
            System.out.println("用户: " + row[0] + ", 次数: " + row[1]);
        }

        return result;
    }

    /**
     * Get file type statistics
     * @return List of file type statistics
     */
    public List<Object[]> getFileTypeStats() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql = "SELECT " +
                "CASE " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.pdf' THEN 'PDF' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.doc%' THEN 'Word' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.xls%' THEN 'Excel' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.ppt%' THEN 'PowerPoint' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.txt' THEN 'Text' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.jpg' OR LOWER(f.FIL_NAME_C) LIKE '%.jpeg' OR LOWER(f.FIL_NAME_C) LIKE '%.png' OR LOWER(f.FIL_NAME_C) LIKE '%.gif' THEN 'Image' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.mp4' OR LOWER(f.FIL_NAME_C) LIKE '%.avi' OR LOWER(f.FIL_NAME_C) LIKE '%.mov' THEN 'Video' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.mp3' OR LOWER(f.FIL_NAME_C) LIKE '%.wav' THEN 'Audio' " +
                "    WHEN LOWER(f.FIL_NAME_C) LIKE '%.zip' OR LOWER(f.FIL_NAME_C) LIKE '%.rar' THEN 'Archive' " +
                "    ELSE 'Other' " +
                "END as file_type, " +
                "COUNT(f.FIL_ID_C) as count, " +
                "SUM(f.FIL_SIZE_N) as total_size " +
                "FROM T_FILE f " +
                "JOIN T_AUDIT_LOG l ON f.FIL_ID_C = l.LOG_IDENTITY_C " +
                "WHERE l.LOG_TYPE_C = 'CREATE' " +
                "GROUP BY file_type " +
                "ORDER BY count DESC";
        
        return em.createNativeQuery(sql).getResultList();
    }
}
