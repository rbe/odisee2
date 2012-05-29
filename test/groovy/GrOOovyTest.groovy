/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

// TODO http://wiki.services.openoffice.org/wiki/API/Samples/Groovy/Writer/CleanUpForHTML
// new File("private:factory/swriter")

class GrOOovyTest {

    /**
     * Installation directory of OOo.
     */
    def static final oooProgram = "/Applications/LibreOffice.org.app/Contents/MacOS"

    /**
     * Options for starting an OOo instance.
     */
    def static final oooOptions = ["-nologo",
            "-nofirststartwizard", "-nodefault",
            "-nocrashreport", "-norestart", "-norestore",
            "-nolockcheck"]

    /**
     * Group for local OOo instance.
     */
    def static final localGroup = ["pipe1"]

    /**
     * Group for TCP/IP/remote OOo instance(s).
     */
    def static final remoteGroup = ["127.0.0.1": [2001]/*..2009*/]

    /**
     * Connect to a local or remote OOo instance through OOoConnectionManager.
     * If that specified instance does not exist, it is started automatically.
     * @param map A map: oooProgram Installation directory of OOo.
     *                   groupConfig A map: configuration of group 'groupName', e.g.
     *                                      group1: ["127.0.0.1": [2002]] or ["127.0.0.1": [2002..2009]
     */
    def static connectOOo(map) {
        // Setup and return connection manager
        new eu.artofcoding.odisee.ooo.server.OOoConnectionManager(map)
    }

    /**
     * Process an XML request with OOo instance provided by OOoConnectionManager.
     * @param ocm OOoConnectionManager.
     * @param num Process request 'num' times.
     */
    def static processXML(ocm, num = 0) {
        use(eu.artofcoding.odisee.OdiseeXmlCategory) {
            def r = new java.io.File("/Users/rbe/project/odisee/var/req.xml")
            try {
                //println "#${num}: starting"
                def result = r.toDocument(ocm, [id: num])
                if (result) {
                    //println "#${num}: took ${result.time} ms"
                    //println "result=${result}"
                } else {
                    // This should never happen; except all OOo instances have crashed.
                    //println "#${num} FAILED"
                }
            } catch (e) {
                //println "${num}: could not generate document from request ${r}: ${e}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Process XML request through remote OOo instance.
     */
    def static remoteWithXML(num) {
        // Start time
        def start1 = System.nanoTime()
        // Setup connection manager
        def ocm = new eu.artofcoding.odisee.ooo.server.OOoConnectionManager(oooProgram: oooProgram, group1: remoteGroup)
        // ThreadPool; size = OOo instances / 2
        def tp = java.util.concurrent.Executors.newFixedThreadPool(4)
        // Process XML request 'num' times
        0.upto 0, { n ->
            tp.execute({-> processXML(ocm, n) } as java.lang.Runnable)
        }
        // Shutdown pool
        tp.shutdown()
        while (!tp.isTerminated()) {
            Thread.sleep(1 * 1000)
        }
        //println "Thread pool shut down"
        ocm.shutdown(/*true*/)
        //println "OCM shut down"
        // End time
        def stop1 = System.nanoTime()
        def ms1 = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(stop1 - start1)
        //println "took ${ms1} ms"
    }

    /**
     * Process XML request through local OOo instance.
     */
    def static localWithXML = {
        // Start time
        def start1 = System.nanoTime()
        // Setup connection manager
        // oooProgram: "/Applications/OpenOffice.org.app/Contents/MacOS",
        def ocm = connectOOo(group1: localGroup)
        processXML(ocm)
        //
        ocm.shutdown(/*true*/)
        //println "OCM shut down"
        // End time
        def stop1 = System.nanoTime()
        def ms1 = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(stop1 - start1)
        //println "took ${ms1} ms"
    }

    /**
     * Start an OOo instance, communicate through a pipe:
     * open a template, fill tables with values and save document.
     */
    def static localAndManual = {
        def start1 = System.nanoTime()
        // Setup connection manager
        // oooProgram: "/Applications/OpenOffice.org.app/Contents/MacOS", 
        def ocm = connectOOo(oooOptions: oooOptions, group1: ["pipe1"])
        // Get connection to OpenOffice
        def oooConnection = ocm.acquire("group1")
        if (!oooConnection)
            throw new IllegalStateException("No connection")
        // Get template
        def template = new java.io.File("../../var/template/abc/abc_rev5.ott")
        // Process template
        def doc
        try {
            use(eu.artofcoding.odisee.ooo.OOoDocumentCategory) {
                // Create new document from template
                doc = template.open(oooConnection, [Hidden: Boolean.FALSE])
                // Set table data
                use(eu.artofcoding.odisee.ooo.OOoTextTableCategory) {
                    doc['Tabelle1'] = [
                            ['test a1', 'test a2'],
                            ['test b1', 'test b2'],
                            ['test c1', 'test c2'],
                            ['test d1', 'test d2']
                    ] as String[][]
                    /*
                doc['Tabelle2'] = [
                        A1: 'map a1', B1: 'map b1',
                        A2: 'a2', B2: 'b2', C2: 'c2', D2: 'die is neu'
                    ]
                    */
                    doc['Tabelle1\$A1'] = 'A1'
                }
                // Execute macro
                //doc.executeMacro("Westa.Main.Main?language=Basic&location=application")
                // Save document
                doc.saveAs(new java.io.File("/Users/rbe/Desktop/bla.odt"))
            }
        } catch (e) {
            e.printStackTrace()
        } finally {
            // Close document
            use(eu.artofcoding.odisee.ooo.OOoDocumentCategory) {
                doc.close()
            }
            // Shutdown
            //println "shutdown OCM"
            ocm.shutdown(/*true*/)
            // Stats
            def stop1 = System.nanoTime()
            def ms1 = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(stop1 - start1)
            //println "took ${ms1} ms"
        }
    }

    /**
     * Main.
     */
    static main(args) {
        //localAndManual()
        //localWithXML()
        remoteWithXML()
    }

}
