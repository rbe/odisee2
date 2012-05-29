<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
    <meta name="layout" content="main"/>
</head>
<body>
<p>
    <g:remoteLink controller="document" action="add" update="[success: 'pagecontent', failure: 'pagecontent']">Add template</g:remoteLink>
&middot;
    <g:remoteLink controller="document" action="list" update="[success: 'pagecontent', failure: 'pagecontent']">List files</g:remoteLink>
&middot;
    <g:remoteLink controller="document" action="listTemplates" update="[success: 'pagecontent', failure: 'pagecontent']">List templates</g:remoteLink>
&middot;
    <g:remoteLink controller="document" action="listDocuments" update="[success: 'pagecontent', failure: 'pagecontent']">List documents</g:remoteLink>
</p>
<div id="pagecontent">
</div>
<div id="footer" class="footer">
    <hr style="border: 1px solid #000;"/>
    <p style="padding-left: 10px;">
        Copyright &copy; 2011-2012 <a href="http://www.art-of-coding.eu/" target="_new">art of coding UG (haftungsbeschr&auml;nkt)</a>
        <br/>Copyright &copy; 2005-2010 <a href="http://www.bensmann.com/" target="_new">Informationssysteme Ralf Bensmann</a>
        <br/>Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
        <br/>All Rights reserved. Use is subject to license terms.
        <br/>Odisee&reg; is a registered trademark of Ralf Bensmann.
    </p>
</div>
</html>
</body>
