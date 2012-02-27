java -jar JnJFilePivot.jar pivotstart=6 columncount=1 inputfile="c:\data\input\clinical012.txt" outputfile="c:\data\output\_rbm_p.txt" nbrrows=0 skipfixedcolumns=5


pause
exit

rem required parameters
rem pivotstart - pivot start position (0 based. All columns to the left will be inserted into each output record
rem columncount - number of data columns to be pivoted
rem inputfile - input data file
rem outputfile - output data file

rem optional parameters
rem delimiter - column delimter. Default is tab (\t)
rem skipfixedcolumns - comma separated column numbers (0 based). e.g. "3,5"
rem skipdatacolumns - comma separated column numbers (0 based). e.g. "6"
rem maxcolumns - maximum number of data columns to be pivoted
rem nbrrows - number of header rows (0 based)

rem Example Usage
rem java -jar JnJFilePivot.jar pivotstart=1 columncount=1 inputfile="_gene_expression.txt" outputfile="_gene_expression_p.txt"