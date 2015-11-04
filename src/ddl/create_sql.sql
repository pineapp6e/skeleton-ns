/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     2014-6-24 17:22:47                           */
/*==============================================================*/


drop table if exists tb_gld_auth_cp_r;

drop table if exists tb_gls_auth_pn_c;

drop table if exists tb_uc_group_r;

drop table if exists tb_uc_notify_history_r;

drop table if exists tb_uc_notify_r;

drop table if exists tb_uc_reg_history_r;

drop table if exists tb_uc_user_r;

/*==============================================================*/
/* Table: tb_gld_auth_cp_r                                      */
/*==============================================================*/
create table tb_gld_auth_cp_r
(
   aucp_id              varchar(40) not null comment 'HCPS分配给CP的ID，如果是basic/digest方式，cpId包含在header的Authorization中。',
   aucp_passwd          varchar(20) not null comment 'the password to verify the glau_cp_id',
   aucp_name            varchar(40) not null default '' comment 'Name of CP, ',
   aucp_auth_method     smallint not null default 1 comment 'HTTP authentication method:
            #define NS_HTTP_AUTH_BASIC      1
            #define NS_HTTP_AUTH_DIGEST      2
            #define NS_HTTP_AUTH_IP_CONTROL 4
            Basic and digest are alternative, and IP control is optional.
            ',
   aucp_create_time     timestamp not null,
   aucp_control_ip      varchar(100) not null default '' comment 'permission source IP, split use comma, example:
            123,23.142.34,137.42.32.244',
   aucp_enable          smallint not null default 1 comment '0:disable
            1:enable',
   primary key (aucp_id)
);

alter table tb_gld_auth_cp_r comment 'used for  verify CP , save userName, passwd,  and so on.';

/*==============================================================*/
/* Table: tb_gls_auth_pn_c                                      */
/*==============================================================*/
create table tb_gls_auth_pn_c
(
   aupn_id              int not null auto_increment,
   aupn_aucpid_fk       varchar(40) not null,
   aupn_pn_type         smallint not null default 0 comment '0：HPNS  1：APNS  2：GCM',
   aupn_sender_id       varchar(200) not null default '',
   aupn_sender_auth_token varchar(200) not null default '' comment 'HPNS: secret key
            APNS: certification file.
            GCM: Api Key',
   aupn_pn_url          varchar(100) not null default '' comment 'the PN server URL.',
   aupn_pn_auth         smallint not null default 1 comment 'HCPS auth method:
            1 basic
            2 digest',
   aupn_enable          smallint not null default 1,
   aupn_production      smallint not null default 0,
   primary key (aupn_id)
);

alter table tb_gls_auth_pn_c comment 'used to configure the PN info. as HPNS, APNS ..and their aut';

/*==============================================================*/
/* Table: tb_uc_group_r                                         */
/*==============================================================*/
create table tb_uc_group_r
(
   grus_id              int not null auto_increment comment 'Auto-increment index',
   grus_name            varchar(50) not null default '' comment 'User group name',
   grus_apn_type        smallint not null default 0 comment '0：HPNS  1：APNS  2：GCM',
   grus_desc            varchar(100) not null default '' comment 'The description of user group',
   grus_create_time     timestamp not null,
   primary key (grus_id)
);

alter table tb_uc_group_r comment 'represent the user group or a patch in HCPS';

/*==============================================================*/
/* Table: tb_uc_notify_history_r                                */
/*==============================================================*/
create table tb_uc_notify_history_r
(
   nohi_ucnoid_fk       int not null comment 'Auto-increment index',
   nohi_total_num       int not null default 0 comment 'The total number of the target registration IDs of the queried notification',
   nohi_to_pn_num       int not null default 0 comment 'The number of the mobile users that have received the notification message',
   nohi_to_ma_num       int not null default 0 comment 'The number of the mobile users that have fetched the whole message',
   stno_update_time     timestamp not null comment 'Time of insert. ',
   primary key (nohi_ucnoid_fk)
);

alter table tb_uc_notify_history_r comment 'information for each notification in HCPS';

/*==============================================================*/
/* Table: tb_uc_notify_r                                        */
/*==============================================================*/
create table tb_uc_notify_r
(
   ucno_id              int not null auto_increment comment 'Auto-increment index',
   ucno_aupnid_fk       int not null,
   ucno_group_id        int not null default 0,
   ucno_app_id          int not null default 0,
   ucno_reg_id          varchar(100) not null default '' comment 'The registration IDs',
   ucno_payload         varchar(256) not null default '' comment 'content send to PN',
   ucno_expire          int not null default 0 comment 'Expired time of notification',
   ucno_status          smallint not null default 0 comment 'send status. 0 not send to pn, 1 sended to pn',
   ucno_create_time     timestamp not null comment 'Time of insert, used for debug',
   primary key (ucno_id)
);

alter table tb_uc_notify_r comment 'represent the notification message from CP.';

/*==============================================================*/
/* Table: tb_uc_reg_history_r                                   */
/*==============================================================*/
create table tb_uc_reg_history_r
(
   rehi_ucnoid_fk       int not null comment 'index',
   rehi_reg_id          varchar(100) not null comment 'Registration ID',
   rehi_status          smallint not null default 0 comment '0:send to PN 1: error 2:deliver ',
   rehi_to_pn_time      timestamp not null comment 'corresponding status time. ',
   rehi_to_ma_time      timestamp not null,
   rehi_error_code      int not null default 0,
   primary key (rehi_ucnoid_fk, rehi_reg_id)
);

alter table tb_uc_reg_history_r comment 'Information for each registration in HCPS.';

/*==============================================================*/
/* Table: tb_uc_user_r                                          */
/*==============================================================*/
create table tb_uc_user_r
(
   us_grusid_fk         int not null comment 'index',
   us_reg_id            varchar(100) not null,
   primary key (us_grusid_fk, us_reg_id)
);

      
 insert into tb_gld_auth_cp_r(aucp_id, aucp_passwd, aucp_auth_method)
 values ('testId','testpwd',2);
 
 insert into tb_gld_auth_cp_r(aucp_id, aucp_passwd, aucp_auth_method)
 values ('xitangtest','123654',1);
 
 
 insert into tb_gls_auth_pn_c(aupn_aucpid_fk,aupn_pn_type, aupn_sender_id, aupn_sender_auth_token,aupn_pn_url,aupn_pn_auth,aupn_production)
 values('testId',0,'test@126.com','123654','http://172.27.244.10:8080',2,0);
  
 insert into tb_gls_auth_pn_c(aupn_aucpid_fk,aupn_pn_type, aupn_sender_id, aupn_sender_auth_token,aupn_pn_url,aupn_pn_auth,aupn_production)
 values('testId',1,'apns_dev.p12','123456','http://gateway.sandbox.push.apple.com:2195',2,0);

  insert into tb_gls_auth_pn_c(aupn_aucpid_fk,aupn_pn_type, aupn_sender_id, aupn_sender_auth_token,aupn_pn_url,aupn_pn_auth,aupn_production)
 values('xitangtest',0,'xitang@126.com','123654','http://172.27.244.10:8080',1,0);

