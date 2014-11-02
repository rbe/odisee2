/*
 * Odisee(R)
 * Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.document

import com.sun.org.apache.xerces.internal.dom.DeferredNode
import eu.artofcoding.grails.helper.FileHelper
import eu.artofcoding.grails.helper.OdiseeInstance
import eu.artofcoding.grails.helper.XmlHelper
import eu.artofcoding.odisee.OdiseeXmlCategory
import eu.artofcoding.odisee.server.OfficeConnectionFactory
import grails.transaction.Transactional
import groovy.xml.XmlUtil
import org.springframework.beans.factory.InitializingBean

import java.nio.file.Files
import java.nio.file.Path

import static eu.artofcoding.odisee.server.OdiseeConstant.MINUS_ONE
import static eu.artofcoding.odisee.server.OdiseeConstant.S_GROUP0

@Transactional
class RequestService implements InitializingBean {

    private OfficeConnectionFactory officeConnectionFactory

    @Override
    void afterPropertiesSet() {
        OdiseeInstance odiseeInstance = new OdiseeInstance()
        List<String> odiinst = odiseeInstance.readOdiinst()
        try {
            String localhost = odiinst[0][1]
            int portbase = 2001
            officeConnectionFactory = OfficeConnectionFactory.getInstance(S_GROUP0, localhost, portbase, odiinst.size())
        } catch (e) {
            throw new IllegalStateException('Cannot setup Office connection factory, please check instance configuration', e)
        }
    }

    /**
     * Save XML request to file.
     * @param requestNumber Request# to work with, -1 is the whole file.
     * @return File Reference to generated XML file.
     */
    Path saveRequestToDisk(Map arg, int requestNumber) {
        Path requestXMLFile
        String xmlString
        // Make XML string
        if (requestNumber == MINUS_ONE) {
            Path requestDir = (Path) arg.requestDir
            requestXMLFile = requestDir.resolve("${arg.uniqueRequestId}.xml" as String)
            xmlString = XmlUtil.serialize(arg.xml)
        } else {
            // Just save active request including <odisee> element
            String filename = String.format('%s_%04d.xml', arg.uniqueRequestId, requestNumber)
            Path documentDir = (Path) arg.documentDir
            requestXMLFile = documentDir.resolve(filename)
            DeferredNode deferredNode = (DeferredNode) arg.xml.request[requestNumber]
            xmlString = XmlHelper.asString(deferredNode)
        }
        Files.createDirectories(requestXMLFile.parent)
        FileHelper.writeUTF8(requestXMLFile, xmlString)
        requestXMLFile
    }

    /**
     * Save XML request to disk, process request and set arg.result.
     */
    void processSingleRequest(Map arg) {
        Path requestXMLFile = saveRequestToDisk(arg, arg.activeIndex)
        use(OdiseeXmlCategory) {
            // requestNumber = 0 as file contains only one request
            arg.result = requestXMLFile.toDocument(officeConnectionFactory, 0)
            if (!arg.result) {
                String group = 'group0'
                log.error "ODI-xxxx: ${requestXMLFile.fileName.toString()}/${arg.activeIndex}: Got no result, maybe all instances in group '${group}' are unwilling to perform?"
            }
        }
    }

}