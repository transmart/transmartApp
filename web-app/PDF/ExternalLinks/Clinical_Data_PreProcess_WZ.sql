--	after the data has been loaded into the WZ tables

--	check for single visit_name

select distinct tpm.category_cd
,tpm.visit_name
from time_point_measurement tpm
where (tpm.category_cd) in
     (select x.category_cd
      from time_point_measurement x
      group by x.category_cd
      having count(distinct upper(x.visit_name)) = 1)
;
	  
update time_point_measurement tpm
set visit_name=null
where (tpm.category_cd) in
     (select x.category_cd
      from time_point_measurement x
      group by x.category_cd
      having count(distinct upper(x.visit_name)) = 1);
commit;
	  
--	check for single sample_type

select distinct tpm.category_cd
,tpm.sample_type
from time_point_measurement tpm
where (tpm.category_cd) in
     (select x.category_cd
      from time_point_measurement x
      group by x.category_cd
      having count(distinct upper(x.sample_type)) = 1);
	  
update time_point_measurement tpm
set sample_type=null
where tpm.category_cd in
     (select x.category_cd
      from time_point_measurement x
      group by x.category_cd
      having count(distinct upper(x.sample_type)) = 1);

--	check if visit_name same as data_label, remove visit_name if yes
	  
select distinct tpm.category_cd
,tpm.visit_name
,tpm.data_label
from time_point_measurement tpm
where tpm.visit_name = tpm.data_label
;

--	check if visit_name same as data_value, remove visit_name if yes
	  
select distinct tpm.category_cd
,tpm.visit_name
,tpm.data_value
from time_point_measurement tpm
where tpm.visit_name = tpm.data_value
;

--	sample remove visit_name (duplicated in data_value)

update time_point_measurement
set visit_name=null
where category_cd = 'Scheduled_Visits'
and data_label = 'Scheduled Visits'
;
commit;

--	Check for multiple visit_names where category_cd contains _WEEK_  There should be either no visit_name or only one visit_name   The data needs to be 
--	corrected by the curator and the process restarted from the beginning

select distinct t.category_cd
,t.visit_name
from time_point_measurement t
where t.category_cd in
     (select x.category_cd
      from time_point_measurement x
      where upper(x.category_cd) like '%_WEEK_%'
      group by x.category_cd
      having count(distinct x.visit_name) > 1
     )
;

--	Check for both null and non-null visit_name for one category_cd and data_label.  There should not be both null and non-null.

select distinct t.category_cd
,t.data_label
,t.visit_name
from time_point_measurement t
where exists
     (select 1 from time_point_measurement n
      where t.category_cd = n.category_cd
        and t.data_label = n.data_label
        and n.visit_name is not null)
and exists
   (select 1 from time_point_measurement x
    where t.category_cd = x.category_cd
      and t.data_label = x.data_label
      and x.visit_name is null)
order by t.category_cd
,t.data_label
,t.visit_name desc;

--	check with curator, if ok, delete the records with null visit_names

delete from time_point_measurement s
where s.visit_name is null
and (s.category_cd, s.data_label) in
(select distinct t.category_cd
,t.data_label
--,t.visit_name
from time_point_measurement t
where exists
     (select 1 from time_point_measurement n
      where t.category_cd = n.category_cd
        and t.data_label = n.data_label
        and n.visit_name is not null)
and exists
   (select 1 from time_point_measurement x
    where t.category_cd = x.category_cd
      and t.data_label = x.data_label
      and x.visit_name is null)
);

--	check what the visit_names are and make sure they're in node_curation if needed

select distinct t.visit_name
,coalesce(s.display_name,g.display_name)
,coalesce(s.global_flag,g.global_flag)
from time_point_measurement t
left outer join node_curation g
     on  upper(t.visit_name) = g.node_name
     and g.node_type = 'VISIT_NAME'
     and g.global_flag = 'Y'
left outer join node_curation s
     on  upper(t.visit_name) = s.node_name
     and s.node_type = 'VISIT_NAME'
     and s.global_flag = 'N'
     and s.study_id = 'C-2006-009'
where t.visit_name is not null
order by t.visit_name
;

insert into node_curation
(node_type
,node_name
,display_name
,display_in_ui
,data_type
,global_flag
,study_id
,curator_name
,curation_date
,active_flag
)
values (''	--	node_types are VISIT_NAME, DATA_LABEL, DATA_VALUE, PERIOD
,''    		--	this must be the value in UPPERCASE!!
,''			--	this is the value that should appear in Dataset Explorer
,''			--	usually Y but N if the records associated with the node_type and node_name should not appear in Dataset Explorer
,''			--	usually T for text
,'N'		--	this is a local curation record, remember to check the global (Y) records to see if there is already a global curation that matches the node_type and node_name
,''			--	enter the study_id/trial_id for the trial.  This allows curations to be trial-specific
,''			--	the ETL analyst's initials
,sysdate
,'Y'
)

