/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */

dataSource {
    pooled = true
    /*
    com.mysql.jdbc.Driver
    oracle.jdbc.driver.OracleDriver
    */
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = ''
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

environments {
    development {
        dataSource {
            dbCreate = 'update' // one of 'create', 'create-drop','update'
            url = 'jdbc:h2:file:devDb'
        }
    }
    test {
        dataSource {
            dbCreate = 'update'
            url = 'jdbc:h2:file:testDb'
        }
    }
    production {
        dataSource {
            dbCreate = 'update'
            url = 'jdbc:h2:file:prodDb'
        }
    }
}
