
--TASK: Unhide the NORMALS trial and move to the Experiment Node
--Unhide the nodes on the i2b2 table

select c_visualattributes, count
from i2b2
where c_fullname like '%Across%Normal%'
group by c_visualattributes;

update i2b2
set c_visualattributes = 'LA'
where c_visualattributes = 'LH'
and c_fullname like '%Across%Normal%';

update i2b2
set c_visualattributes = 'FA'
where c_visualattributes = 'FH'
and c_fullname like '%Across%Normal%';
commit;

--Check results
select c_visualattributes, count
from i2b2
 group by c_visualattributes;
--LA=648
--FA=2

--change the root node from Across Trials to Experiment
--get paths
select * from i2b2
where c_hlevel < 2
order by c_hlevel
--\Experimental Medicine Study\
--\Across Trials\
--\Across Trials\Normals\

--Fields to update:
--i2b2: c_fullname, c_dimcode,c_tooltip
--concept_dimension: concept_path
update i2b2
set c_fullname = replace(c_fullname, '\Across Trials\', '\Experimental Medicine Study\')
where c_fullname like '\Across Trials\Normals%';

update i2b2
set c_dimcode = c_fullname,
    c_tooltip = c_fullname
where c_fullname like '\Experimental Medicine Study\Normals%';
COMMIT;

update concept_dimension
set concept_path = replace(concept_path, '\Across Trials\', '\Experimental Medicine Study\')
where concept_path like '\Across Trials\Normals%';
COMMIT

--Verify that records exist on the patient Trial table
select * from patient_trial
where trial like '%orma%'

--They don't so add them:
insert into patient_trial
select distinct patient_num
  from observation_fact
  where concept_cd IN
  (
    select distinct concept_cd from concept_dimension
    where concept_path like '\Experimental Medicine Study\Normals%'
  );
Commit;

--check that the sourcesystem_cd field has a value of Normal on the Patient_Dimension table for each record.
--This is used to load security.
select distinct sourcesystem_cd
  from patient_num
  where sourcesystem_cd like '%orm%'
order by sourcesystem_cd

--They don't so update the records:
update patient_dimension
  set sourcesystem_cd = 'Normals'
  where patient_num IN
  (
    select distinct patient_num
    from observation_fact
    where concept_cd IN
    (
      select distinct concept_cd from concept_dimension
      where concept_path like '\Experimental Medicine Study\Normals%'
    )
  );
COMMIT;

--Load Concept Counts and Security
BEGIN
  i2b2_create_Concept_counts('\Experimental Medicine Study\Normals\');
  i2b2_load_security_data;
  i2b2_create_security_for_trial('Normals');
END;


