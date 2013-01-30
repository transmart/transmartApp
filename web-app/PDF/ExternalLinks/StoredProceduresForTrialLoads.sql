DECLARE
   studyID VARCHAR2(100);
   studyType VARCHAR2(100);
 
BEGIN
   --Set the Trial Number and the Root node the trial will be under in the Dataset Explorer
   studyID := 'C0168T54';
   studyType := 'Clinical Trials';
 
   --Takes files from the staging table, backs them up and moves them into the Work Zone.
   I2B2_PROCESS_RAW_DATA_EXTRNL(studyID);
 
END;


--------------------------------------------------------------------------------

Load the i2b2_lz.patient_info and i2b2_wz.patient_info tables with patient information. The data_labels for Age, Sex, Race, and 
Treatment Group/Study Group will vary by trial or study. Modify the sql below to match the trial/study data. There must always 
be a non-null value for Age.

delete from i2b2_lz.patient_info
where study_id = 'C0168T54';
commit;

insert into i2b2_lz.patient_info
(
STUDY_ID,
SITE_ID,
SUBJECT_ID,
SITE_SUBJ,
AGE_IN_YEARS_NUM,
SEX_CD,
RACE_CD,
TREATMENT_GROUP,
USUBJID
)
select 
a.STUDY_ID,
a.SITE_ID,
a.SUBJECT_ID,
a.SITE_SUBJ,
nvl(max(decode(a.data_label,'Age (AGE)',data_value,null)),0) as age,
max(decode(a.data_label,'Sex (SEX)',data_value,null)) as sex,
max(decode(a.data_label,'Race (RACE)',data_value,null)) as race,
max(decode(a.data_label,'Treatment Groups',data_value,'Study Groups',data_value,null)) as trtgp,
a.USUBJID
from time_point_measurement a
where a.data_label in ('Age (AGE)'
,'Race (RACE)'
,'Sex (SEX)'
,'Treatment Groups'
,'Study Groups'
)
group by 
a.STUDY_ID,
a.SITE_ID,
a.SUBJECT_ID,
a.SITE_SUBJ,
a.USUBJID;

truncate table i2b2_wz.patient_info;

insert into i2b2_wz.patient_info
select * from i2b2_lz.patient_info
where study_id = 'C0168T54'
;
commit;


--------------------------------------------------------------------------------

DECLARE
   studyID VARCHAR2(100);
   studyType VARCHAR2(100);
 
BEGIN
   --Set the Trial Number and the Root node the trial will be under in the Dataset Explorer
   studyID := 'C0168T54';
   studyType := 'Clinical Trials';
 
   --Backs up the existing I2B2 tables
   --I2B2, Concept_dimension, observation_fact, patient_dimension, concept_counts
   I2B2_TABLE_BKP;
 
   --Delete the existing tree and any data under it
   I2B2_DELETE_ALL_NODES('\' || studyType || '\' || studyID || '\');
 
   --Create the root node
   --PARAMS: Trial ID, Full Path, Path Name
   I2B2_ADD_NODE(studyID, '\' || studyType || '\' || studyID || '\', studyID);
 
   --Load the patients into the patient_dimension table from the patient_info table
   I2B2_CREATE_PATIENT_DIM(studyID);
 
   --Load the Patients into the patient_trial table
   --Used by the UI to determine what patients go with what trial
   --studyType is used to identify Public Studies which have special processing
   I2B2_CREATE_PATIENT_TRIAL(studyID, studyType);
 
   --Perform any curation on time point measurement data and load lowest level nodes into i2b2 tables.
   I2B2_APPLY_CURATION_RULES(studyID);
 
   --Load all time point measurement data into the i2b2 tables.
   I2B2_LOAD_TPM_POST_CURATION(studyID);
 
   --Fill in tree. This builds all the sub paths for a trial
   --If a path was created from i2b2_load_tpm such as \Clinical Trials\T32\Clinical Data\Other Measurements\Week 1\Blood Pressure{color}
   --This script will ensure that all the sub paths exist: \Clinical Trials\T32\Clinical Data\, \Clinical Trials\T32\Clinical Data\Other Measurements\etc...
   I2B2_FILL_IN_TREE(studyID,'\' || studyType || '\' || studyID || '\');
 
   --Load the concept_counts table used by the ui to display node counts
   --This also will hide any node with a zero count
   I2B2_CREATE_CONCEPT_COUNTS('\' || studyType || '\' || studyID || '\');
 
   --Loads a record into the Observation Fact table for each Patient in the trial
   --used for security
   --studyType is used to identify Public Studies which have special processing
   I2B2_CREATE_SECURITY_FOR_TRIAL(studyID, studyType);
 
   --Loads a record for each tree node into the I2B2METADATA.i2b2_secure table
   --rebuilds table for the entire I2B2 instance.
   I2B2_LOAD_SECURITY_DATA;
 
   --Copy curated data to time_point_measure_curated, category and patient_info in i2b2_wz
   I2B2_COPY_WZ_TO_CURATED(studyID);
 

    --Moves the 5 basic i2b2 tables, which in oracle will defragment them
   --Due to all the Inserts and deletes performance degrades very quickly
   --TABLES: I2B2, Concept_dimension, observation_fact, patient_dimension, concept_counts
   I2B2_TABLE_DEFRAG;
 
END;

  ---------------------------------------------------
  --OTHER HELPER PROCEDURES
  ---------------------------------------------------
 
   --Hide a node in the i2b2 tree
   --Rather than delete a node this will make it as Hidden
   --Updates i2b2.c_visual_attributes to FH or LH. (Folder Hidden or Leaf Hidden)
   I2B2_HIDE_NODE('\Clinical Trials\C0168T30\Clinical Data\Other\Deodorant Usage\');
 
   --Moves a tree node and all it's children under a new path
   --This should NOT be used to rename a node as it does not update the i2b2.c_name field
   --Updates i2b2.c_visual_attributes to FH or LH. (Folder Hidden or Leaf Hidden)
   I2B2_MOVE_NODE('\Clinical Trials\C0168T30\Week', '\Clinical Trials\C0168T30\Clinical Data\Week');
 
   --Rename a node
   --searches the tree for any node with this name and replaces it. (For a specified trial)
   I2B2_RENAME_NODE('C0168T30', 'WEEK 6', 'Week 06');
 
   --Shows a node
   --Updates i2b2.c_visual_attributes to FA or LA depending on which type it was previously
   I2B2_SHOW_NODE('\Clinical Trials\C0168T30\Clinical Data\Other\Deodorant Usage\');
 
   --Delete 1 Node
   --Does not delete any of it's children
   I2B2_DELETE_1_NODE('\Clinical Trials\C0168T30\Clinical Data\Other\Deodorant Usage\')

 
   --Backout a trial - USE WITH CAUTION!!!!!!!!!
   --Deletes all i2b2-related and patient information
   I2B2_BACKOUT_TRIAL('C0168T30','\Clinical Trials\C0168T30\')

