drop table BIOMART.BIO_ASSAY_ANALYSIS_EQTL cascade constraints;

/*==============================================================*/
/* Table: BIO_ASSAY_ANALYSIS_EQTL                               */
/*==============================================================*/
create table BIOMART.BIO_ASSAY_ANALYSIS_EQTL 
(
   BIO_ASY_ANALYSIS_DATA_ID NUMBER(22)           not null,
   BIO_ASSAY_ANALYSIS_ID NUMBER(22),
   RS_ID                NVARCHAR2(50),
   GENE                 CHAR(10),
   P_VALUE              NUMBER(18,5),
   CIS_TRANS            CHAR(10),
   DISTANCE_FROM_GENE   CHAR(10),
   ETL_ID               NVARCHAR2(100),
   constraint PK_BIO_ASSAY_ANALYSIS_EQTL primary key (BIO_ASY_ANALYSIS_DATA_ID)
);

create trigger BIOMART.TRG_BIO_ASY_ANALYSIS_EQTL_ID  before insert on BIOMART.BIO_ASSAY_ANALYSIS_EQTL for each row
begin     
  if inserting then       
    if :NEW."BIO_ASY_ANALYSIS_DATA_ID" is null then          
      select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_DATA_ID" from dual;
    end if;
  end if;
end;