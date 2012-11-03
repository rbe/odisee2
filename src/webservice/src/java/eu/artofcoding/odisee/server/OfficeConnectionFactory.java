/*
 * Odisee(R)
 * odisee-server, odisee-xml-processor
 *
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com/
 * Copyright (C) 2011-2012 art of coding UG, http://www.art-of-coding.eu/
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 04.08.12 17:52
 */

package eu.artofcoding.odisee.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provide OfficeConnections, provide a pool for them and act as a watchdog.
 */
public class OfficeConnectionFactory {

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private String groupname;

    private final int QUEUE_POLL_TIMEOUT = 5;
    private final TimeUnit QUEUE_POLL_TIMEUNIT = TimeUnit.SECONDS;

    private List<InetSocketAddress> addresses;
    private LinkedBlockingQueue<OfficeConnection> connections;

    public OfficeConnectionFactory(String groupname, List<InetSocketAddress> addresses) {
        this.groupname = groupname;
        this.addresses = addresses;
        initializeConnections();
    }

    public OfficeConnectionFactory(String groupname, String host, int basePort, int count) {
        this.groupname = groupname;
        this.addresses = new ArrayList<InetSocketAddress>();
        addConnections(host, basePort, count);
        initializeConnections();
    }

    public void addConnections(String host, int basePort, int count) {
        for (int i = 0; i < count; i++) {
            addresses.add(new InetSocketAddress(host, basePort + i));
        }
    }

    public OfficeConnection fetchConnection(boolean waitForever) throws OdiseeServerException {
        // Check state
        if (shuttingDown.get()) {
            throw new OdiseeServerException("Shutdown in progress");
        }
        OfficeConnection officeConnection = null;
        // Poll a connection from queue, waiting some seconds if necessary
        try {
            if (!waitForever) {
                officeConnection = connections.poll(QUEUE_POLL_TIMEOUT, QUEUE_POLL_TIMEUNIT);
            } else {
                officeConnection = connections.take();
            }
            //dumpNextConnection("fetch", officeConnection);
        } catch (InterruptedException e) {
            // ignore
        }
        /*
        // TODO No connection polled and queue has capacity, create a new one
        if (null == officeConnection && connections.remainingCapacity() > 0) {
            officeConnection = new OfficeConnection();
            officeConnection.connect(unoURL);
        }
        */
        // Check if we could get an OfficeConnection
        if (null == officeConnection) {
            throw new OdiseeServerException("[group=" + groupname + "] Could not fetch connection from pool, sorry.");
        }
        try {
            // Connect
            officeConnection.connect();
        } catch (OdiseeServerException e) {
            // Put connection back into pool, better luck next time
            repositConnection(officeConnection);
        }
        // Return connection
        return officeConnection;
    }

    public void repositConnection(OfficeConnection officeConnection) throws OdiseeServerException {
        // Check state
        if (shuttingDown.get()) {
            throw new OdiseeServerException("Shutdown in progress");
        }
        boolean connectionWasPutBack = false;
        int i = 0;
        while (i++ < 3 && !connectionWasPutBack) {
            connectionWasPutBack = connections.offer(officeConnection);
            //dumpNextConnection("reposit", officeConnection);
        }
        if (!connectionWasPutBack) {
            throw new OdiseeServerException("[group=" + groupname + "] Could not reposit connection, I tried it more than once, sorry.");
        }
    }

    /**
     * Shutdown all connections and the factory.
     * @param cleanup If false, OfficeConnection objects remain in the pool, otherwise they are removed.
     */
    public void shutdown(boolean cleanup) {
        shuttingDown.getAndSet(true);
        Iterator<OfficeConnection> iter = connections.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().close();
                if (cleanup) {
                    iter.remove();
                }
            } catch (OdiseeServerException e) {
                // ignore
            }
        }
    }

    private synchronized void initializeConnections() {
        // Check state
        if (null == addresses || addresses.size() == 0) {
            throw new OdiseeServerRuntimeException("Initialization error");
        }
        // Setup queue for connections
        connections = new LinkedBlockingQueue<OfficeConnection>(addresses.size());
        // Process all TCP/IP addresses
        for (InetSocketAddress socketAddress : addresses) {
            OfficeConnection officeConnection = new OfficeConnection(socketAddress);
            try {
                officeConnection.bootstrap(false);
                connections.offer(officeConnection);
            } catch (OdiseeServerException e) {
                // ignore
                System.err.println("[group=" + groupname + "] Could not bootstrap connection to " + socketAddress + ": " + e.getLocalizedMessage());
            }
        }
    }

    private void dumpNextConnection(String action, OfficeConnection officeConnection) {
        try {
            System.out.println(Thread.currentThread().getName() + " [group=" + groupname + "] " + action + ": " + officeConnection + ", next is " + connections.element() + ", " + connections.size() + " available");
        } catch (NoSuchElementException e) {
            // ignore
            System.out.println(Thread.currentThread().getName() + " [group=" + groupname + "] " + action + ": " + officeConnection + ", no more connections available");
        }
    }

}
