-- DBUPDATE-032-0.SQL

create memory table T_USER_REGISTRATION ( REG_ID varchar(36) not null, REG_USERNAME varchar(50) not null, REG_PASSWORD varchar(50) not null, REG_EMAIL varchar(100) not null, REG_DATE datetime not null, REG_STATUS varchar(20) not null, REG_ADMIN_COMMENT varchar(500), primary key (REG_ID) );

insert into T_USER_REGISTRATION(REG_ID, REG_USERNAME, REG_PASSWORD, REG_EMAIL, REG_DATE, REG_STATUS, REG_ADMIN_COMMENT) values('1', 'zbc', 'pwdzbc', '123456@example.com', NOW(), 'pending', '');
insert into T_USER_REGISTRATION(REG_ID, REG_USERNAME, REG_PASSWORD, REG_EMAIL, REG_DATE, REG_STATUS, REG_ADMIN_COMMENT) values('2', 'fqh', 'pwdfqh', '147258@example.com', NOW(), 'pending', '');
insert into T_USER_REGISTRATION(REG_ID, REG_USERNAME, REG_PASSWORD, REG_EMAIL, REG_DATE, REG_STATUS, REG_ADMIN_COMMENT) values('3', 'zmx', 'pwdzmx', '357896@example.com', NOW(), 'pending', '');
