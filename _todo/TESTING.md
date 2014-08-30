# Testing Odisee(R) Server

## Odisee Tester

There are several Odisee clients available, for several programming languages.

### Windows

#### Visual Basic .NET

Download of ClickOnce installer is [here](http://files.odisee.de/product/odisee-client/windows/vbnet/).
The source code is publicly available: https://bitbucket.org/odisee/client-vbnet

## odipost

Post a request, catching the output in `HalloOdisee.odt`:

    $ odipost -f test_HalloOdisee_single_odt.xml -o HalloOdisee.odt

Result:

    * About to connect() to 127.0.0.1 port 8080 (#0)
    *   Trying 127.0.0.1...
    * connected
    * Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
    * Server auth using Basic with user 'odisee'
    > POST /odisee/document/generate/1 HTTP/1.1
    > Authorization: Basic b2Rpc2VlOm9kaXNlZQ==
    > User-Agent: curl/7.27.0
    > Host: 127.0.0.1:8080
    > Accept: */*
    > Content-Type: text/xml
    > Content-Length: 456
    > 
    } [data not shown]
    * upload completely sent off: 456 out of 456 bytes
    Oct 17 15:42:40 Ralfs-iMac.local soffice[90080] <Error>: clip: empty path.
    < HTTP/1.1 200 OK
    < Server: Apache-Coyote/1.1
    < Cache-Control: no-cache,no-store,must-revalidate,max-age=0
    < Content-disposition: inline; filename=HalloOdisee.odt
    < Content-Type: application/vnd.oasis.opendocument.text
    < Content-Length: 92665
    < Date: Wed, 17 Oct 2012 13:42:41 GMT
    < 
    { [data not shown]
    * Connection #0 to host 127.0.0.1 left intact
    * Closing connection #0

The generated OpenDocument file:

    $ ls -l
    total 216
    -rw-r--r--  1 rbe  staff  92665 17 Okt 15:42 HalloOdisee.odt
