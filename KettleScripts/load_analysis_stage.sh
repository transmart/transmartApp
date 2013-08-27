etl_id=${1}
/home/transmart/ETL/data-integration/kitchen.sh -norep=Y -file=/home/transmart/ETL/Kettle-ETL/load_analysis_from_lz_to_staging.kjb \
-param:ETL_ID=$etl_id
