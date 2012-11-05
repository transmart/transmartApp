--------------------------------------------------------
--  File created - Thursday-October-25-2012   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table BIO_ASY_ANALYSIS_DATA_IDX
--------------------------------------------------------

  CREATE TABLE "BIOMART"."BIO_ASY_ANALYSIS_DATA_IDX" 
   (	"BIO_ASY_ANALYSIS_DATA_IDX_ID" NUMBER(18,0), 
	"EXT_TYPE" VARCHAR2(100 BYTE), 
	"FIELD_NAME" VARCHAR2(100 BYTE), 
	"FIELD_IDX" NUMBER(18,0), 
	"DISPLAY_NAME" VARCHAR2(100 BYTE), 
	"DISPLAY_IDX" NUMBER(38,0)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS NOLOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "TRANSMART" ;


--------------------------------------------------------
--  DDL for Trigger TRG_BIO_ASY_ANLSIS_DATA_IDX_ID
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_ASY_ANLSIS_DATA_IDX_ID" 
  before insert on "BIO_ASY_ANALYSIS_DATA_IDX"    
  for each row begin     
  if inserting then       
  if :NEW."BIO_ASY_ANALYSIS_DATA_IDX_ID" is null then          
  select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_ASY_ANALYSIS_DATA_IDX_ID" from dual;       end if;    end if; end;
/
ALTER TRIGGER "BIOMART"."TRG_BIO_ASY_ANLSIS_DATA_IDX_ID" ENABLE;
