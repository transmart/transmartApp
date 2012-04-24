<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Simple PDF demo2</title>
    <style>
      .sample_table {
        width:60%;
        background-color:#b2b2b2;
      }
      .sample_table th {
        text-align:center;
        text-decoration:underline;
      }
      .sample_table td {
        padding:7px;
        background-color: #fff;
      }
      .small_text {
        font-size:8pt;
      }
    </style>
  </head>
  <body>
    <h1>Sample PDF Output</h1>
    <h2>This is simple HTML</h2>
    <h2 style='color:red'>this has inline CSS</h2>
    
    <h3>Here is some data passed from the contorller...</h3>
    <p>Information gathered from the controller: ${randomString}</p>
    
    <h3>Here is some information sent in the URL and handled by a controller (get variables):</h3>
    <table class="sample_table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Age</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>${id}</td>
          <td>${name}</td>
          <td>${age}</td>
        </tr>
        <tr>
          <td colspan="3" class="small_text">styled with css...</td>
        </tr>
      </tbody>
    </table> 
    
    <h3>Here is some information gathered from a form and handled by a controller (post variables):</h3>
    <p>Favorite food: ${pdf?.food}</p>
    <p>Favorite food: ${pdf?.food.toString()}</p>
    <p>Hometown: ${pdf?.hometown}</p> 
    
    <p>${pdf}</p>
    
    <p>Images with relative URL's are automatically resolved by the the modified version of XHTMLrenderer included with the plugin</p>
    <img src="<g:resource dir='images' file='laptop.jpg' />" alt="laptop" title="laptop" />
    
    <p>Images with absolute URI's also do just fine:</p>
    <img src="http://system76.com/images/nb1_front_med.jpg" alt="laptop2" title="laptop2" />
    
    <p><strong>Hint:</strong> One way to style gsp's that you intend to make into pdf's is to have two seperate style sheets one for media="print" and one for media="screen". The print style sheet will be used to style the PDF, and if PDF generation fails you will get a styled HTML view that isn't all weird because of fonts sized in pt and such.</p>
    
    <p>Varialbles passed into gsp that the plugin will render as PDF need to start with pdf. so for example the form field name was hometown and to reference that varialbe here we needed {pdf.hometown}</p>
    <code>
      ${content}
    </code>
  </body>
</html>
