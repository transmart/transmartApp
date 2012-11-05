alter table BIO_ASY_ANALYSIS_DATA_EXT
   drop constraint FK_BIO_ASY__BIO_ASY_A_BIO_ASSA;

drop table BIO_ASY_ANALYSIS_DATA_EXT cascade constraints;

/*==============================================================*/
/* Table: BIO_ASY_ANALYSIS_DATA_EXT                             */
/*==============================================================*/
create table BIOMART.BIO_ASY_ANALYSIS_DATA_EXT 
(
   BIO_ASY_ANALYSIS_DATA_ID NUMBER(22),
   EXT_TYPE             VARCHAR2(20),
   EXT_DATA             VARCHAR2(4000)
);

alter table BIOMART.BIO_ASY_ANALYSIS_DATA_EXT
   add constraint FK_BIO_ASY__BIO_ASY_A_BIO_ASSA foreign key (BIO_ASY_ANALYSIS_DATA_ID)
      references BIOMART.BIO_ASSAY_ANALYSIS_GWAS (BIO_ASY_ANALYSIS_GWAS_ID);
