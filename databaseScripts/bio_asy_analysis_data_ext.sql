alter table BIO_ASY_ANALYSIS_DATA_EXT
   drop constraint PK_BIO_ASSAY_ANALYSIS_DATA_EXT;

alter table BIO_ASY_ANALYSIS_DATA_EXT
   drop constraint FK_BIO_ASY__BIO_ASY_A_BIO_ASSA;

drop table BIOMART.BIO_ASY_ANALYSIS_DATA_EXT cascade constraints;

/*==============================================================*/
/* Table: BIO_ASY_ANALYSIS_DATA_EXT                             */
/*==============================================================*/
create table BIOMART.BIO_ASY_ANALYSIS_DATA_EXT 
(
   BIO_ASY_ANALYSIS_DATA_EXT_ID NUMBER(18)           not null,
   BIO_ASY_ANALYSIS_DATA_ID NUMBER(22),
   EXT_TYPE             VARCHAR2(20),
   EXT_DATA             VARCHAR2(4000),
   constraint PK_BIO_ASSAY_ANALYSIS_DATA_EXT primary key (BIO_ASY_ANALYSIS_DATA_EXT_ID)
);

alter table BIOMART.BIO_ASY_ANALYSIS_DATA_EXT
   add constraint FK_BIO_ASY__BIO_ASY_A_BIO_ASSA foreign key (BIO_ASY_ANALYSIS_DATA_ID)
      references BIOMART.BIO_ASSAY_ANALYSIS_GWAS (BIO_ASY_ANALYSIS_GWAS_ID);

drop trigger BIOMART.TRG_BIO_ASY_ANALYSIS_DATA_EXT_ID
/

create trigger BIOMART.TRG_BIO_ASY_ANALYSIS_DATA_EXT before insert on BIOMART.BIO_ASY_ANALYSIS_DATA_EXT for each row
begin     
  if inserting then       
    if :NEW."BIO_ASY_ANALYSIS_DATA_EXT_ID" is null then          
      select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_DATA_EXT_ID" from dual;
    end if;
  end if;
end;      