--	check for sample_type names, typical issue is inconsistent capitalization (Whole Blood vs WHOLE BLOOD), add to node_curation if needed

select distinct t.sample_type
,coalesce(s.display_name,g.display_name)
,coalesce(s.global_flag,g.global_flag)
from time_point_measurement t
left outer join node_curation g
     on  upper(t.sample_type) = g.node_name
     and g.node_type = 'SAMPLE_TYPE'
     and g.global_flag = 'Y'
left outer join node_curation s
     on  upper(t.sample_type) = s.node_name
     and s.node_type = 'SAMPLE_TYPE'
     and s.global_flag = 'N'
     and s.study_id = 'C-2006-009'
where t.sample_type is not null
order by t.sample_type
;
	  
--	delete Unscheduled visits, the visit_name is usually 'Unscheduled' but it could be Unsched, etc   check!

delete time_point_measurement
where visit_name = 'Unscheduled';



--	check SCHEDULED_VISITS,  check for data_label, check data_vales and put on node_curation if needed, check that data_label is 'Scheduled Visits'

select distinct tpm.category_cd, tpm.visit_name, tpm.data_label, tpm.data_value, cd.category_path
from time_point_measurement tpm
,category cd
where upper(tpm.category_cd) like 'SCHED%'
and tpm.category_cd = cd.category_cd;


select distinct data_label
from time_point_measurement
where upper(category_cd) = 'SCHEDULED_VISITS';

--	check for data_labels in demographics and add to node_curation if needed, also check age/race/sex/treatment groups for patient info isert

select distinct category_cd
,data_label
from time_point_measurement
where upper(category_cd) like 'SUBJECTS%DEMO%'
order by category_cd;


--	check Treatment_Groups, add to node_curation if needed

select distinct category_cd, data_label
from time_point_measurement
where upper(category_cd) like 'TREAT%';

--	check for any data labels that end in a % sign   the % needs to be changed to Pct in node_curation

select distinct data_label from time_point_measurement
where substr(data_label,length(data_label),1) = '%';
  
--	check for any data_labels that have a % sign that will match to a different data_label, add data_label to node_curation (DATA_LABEL) and replace % with PCT in node_name

select distinct dl1.category_cd
,dl1.data_label
,count(distinct dl2.data_label)
from time_point_measurement dl1
,time_point_measurement dl2
where substr(dl1.data_label,length(dl1.data_label),1) = '%'
and dl1.category_cd = dl2.category_cd
and dl2.data_label like dl1.data_label
group by dl1.category_cd
        ,dl1.data_label
having count(distinct dl2.data_label) > 1;
;

--	check for PERIOD, add to node_curation if needed, case is the usual issue

select distinct t.period
,coalesce(s.display_name,g.display_name)
,coalesce(s.global_flag,g.global_flag)
from time_point_measurement t
left outer join node_curation g
     on  upper(t.period) = g.node_name
     and g.node_type = 'PERIOD'
     and g.global_flag = 'Y'
left outer join node_curation s
     on  upper(t.period) = s.node_name
     and s.node_type = 'PERIOD'
     and s.global_flag = 'N'
     and s.study_id = 'C-2006-009'
where t.period is not null
order by t.period
;


--	check data_value for curation, <>, etc

select distinct data_value
from time_point_measurement
order by data_value desc


--	check to see if any text repeated or doesn't look like it belongs

select distinct tpm.category_cd
,cd.category_path
,tpm.data_label
from time_point_measurement tpm
    ,category cd
where tpm.category_cd = cd.category_cd
order by tpm.category_cd
,tpm.data_label

--	check for dups on key (category_cd, data_label, period, sample_type, visit_name, and usubjid) values.  There should only be a single record for each combination except in the Samples & Timepoints and Scheduled Visits categories

  select 
    category_Cd, 
    data_label,  
    period,
    sample_type,
    visit_name,
    usubjid
from time_point_measurement
where upper(category_cd) not like 'SAMPLES%'
  and upper(category_cd) != 'SCHEDULED_VISITS'
group by
    category_Cd, 
    data_label,  
    period,
    sample_type,
    visit_name,
    usubjid
 having count(*) > 1
 order by category_cd
 ,data_label

 --	this code will list the specific records that have duplicate key values
 
