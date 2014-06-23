package com.recomdata.transmart.data.export

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

class I2b2ExportHelperService {

    static transactional = false
	def dataSource;


    def findStudyAccessions(result_instance_ids) {
        checkQueryResultAccess(*(result_instance_ids as List))

		def rids = []
		for(r in result_instance_ids){
			if(r?.trim()?.length()>0 ){
				rids.add('CAST('+ r + ' AS numeric)');
			}
		}
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			StringBuilder sqltb=new StringBuilder("select DISTINCT b.TRIAL FROM i2b2demodata.QT_PATIENT_SET_COLLECTION a ").
			append("INNER JOIN i2b2demodata.PATIENT_TRIAL b").
		    	append(" ON a.PATIENT_NUM=b.PATIENT_NUM WHERE RESULT_INSTANCE_ID IN(").
				append(rids.join(", ")).append(")");
			def trials =[]
			sql.eachRow(sqltb.toString(), 
				{row ->
					trials.add(row.TRIAL);
					}
				)
		return trials 
    }
}
