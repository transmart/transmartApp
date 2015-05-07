// Creates the Utilities Menu to use on all of the five main screens
function createUtilitiesMenu(helpURL, contact, appTitle, basePath, buildVer, css)
{
	var gpl3LicenseInfo = '<h1>' + appTitle + ' License Information</h1>' +
    	'Copyright (C) 2008-2012<br/><br/>' +    	
    	'This program is free software: you can redistribute it and/or modify<br/>' +
    	'it under the terms of the GNU General Public License as published by<br/>' +
    	'the Free Software Foundation, either version 3 of the License, or<br/>' +
    	'(at your option) any later version.<br/><br/>' +

    	'This program is distributed in the hope that it will be useful,<br/>' + 
    	'but WITHOUT ANY WARRANTY; without even the implied warranty of<br/>' +
    	'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br/>' +
    	'GNU General Public License for more details.<br/><br/>' +

    	'You should have received a copy of the GNU General Public License<br/>' +
    	'along with this program.  If not, click <a href="http://www.gnu.org/licenses" target="_blank">here</a>';
    	
	var utilityMenu = new Ext.menu.Menu(
	{
		id : 'utilMenu',
	    minWidth: 250,
	    items : [
	    {
	    	text : 'Help',
	        handler : function()
	        {
	        	popupWindow(helpURL, '_help');
	        }
	    },{
	        text : 'Contact Us',                        
	        href : contact
	    },{
	        text : 'About',
	        handler : function()
	        {
	        	Ext.Msg.alert(appTitle, buildVer); 
	        }
	    },'-',{
	        text : 'Login',
	        href : basePath+'/login/forceAuth'
	    },'-',{
	        text : 'Log Out',
	        href : basePath+'/logout'
	    }]
	});
	                                  
	var utilTbar = new Ext.Toolbar(
	{                           
		id : 'utilityToolbar',
	    title : 'Utilities ToolBar',
	    cls: css,
	    items : ['->',
	             new Ext.Toolbar.Button(
	             {
	            	 id : 'utilities',
	                 text : 'Utilities',
	                 menu : utilityMenu
	             })
	    ]
	});
	return utilTbar;
}

function popupWindow(mylink, windowname)
{
	if (!window.focus)	{
		return true;
	}
	
	var href;
	if (typeof(mylink) == 'string')	
		href = mylink;
	else 
		href = mylink.href;

	var w = window.open(href, windowname, 'width=800,height=800,scrollbars=yes');
	w.focus();
	return false;
}