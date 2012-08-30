drop trigger BIOMART.TRG_BIO_OBSERVATION_ID
/

alter table BIOMART.BIO_DATA_OBSERVATION
   drop constraint FK_BIO_DATA_REFERENCE_BIO_OBSE
/

drop table BIOMART.BIO_OBSERVATION cascade constraints
/

/*==============================================================*/
/* Table: BIO_OBSERVATION                                       */
/*==============================================================*/
create table BIOMART.BIO_OBSERVATION 
(
   BIO_OBSERVATION_ID   NUMBER(18)           not null,
   VALUE                NVARCHAR2(510)       not null,
   VOCAB                NVARCHAR2(510),
   VOCAB_CODE           NVARCHAR2(510),
   ETL_ID               VARCHAR2(50),
   constraint OBSERVATIONDIM_PK primary key (BIO_OBSERVATION_ID)
)
initrans 1
storage
(
    initial 576K
    minextents 1
    maxextents unlimited
)
tablespace BIOMART
nologging
monitoring
 noparallel
/


create trigger BIOMART.TRG_BIO_OBSERVATION_ID  before insert on BIOMART.BIO_OBSERVATION for each row
begin     if inserting then       if :NEW."BIO_OBSERVATION_ID" is null then          select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_OBSERVATION_ID" from dual;
       end if;
    end if;
 end;
/
