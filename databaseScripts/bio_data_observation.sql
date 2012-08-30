alter table BIOMART.BIO_DATA_OBSERVATION
   drop constraint FK_BIO_DATA_REFERENCE_BIO_ASSA;

alter table BIOMART.BIO_DATA_OBSERVATION
   drop constraint FK_BIO_DATA_REFERENCE_BIO_OBSE;

drop index BIOMART.BIO_DT_DIS_DID_IDX2;

drop index BIOMART.BIO_DD_IDX3;

drop table BIOMART.BIO_DATA_OBSERVATION cascade constraints;

/*==============================================================*/
/* Table: BIO_DATA_OBSERVATION                                  */
/*==============================================================*/
create table BIOMART.BIO_DATA_OBSERVATION 
(
   BIO_DATA_ID          NUMBER(18)           not null,
   BIO_OBSERVATION_ID   NUMBER(18)           not null,
   ETL_SOURCE           VARCHAR2(100),
   constraint BIO_DATA_OBSERVATION_PK primary key (BIO_DATA_ID, BIO_OBSERVATION_ID)
         using index
       pctfree 10
       initrans 2
       storage
       (
           initial 64K
           minextents 1
           maxextents unlimited
       )
       tablespace INDX
        nologging
)
initrans 1
storage
(
    initial 64K
    minextents 1
    maxextents unlimited
)
tablespace BIOMART
nologging
monitoring
 noparallel;

/*==============================================================*/
/* Index: BIO_DD_IDX3                                           */
/*==============================================================*/
create index BIOMART.BIO_DD_IDX3 on BIOMART.BIO_DATA_OBSERVATION (
   BIO_OBSERVATION_ID ASC
)
pctfree 10
initrans 2
storage
(
    initial 64K
    minextents 1
    maxextents unlimited
    buffer_pool default
)
tablespace INDX
nologging
 parallel 4;

/*==============================================================*/
/* Index: BIO_DT_DIS_DID_IDX2                                   */
/*==============================================================*/
create index BIOMART.BIO_DT_DIS_DID_IDX2 on BIOMART.BIO_DATA_OBSERVATION (
   BIO_DATA_ID ASC
)
pctfree 10
initrans 2
storage
(
    initial 64K
    minextents 1
    maxextents unlimited
    buffer_pool default
)
tablespace INDX
nologging
 parallel 4;

alter table BIOMART.BIO_DATA_OBSERVATION
   add constraint FK_BIO_DATA_REFERENCE_BIO_ASSA foreign key (BIO_DATA_ID)
      references BIOMART.BIO_ASSAY_ANALYSIS (BIO_ASSAY_ANALYSIS_ID);

alter table BIOMART.BIO_DATA_OBSERVATION
   add constraint FK_BIO_DATA_REFERENCE_BIO_OBSE foreign key (BIO_OBSERVATION_ID)
      references BIOMART.BIO_OBSERVATION (BIO_OBSERVATION_ID);
