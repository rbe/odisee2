# Installing Odisee(R) Server

## UNIX(TM)

### OpenSSH

    Subsystem    sftp    internal-sftp
    
    Match User odisee
        ChrootDirectory none
    
    Match User !odisee Group odisee
        ChrootDirectory %h
        ForceCommand internal-sftp


## Debian GNU/Linux

    $ sudo dpkg -i Odisee.deb

## FreeBSD

* Create group and user

        pw group add -n odisee -g 5000
        pw user add -n odisee -u 5000 -g odisee -m -d /usr/local/odisee

* Download and extract installation media

        cd /usr/local/odisee
        wget http://files.odisee.de/product/odisee-server/Odisee_FreeBSD.tar.gz
        tar xzf Odisee_FreeBSD.tar.gz

## Microsoft Windows

