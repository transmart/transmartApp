<html>
<head>
    <title>${grailsApplication.config.com.recomdata.appTitle} - ${title}</title>
    <style type="text/css">
    .message {
        border: 1px solid black;
        padding: 5px;
        background-color: #E9E9E9;
    }

    .stack {
        border: 1px solid black;
        padding: 5px;
        overflow: auto;
        height: 300px;
    }

    .snippet {
        padding: 5px;
        background-color: white;
        border: 1px solid black;
        margin: 3px;
        font-family: courier;
    }
    </style>
</head>

<body>
<h1>${title}</h1>

<h2>Error Details</h2>

<div class="message">
    <strong>Message:</strong> ${message}
</div>
</body>
</html>
