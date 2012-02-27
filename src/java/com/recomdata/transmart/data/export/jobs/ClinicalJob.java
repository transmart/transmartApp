/*************************************************************************
  * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
/**
 * 
 */
package com.recomdata.transmart.data.export.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.recomdata.transmart.data.export.dao.ClinicalDao;
import com.recomdata.transmart.data.export.dao.impl.ClinicalDaoImpl;
import com.recomdata.transmart.data.export.util.FileWriterUtil;

/**
 * @author SMunikuntla
 *
 */
public class ClinicalJob extends JobResultStore implements Job {

	
	/**
	 * 
	 */
	public ClinicalJob() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		ClinicalDao clinicalDao = new ClinicalDaoImpl();
		clinicalDao.getClinicalData();
		
		//FileWriterUtil fileWriterUtil = new FileWriterUtil(null, null);
		
		//setGeneratedFile(fileWriterUtil.write(null));
	}

}
