<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <title>
        art of coding - Odisee Server
    </title>
    <link rel="stylesheet" href="css/odisee.css"/>
    <g:javascript library="prototype"/>
    <g:layoutHead/>
</head>
<body>
<div id="header" style="margin: 10px 10px 10px 10px;"> <!-- top right bottom left -->
    <a href="http://www.odisee.de">
        <img src="images/odisee/Odisee_Logo.png" height="50px" border="0"/>
    </a>
</div>
<div id="content">
    <iframe name="uploadframe" style="display: none;" onload="new Ajax.Updater({success: 'pagecontent', failure: 'pagecontent'}, '${createLink(controller: 'document', action: 'list')}', {asynchronous: true, evalScripts: true});return false;">
    </iframe>
    <g:layoutBody/>
</div>
</body>
</html>
