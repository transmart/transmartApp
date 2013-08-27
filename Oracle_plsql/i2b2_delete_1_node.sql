CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_DELETE_1_NODE" 
(
  path VARCHAR2
)
AS
BEGIN
  
  -------------------------------------------------------------
  -- Delete a tree node in I2b2
  -- Not handling Observation Fact. It will take too long. 
  -- KCR@20090404 - First Rev
  -------------------------------------------------------------
  if path != ''  or path != '%'
  then 
    --I2B2
    DELETE 
      FROM OBSERVATION_FACT 
    WHERE 
      concept_cd IN (SELECT C_BASECODE FROM I2B2 WHERE C_FULLNAME = PATH);
   -- COMMIT;

      --CONCEPT DIMENSION
    DELETE 
      FROM CONCEPT_DIMENSION
    WHERE 
      CONCEPT_PATH = path;
    --COMMIT;
    
      --I2B2
      DELETE
        FROM i2b2
      WHERE 
        C_FULLNAME = PATH;
    --COMMIT;

  --i2b2_secure
      DELETE
        FROM i2b2_secure
      WHERE 
        C_FULLNAME = PATH;
    --COMMIT;

  --i2b2_secure
      DELETE
        FROM concept_counts
      WHERE 
        concept_path = PATH;
    COMMIT;

  END IF;
  
END;
/

