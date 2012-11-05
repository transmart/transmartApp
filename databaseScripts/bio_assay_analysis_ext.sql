alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   drop constraint FK_BIO_ASSA_BIO_ASY_A_BIO_ASSA;

alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   drop constraint FK_BIO_ASSA_REFERENCE_BIO_ASSA;

alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   drop constraint FK_BIO_ASSA_REFERENCE_BIO_ASSA;

drop table BIOMART.BIO_ASSAY_ANALYSIS_EXT cascade constraints;

/*==============================================================*/
/* Table: BIO_ASSAY_ANALYSIS_EXT                                */
/*==============================================================*/
create table BIOMART.BIO_ASSAY_ANALYSIS_EXT 
(
   BIO_ASSAY_ANALYSIS_ID NUMBER(22)           not null,
   BIO_ASSAY_EXPRESSION_PLATFORM_ NUMBER(18),
   BIO_ASSAY_GENOTYPE_PLATFORM_ID NUMBER(18),
   GNOME_VERSION        CHAR(10),
   TISSUE               CHAR(10),
   CELL_TYPE            CHAR(10),
   POPULATION           CHAR(10),
   RU                   CHAR(10),
   SAMPLE_SIZE          CHAR(10),
   constraint PK_BIO_ASSAY_ANALYSIS_EXT primary key (BIO_ASSAY_ANALYSIS_ID)
);

alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   add constraint FK_BIO_ASSA_BIO_ASY_A_BIO_ASSA foreign key (BIO_ASSAY_ANALYSIS_ID)
      references BIOMART.BIO_ASSAY_ANALYSIS (BIO_ASSAY_ANALYSIS_ID);

alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   add constraint FK_BIO_ASSA_REFERENCE_BIO_ASSA foreign key (BIO_ASSAY_EXPRESSION_PLATFORM_)
      references BIOMART.BIO_ASSAY_PLATFORM (BIO_ASSAY_PLATFORM_ID);

alter table BIOMART.BIO_ASSAY_ANALYSIS_EXT
   add constraint FK_BIO_ASSA_REFERENCE_BIO_ASSA foreign key (BIO_ASSAY_GENOTYPE_PLATFORM_ID)
      references BIOMART.BIO_ASSAY_PLATFORM (BIO_ASSAY_PLATFORM_ID);
