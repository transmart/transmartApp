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

function generatePatientSampleCohort(callback)
{
    //Verify subsets are entered.
    if(areAllSubsetsEmpty()) return false;

    //Determine subset count.
    determineNumberOfSubsets();

    //Iterate through all subsets calling the ones that need to be run.
    for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
    {
        if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
        {
            runQuery(i, callback);
        }
    }

}

//Verify the user actually selected subsets.
function areAllSubsetsEmpty()
{
    if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
        return true;
    }
}

//Determine the number of subsets we need and store it in a global js variable.
function determineNumberOfSubsets()
{
    var subsetstorun = 0;

    for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
    {
        if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
        {
            subsetstorun ++ ;
        }
    }

    STATE.QueryRequestCounter = subsetstorun;
}