# Odisee(R)

[www.odisee.de](http://www.odisee.de/)  
Make Your Documents Smile.

## Quick Installation w/ ZIP archive

1. Download distribution ZIP archive for your platform

        wget https://ci.art-of-coding.eu/guestAuth/odisee2_build/latest.lastSuccessful/distrib/odisee-PLATFORM.zip

    Where PLATFORM is one of the following:

        linux-x86_64
        freebsd-amd64

1. Optional: create an user (`odisee`,  home = `$ODISEE_HOME`) and group (`odisee`)
        
        sudo groupadd -n odisee -g 5000
        sudo useradd -n odisee -u 5000 -g odisee -m -d ${ODISEE_HOME}
        mkdir -p ${ODISEE_HOME}

1. Unpack archive contents in `$ODISEE_HOME`

        unzip -q odisee-*.zip

1. Optional: login as `odisee`

        su - odisee

1. Check `etc/odiinst` for correct path to OpenOffice/LibreOffice installation
1. Start Odisee `odictl -q start`

### Apache Configuration

1. Enable modules: deflate, headers, proxy, proxy_ajp, ssl

        a2enmod deflate headers proxy proxy_ajp ssl

1. Edit `$ODISEE_HOME/etc/apache/odisee.conf` or `odisee_ssl.conf` and copy into your Apache configuration

## Documentation

See blog at https://blog.art-of-coding.eu/odisee.

## Terms of Use

### Standard Edition

Case 1: Non-commercial Use

1. Usage for non-commercial or educational purposes only  
1. You send us a notification by email and describe the use case
1. You allow us to refer to you and your project on our website

Case 2: Commercial Use

Buy a license on a per-year or per-server basis

### Enterprise Edition

Case 1: Commercial Use

A license on a per-year or per-server basis is needed.

## Copyright

Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).  
Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.  
Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.  
All rights reserved. Use is subject to license terms.
