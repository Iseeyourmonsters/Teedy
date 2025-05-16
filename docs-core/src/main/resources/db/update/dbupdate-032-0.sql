-- DBUPDATE-032-0.SQL

create memory table T_USER_REGISTRATION ( REG_ID varchar(36) not null, REG_USERNAME varchar(50) not null, REG_PASSWORD varchar(50) not null, REG_EMAIL varchar(100) not null, REG_DATE datetime not null, REG_STATUS varchar(20) not null, REG_ADMIN_COMMENT varchar(500), primary key (REG_ID) );

-- Update the database version
update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION';