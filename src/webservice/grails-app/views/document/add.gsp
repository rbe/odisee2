<h2 style="font-variant: small-caps;">Add Template</h2>
<p>
    Upload a new OpenOffice template into Odisee Server.
    <br/>If a template with that name already exists, a new revision will be stored.
</p>
<hr style="border: 1px solid #000;"/>
<g:form action="add" method="post" enctype="multipart/form-data" target="uploadframe">
    <p>
        <input type="file" name="file"/>
    </p>
    <input type="submit" value="        Upload" style="height: 35px; background-color: #fff; border: none; background-image: url(images/odisee/upload.png); background-repeat: no-repeat; background-position: left center;" border="0"/></input>
</g:form>
