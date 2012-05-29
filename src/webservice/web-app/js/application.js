/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

var Ajax;
if (Ajax && (Ajax != null)) {
    Ajax.Responders.register({
        onCreate: function() {
            if ($('spinner') && Ajax.activeRequestCount > 0)
                Effect.Appear('spinner', {duration: 0.5, queue: 'end'});
        },
        onComplete: function() {
            if ($('spinner') && Ajax.activeRequestCount == 0)
                Effect.Fade('spinner', {duration: 0.5, queue: 'end'});
        }
    });
}
