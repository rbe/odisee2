<h2 style="font-variant: small-caps;">List Documents</h2>
<p>
    ${documents?.size()} document(s) of total: ${totalDocuments}
</p>
<hr style="border: 1px solid #000;"/>
<table border="0" style="width: 100%;">
    <thead>
    <th align="center">ID</th>
    <th align="center">Action</th>
    <th align="left">Name</th>
    <th align="left">Date</th>
    <th align="left">Type</th>
    <th align="center">Revision</th>
    <th align="left">MimeType</td>
    <th align="right">Size</td>
    </thead>
    <g:each in="${documents}" var="item">
        <tr class="normal" onmouseover="this.className = 'highlight'" onmouseout="this.className = 'normal'">
            <td align="center">${item.id}</td>
            <td align="center">
                <odisee:stream id="${item.id}"><img src="images/odisee/download.png" border="0"/></odisee:stream>
                <odisee:remove id="${item.id}"><img src="images/odisee/remove.png" border="0"/></odisee:remove>
            </td>
            <td align="left">${item.name}</td>
            <td align="left">${item.lastUpdated.format("dd.MM.yyyy HH:mm:ss") ?: item.dateCreated.format("dd.MM.yyyy HH:mm:ss")}</td>
            <td>${item.template ? "Template" : "Document"}</td>
            <td align="center">
                <g:if test="${item.template == true}">
                    ${item.revision}
                </g:if>
                <g:else>
                    ${item.instanceOfName}/${item.instanceOfRevision}
                </g:else>
            </td>
            <td align="left">${item.mimeType.name}</td>
            <td align="right">${Math.round(item.data?.length() / 1024)} kB</td>
        </tr>
    </g:each>
</table>
