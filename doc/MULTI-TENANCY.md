# Multi-Tenancy in Odisee(R) Server

    Subsystem   sftp    internal-sftp
    
    Match User odisee
        ChrootDirectory none
    
    Match User !odisee Group odisee
        ChrootDirectory %h
        ForceCommand internal-sftp