select tpm.* from time_point_measurement tpm
where (tpm.category_cd
      ,tpm.data_label
      ,nvl(tpm.period,'**NULL**')
      ,nvl(tpm.sample_type,'**NULL**')
      ,nvl(tpm.visit_name,'**NULL**')
      ,tpm.usubjid
      ) in 
 ( select 
    category_Cd, 
    data_label,  
    nvl(period,'**NULL**'),
    nvl(sample_type,'**NULL**'),
    nvl(visit_name,'**NULL**'),
    usubjid
from time_point_measurement
where upper(category_cd) not like 'SAMPLES%'
  and upper(category_cd) != 'SCHEDULED_VISITS'
group by
    category_Cd, 
    data_label,  
    period,
    sample_type,
    visit_name,
    usubjid
 having count(*) > 1)

 --	load dups to tmp table
 
 truncate table tmp_dups;
 
 insert into tmp_dups
 (category_cd
 ,data_label
 ,period
 ,sample_type
 ,visit_name
 ,usubjid
 )
   select 
    category_Cd, 
    data_label,  
    period,
    sample_type,
    visit_name,
    usubjid
from time_point_measurement
where upper(category_cd) not like 'SAMPLES%'
  and upper(category_cd) != 'SCHEDULED_VISITS'
group by
    category_Cd, 
    data_label,  
    period,
    sample_type,
    visit_name,
    usubjid
 having count(*) > 1
 ;
 commit;
 
delete from time_point_measurement t
where exists
      (select 1 from tmp_dups d
       where t.category_cd = d.category_cd
         and t.usubjid = d.usubjid
         and t.data_label = d.data_label
         and nvl(t.visit_name,'**NULL**') = nvl(d.visit_name,'**NULL**')
         and nvl(t.sample_type,'**NULL**') = nvl(d.sample_type,'**NULL**')
         and nvl(t.period,'**NULL**') = nvl(d.period,'**NULL**')
      )
;
commit;

--	add "special" curation because treatment_group data value needs to be curated before the insert into patient_info, this is only necessary if a node_curation record has been added for Treatment Groups

  update time_point_measurement a
    set a.data_value = 
      (select replace(Upper(a.data_value), b.node_name, b.display_name)
        from node_curation b
      where b.node_type = 'DATA_VALUE'
        and upper(a.data_value) = b.node_name  
        and display_in_ui = 'Y'
        and active_flag = 'Y'
        and (global_flag = 'Y' OR b.study_id = a.study_id)
      )
    where exists
    (select 1 
      from node_curation b 
      where b.node_type = 'DATA_VALUE'
        and upper(a.data_value) = b.node_name  
        and active_flag = 'Y'
        and display_in_ui = 'Y'
        and (global_flag = 'Y' OR b.study_id = a.study_id)
	)
		;
		commit;
  
--	insert into patient_info tables (i2b2_lz first and then i2b2_wz).  The delete is only needed if the patient_info data for the trial has already been loaded into i2b2_lz.patient_info

	delete from i2b2_lz.patient_info
	where study_id = (select distinct study_id from time_point_measurement);
	commit;
	
--	for any trial, one or more of the patient demographic data points may be missing.  You may have to comment out one or more of the join selects to fit the data.
--	Also, the application REQUIRES that age not be null.  If there is no Age demographic data, then set age to 0
--	for each of the selects, the value that the data label is being compared to must match the data_labels that were listed when the category_cd 'SUBJECTS+DEMO%' and 'TREAT%' where queried

  INSERT
  INTO I2B2_LZ.PATIENT_INFO
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
    nvl(max(decode(a.data_label,'Age',data_value,null)),0) as age,
    max(decode(a.data_label,'Sex',data_value,null)) as sex,
    max(decode(a.data_label,'Race',data_value,null)) as race,
    max(decode(a.data_label,'Treatment Groups',data_value,'Study Groups',data_value,null)) as trtgp,
    a.USUBJID
  from time_point_measurement a
 -- where a.study_id = 'C0168T48'
  where a.data_label in ('Age'
                    ,'Race'
                    ,'Sex'
                    ,'Treatment Groups'
					,'Study Groups'
                    )
 group by 
    a.STUDY_ID,
    a.SITE_ID,
    a.SUBJECT_ID,
    a.SITE_SUBJ,
    a.USUBJID;
     
 	commit;
	
	truncate table i2b2_wz.patient_info;
	
	insert into i2b2_wz.patient_info
	select * from i2b2_lz.patient_info
	where study_id = 'GSE6956'
	;
	commit;

	
--	check reasonableness of path,  \\'s can be ignored, they'll be removed when path is actually created

select distinct '\' || tpm.study_id || '\' || cc.category_path || '\' ||
tpm.visit_name || '\' || tpm.period || '\' || tpm.sample_type || '\' ||
tpm.data_label
from time_point_measurement tpm
,category cc
where upper(tpm.category_cd) like 'SAMPLES_AND_TIMEPOINTS%'
--and instr(cc.category_path,tpm.data_label) > 0
and tpm.category_cd = cc.category_cd

--	JEA@20091019	Added check for % at end of data_label.  If found, the data label value should be added to node_curation and the % changed to Pct
--	JEA@20091021	Added check for multiple visit_names where category_cd contains _WEEK_




