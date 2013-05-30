BEGIN
	i2b2_table_bkp;
END;

	--	apply curation rules and generate lowest-level i2b2 and concept_dimension paths

declare 
	studyID VARCHAR2(100);
	studyType VARCHAR2(100);
	returnCode	int;

BEGIN
	studyID := 'C-2006-009';
	studyType := 'Clinical Trials';	--	this can be Clinical Trials, Experimental Medicine Study, Internal Studies or Public Studies depending on the type of trial

	returnCode := 0;
  
	i2b2_delete_all_nodes('\' || studyType || '\' || studyID || '\');
	i2b2_add_node(studyID, '\' || studyType || '\' || studyID || '\', studyID);
  
    i2b2_create_patient_dim(studyID,null,returnCode);   				--   ONLY LOAD PATIENTS ON FIRST LOAD OR IF YOU RECEIVE UPDATED PATIENT INFO
	
    if returnCode = 0 then
       i2b2_create_patient_trial(studyID,studyType,null,returnCode);	--   ONLY LOAD PATIENTS ON FIRST LOAD OR IF YOU RECEIVE UPDATED PATIENT INFO
    end if;
  
	if returnCode = 0 then
		i2b2_apply_curation_rules(studyID,null,returnCode);
	end if;
  
	if returnCode = 0 then
		i2b2_load_tpm_post_curation(studyID,null,returnCode);
	end if;

	if returnCode = 0 then
		i2b2_fill_in_tree(studyID,'\' || studyType || '\' || studyID || '\');
		i2b2_create_concept_counts('\' || studyType || '\' || studyID || '\');
		I2B2_CREATE_SECURITY_FOR_TRIAL(studyID, studyType);
		i2b2_load_security_data;
		i2b2_table_defrag;
	end if;

end;
  
  
--	there may be additional changes (hide node, etc) after the curator reviews the data in Dataset Explorer
--	annotate those changes here.  If data_labels, data_values, visit_names or periods need to be changed, add the changes to node_curation and reload the trial

--	when the curator has approved the trial in Dataset Explorer, move the curated data from i2b2_wz into i2b2_lz.time_point_measure_curated, i2b2_lz.category, and i2b2_lz.patient_info

declare 
BEGIN
  i2b2_copy_wz_to_curated('C-2006-009');
end;

--	if you need to reload trial at a later date, copy curated data to wz

declare 
  studyID VARCHAR2(100);

BEGIN
  studyID := 'C-2006-004';
  i2b2_copy_curated_to_wz(studyID);
end;


