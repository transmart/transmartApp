
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
class DataAttestation {

	Long id
	Long authUserId
	Date lastDateAgreed
	
	//static belongsTo = [people: AuthUser]

	static mapping = {
		table 'DATA_ATTESTATION'
		version false
		id generator:'sequence', params:[sequence:'DATA_ATTESTATION_ID_SEQ']
		columns {
			id column:'DATA_ATTESTATION_ID'
			authUserId column:'AUTH_USER_ID'
			lastDateAgreed column:'LAST_DATE_AGREED'
		}
		
	}
	
	def beforeInsert  ={
		if(lastDateAgreed == null)	{
			lastDateAgreed = new Date()
		}
	}
	
	static constraints = {
		authUserId(nullable:false)
		lastDateAgreed(nullable:false)
	}
	
	def hasAgreed() {
		def agreed = true 
		def today = new Date()
		if (daysBetween(lastDateAgreed,today) > 90)
			agreed = false
		return agreed
	}
	
	static needsDataAttestation(user) {
		def da = DataAttestation.findByAuthUserId(user.id)
        return (da == null || !da.hasAgreed())
    }
    
    static updateOrAddNewAgreementDate(user) {
    	def da = DataAttestation.findByAuthUserId(user.id)
    	if (da == null) {
			new DataAttestation(authUserId: user.id, lastDateAgreed: new Date()).save()
    	} else {
    		da.lastDateAgreed = new Date()
    		da.save()
    	}
    }

    int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}
