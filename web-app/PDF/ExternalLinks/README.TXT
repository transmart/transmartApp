
The files in this folder are located in the following location on the Cloud:

<server>/transmart/PDF/ExternalLinks

For example:

https://transmartdev.jnj.com/transmart/PDF/ExternalLinks/DataLoadContacts.txt



================================
Linking to files on the JnJ wiki
================================


These files contain sample scripts and other info that may change and thus are easier to manage in a separate text file rather than in the PDF - for example, names of JnJ and Recom contacts, sql scripts, etc.

Other files are included, such as very large image files and GenePattern PDFs.  Generally, any file that is linked from a PDF or Help topic whose source is a Word Guide or Reference is located here.


The links can only be tested on the Cloud.  You will get errors when you generate the Help, saying that the target files can't be found.


====================
Setting Up the Links
====================

Set up a hyperlink in Word and copy/paste the following root path, then type the file name:


ROOT:

/transmart/PDF/ExternalLinks


EXAMPLE:

/transmart/PDF/ExternalLinks/Jub_Jwb_S2T.xlsx

This will link to:
<server>/transmart/PDF/ExternalLinks/Jub_Jwb_S2T.xlsx


NOTE: You can use the same root to link from either a PDF or Help topic.
