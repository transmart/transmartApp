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

create trigger BIOMART.TRG_BIO_ASY_ANYS_DATA_IDX  before insert on BIOMART.BIO_ASY_ANALYSIS_DATA_IDX for each row
begin     
  if inserting then       
    if :NEW."BIO_ASY_ANALYSIS_DATA_IDX_ID" is null then          
      select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_DATA_IDX_ID" from dual;
    end if;
  end if;
end;