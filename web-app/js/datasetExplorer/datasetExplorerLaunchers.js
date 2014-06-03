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

//This function is used to redirect the user to the sample explorer page with search results loaded for the selected cohort.
function launchSampleBrowseWithCohort()
{
    //This function gets called from the DSE when we've already generated the cohort. We need to generate the list of samples and continue on to the sample explorer page.
    jQuery.ajax({
        async: false,
        url: pageInfo.basePath+'/sampleExplorer/generateSampleCohort?result_instance_id='+GLOBAL.CurrentSubsetIDs[1],
        success: function() {window.location=pageInfo.basePath+'/sampleExplorer/showCohortSamples?result_instance_id='+GLOBAL.CurrentSubsetIDs[1]},
        failure: function() {alert("Failed to generate Patient Sample set. Please contact an administrator.")}
    });


}