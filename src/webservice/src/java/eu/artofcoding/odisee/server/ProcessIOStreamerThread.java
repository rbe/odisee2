/*
 * Odisee(R)
 *
 * Copyright (C) 2011-2014 art of coding UG, http://www.art-of-coding.eu
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann, http://www.bensmann.com
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 *
 * rbe, 07.09.13 18:40
 */

package eu.artofcoding.odisee.server;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ProcessIOStreamerThread extends Thread {

    private final AtomicBoolean work = new AtomicBoolean(true);

    private final InputStream in;

    private final PrintStream out;

    private final String prefix;

    public ProcessIOStreamerThread(final InputStream in, final PrintStream out, final String prefix) {
        super(String.format("%s-Pipe: %s", ProcessIOStreamerThread.class.getSimpleName(), prefix));
        this.in = in;
        this.out = out;
        this.prefix = prefix;
    }

    @Override
    public void run() {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            while (work.get()) {
                final String s = r.readLine();
                if (s == null) {
                    break;
                }
                out.printf("%s%s%n", prefix, s);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void stopWork() {
        work.getAndSet(false);
    }

}
