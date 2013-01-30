java -jar GPLInfo.jar gplid=GPL80 inputfile="GPL80.txt" outputfile="GPL80_p.txt" probecol=0 idcol=11 symbolcol=10 skiprows=17

pause
exit

rem		* required parameters 
rem		* 1)gplid			GPL number
rem		* 2)inputfile		input data file 
rem		* 3)outputfile		output data file
rem		* 4)probecol		column number of probe/id (0-based)
rem		* 5)idcol			column number of entrez gene id (0-based)
rem		* 6)symbolcol		column number of gene symbol (0-based)
rem		* 7)skiprows		number of header rows to skip
rem		* 
rem		* optional parameters
rem		* 1)delimiter		column delimiter . Default is tab (\t)
rem		* 2)genetable		Y if gene symbol embedded in table,
rem		* 				 	symbolcol is the column that contains the table
rem		* 3)genepos		position (0-based) of gene symbol in genetable
rem		* 4)idpos			position (0-based) of gene id in genetable	
rem		* 5)genedelim		gene table delimiter

rem Usage Examples

rem	Affymetrix platform
rem java -jar GPLInfo.jar gplid=GPL91 inputfile="c:\data\GPL91.txt" outputfile="c:\data\GPL91_p.txt" probecol=0 idcol=11 symbolcol=10 skiprows=17

rem	Aglient platform full table download from GEO
rem java -jar GPLInfo.jar gplid=GPL1708 inputfile="c:\data\GPL1708.txt" outputfile="c:\data\GPL1708_p.txt" probecol=0 idcol=8 symbolcol=9 skiprows=21

rem Agilent platform soft format
rem java -jar GPLInfo.jar gplid=GPL5981 inputfile="GPL5981.txt" outputfile="GPL5981_p.txt" probecol=0 idcol=1 symbolcol=2 skiprows=116

rem Illumina platform
rem java -jar GPLInfo.jar gplid=GPL8432 inputfile="c:\data\GPL8432.txt" outputfile="c:\data\GPL8432_p.txt" probecol=0 idcol=9 symbolcol=5 skiprows=31

rem Affy platform with embedded gene tables
rem java -jar GPLInfo.jar gplid=GPL6244 inputfile="GPL6244.txt" outputfile="GPL6244_p.txt" probecol=0 symbolcol=9 skiprows=13 genepos=1 idpos=4 idcol=10 genetable=Y