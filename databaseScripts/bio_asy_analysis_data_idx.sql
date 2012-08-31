drop table BIO_ASY_ANALYSIS_DATA_IDX cascade constraints;

/*==============================================================*/
/* Table: BIO_ASY_ANALYSIS_DATA_IDX                             */
/*==============================================================*/
create table BIO_ASY_ANALYSIS_DATA_IDX 
(
   BIO_ASY_ANALYSIS_DATA_IDX_ID NUMBER(22)           not null,
   EXT_TYPE             NVARCHAR2(20),
   FIELD_NAME           NVARCHAR2(200),
   FIELD_IDX            NUMBER(3),
   DISPLAY_IDX          NUMBER(3),
   constraint PK_BIO_ASY_ANALYSIS_DATA_IDX primary key (BIO_ASY_ANALYSIS_DATA_IDX_ID)
);
