alter table BIOMART.BIO_ASSAY_ANALYSIS_GWAS
   drop constraint FK_BIO_ASSA_BIO_ASY_A_BIO_ASSA;

alter table BIO_ASY_ANALYSIS_DATA_EXT
   drop constraint FK_BIO_ASY__BIO_ASY_A_BIO_ASSA;

drop table BIOMART.BIO_ASSAY_ANALYSIS_GWAS cascade constraints;

/*==============================================================*/
/* Table: BIO_ASSAY_ANALYSIS_GWAS                               */
/*==============================================================*/
create table BIOMART.BIO_ASSAY_ANALYSIS_GWAS 
(
   BIO_ASY_ANALYSIS_GWAS_ID NUMBER(18)           not null,
   BIO_ASSAY_ANALYSIS_ID NUMBER(18),
   RS_ID                NVARCHAR2(50),
   P_VALUE              NUMBER(18,5),
   LOG_P_VALUE          NUMBER(18,5),
   ETL_ID               NVARCHAR2(100),
   constraint PK_BIO_ASSAY_ANALYSIS_GWAS primary key (BIO_ASY_ANALYSIS_GWAS_ID)
);

alter table BIOMART.BIO_ASSAY_ANALYSIS_GWAS
   add constraint FK_BIO_ASSA_BIO_ASY_A_BIO_ASSA foreign key (BIO_ASSAY_ANALYSIS_ID)
      references BIOMART.BIO_ASSAY_ANALYSIS (BIO_ASSAY_ANALYSIS_ID);
/

drop trigger BIOMART.TRG_BIO_ASY_ANALYSIS_GWAS_ID
/

create trigger BIOMART.TRG_BIO_ASY_ANALYSIS_GWAS_ID  before insert on BIOMART.BIO_ASSAY_ANALYSIS_GWAS for each row
begin     
  if inserting then       
    if :NEW."BIO_ASY_ANALYSIS_GWAS_ID" is null then          
      select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_GWAS_ID" from dual;
    end if;
  end if;
end;
