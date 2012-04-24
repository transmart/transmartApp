<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Simple PDF demo</title>
    <style>
      .code_table {
        width:80%;
        background-color:#b2b2b2;
      }
      .code_table tr {padding:2px;}
      .code_table td {
        padding:7px;
        background-color: #fff;
      }
    </style>
  </head>
    <body>
    <h1>Grails PDF Plugin demo page</h1>
    
    <h2>Tag Documentation / Live Examples / Functional Testing</h2>
    <h3>pdfLink tag:</h3>
    
    <table class="code_table">
      <thead>
        <tr>
          <th>Description</th>
          <th>Sample Source/HTML output</th>
          <th>In Action</th>
        </tr>
      </thead>
      <tbody>
        <tr><td colspan="3"><em>URL method examples:</em></td></tr>
        <tr>
          <td rowspan="2" >Simple Usage:</td>
          <td>
            <code>&lt;g:pdfLink url="/pdf/demo2"&gt;PDF View&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink url="/pdf/demo2">PDF View</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?url=%2Fpdf%2Fdemo2"&gt;<br />
            PDF View <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Simple Usage w/ Get data:</td>
          <td>
            <code>&lt;g:pdfLink url="/pdf/demo2/5?name=bob&age=22"&gt;PDF View&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink url="/pdf/demo2/5?name=bob&age=22">PDF View</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?url=%2Fpdf%2Fdemo2%2F5%3Fname%3Dbob%26age%3D22"&gt;<br />
            PDF View <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Custom filename</td>
          <td>
            <code>&lt;g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf"&gt;sample.pdf&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2" > 
            <g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf">sample.pdf</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?url=%2Fpdf%2Fdemo2.gsp&filename=sample.pdf"&gt;<br />
            sample.pdf<br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2">Bundled icon</td>
          <td>
            <code>&lt;g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf" icon="true"/&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf" icon="true"/>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?url=%2Fpdf%2Fdemo2.gsp&filename=sample.pdf"&gt; <br />
            &lt;img border="0" alt="PDF Version" src="/pdf/images/pdf_button.png" /&gt; <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2">Bundled icon w/ link content</td>
          <td>
            <code>&lt;g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf" icon="true" class="myPdfLink"&gt;Custom link&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink url="/pdf/demo2.gsp" filename="sample.pdf" icon="true" class="myPdfLink">Custom link</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="myPdfLink" title="pdf" href="/pdf/pdf/pdfLink?url=%2Fpdf%2Fdemo2.gsp&filename=sample.pdf"&gt; <br />
            &lt;img border="0" alt="PDF Version" src="/pdf/images/pdf_button.png" /&gt; <br />
            Custom link<br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr><td colspan="3"><em>String method examples:</em></td></tr>
  %{--  <tr>
          <td rowspan="2" >Simple Template Usage:</td>
          <td>
            <code>&lt;g:pdfLink template="demo2"&gt;template as PDF&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink template="demo2">template as PDF</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?template=demo2&filename=document.pdf"&gt;<br />
            PDF View <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr> --}%
        <tr>
          <td rowspan="2" >Simple Controller Action Usage (action and id are optional):</td>
          <td>
            <code>&lt;g:pdfLink pdfController="pdf" pdfAction="demo2"&gt;GSP as PDF&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink pdfController="pdf" pdfAction="demo2">GSP as PDF</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?&pdfController=pdf&pdfAction=demo2"&gt;<br />
            PDF View <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Simple Controller Action + Id Usage:</td>
          <td>
            <code>&lt;g:pdfLink pdfController="pdf" pdfAction="demo2" pdfId="65432"&gt;GSP as PDF&lt;/g:pdfLink&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfLink pdfController="pdf" pdfAction="demo2" pdfId="65432">GSP as PDF</g:pdfLink>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;a class="pdf" title="pdf" href="/pdf/pdf/pdfLink?&pdfController=pdf&pdfAction=demo2&pdfId=65432"&gt;<br />
            PDF View <br />
            &lt;/a&gt;
            </code>
          </td>
        </tr>
      </tbody>
    </table>
    
    <h3>pdfForm tag:</h3>
    <table class="code_table">
      <thead>
        <tr>
          <th>Description</th>
          <th>Sample Source/HTML output</th>
          <th>In Action</th>
        </tr>
      </thead>
      <tbody>
        <tr><td colspan="3"><em>GET method examples:</em></td></tr>
        <tr>
          <td rowspan="2" >Simple Usage:</td>
          <td>
            <code>&lt;g:pdfForm url="/pdf/demo2"&gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm url="/pdf/demo2" >
            name:<br />
            <g:textField name="name" size="10"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="get" action="/pdf/pdf/pdfForm" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="/pdf/demo2" name="url"/&gt;<br />
            &lt;input type="hidden" value="document.pdf" name="filename"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;</code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >w/ Id and Filename:</td>
          <td>
            <code>&lt;g:pdfForm url="/pdf/demo2/1968" filename="sample.pdf"&gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm url="/pdf/demo2/1968" filename="sample.pdf">
            age:<br />
            <g:textField name="age" size="3"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="get" action="/pdf/pdf/pdfForm" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="/pdf/demo2/1968" name="url"/&gt;<br />
            &lt;input type="hidden" value="sample.pdf" name="filename"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;
            </code>
          </td>
        </tr>
        <tr><td colspan="3"><em>POST method examples:</em></td></tr>
        <tr>
          <td rowspan="2" >Controller and Action:</td>
          <td>
            <code>&lt;g:pdfForm controller="pdf" action="demo3" method="post"&gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm controller="pdf" action="demo3" method="post" >
            food:<br />
            <g:textField name="food" size="10"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="post" action="/pdf/pdf/pdfForm" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="pdf" name="pdfController"/&gt;<br />
            &lt;input type="hidden" value="demo3" name="pdfAction"/&gt;<br />
            &lt;input type="hidden" value="document.pdf" name="filename"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Controller, Action, Id and Filename:</td>
          <td>
            <code>&lt;g:pdfForm controller="pdf" action="demo3" method="post"&gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm controller="pdf" action="demo3" id="1942" method="post" filename="sample.pdf" >
            food:<br />
            <g:textField name="food" size="10"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="post" action="/pdf/pdf/pdfForm" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="pdf" name="pdfController"/&gt;<br />
            &lt;input type="hidden" value="demo3" name="pdfAction"/&gt;<br />
            &lt;input type="hidden" value="document.pdf" name="filename"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Template:</td>
          <td>
            <code>&lt;g:pdfForm template="demo2" &gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm template="demo2" method="post">
            hometown:<br />
            <g:textField name="hometown" size="10"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="post" action="/pdf/pdf/pdfForm2" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="demo2" name="template"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;
            </code>
          </td>
        </tr>
        <tr>
          <td rowspan="2" >Template and Filename:</td>
          <td>
            <code>&lt;g:pdfForm template="demo2" filename="sample.pdf"&gt;...&lt;/g:pdfForm&gt;</code>
          </td>
          <td rowspan="2">
            <g:pdfForm template="demo2" filename="sample.pdf" method="post">
            hometown:<br />
            <g:textField name="hometown" size="10"/>
            <g:submitButton name="printPdf" value="pdf" />
            </g:pdfForm>
          </td>
        </tr>
        <tr>
          <td>
            <code>
            &lt;form id="simplePdfForm" method="post" action="/pdf/pdf/pdfForm2" name="simplePdfForm"&gt;<br />
            &lt;input type="hidden" value="demo2" name="template"/&gt;<br />
            &lt;input type="hidden" value="sample.pdf" name="filename"/&gt;<br />
            ...<br /> 
            &lt;/form&gt;
            </code>
          </td>
        </tr>
      </tbody>
    </table>
  </body>
</html>
