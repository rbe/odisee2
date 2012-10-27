#!/usr/bin/env bash

ODISEE_HOME=/usr/local/odisee

# check for sudo

# add group and user
sudo pw group add -n odisee -g 5000
sudo pw user add -n odisee -u 5000 -g odisee -m -d ${ODISEE_HOME}

# copy configuration files
cd ${ODISEE_HOME}
cp etc/dot-profile .profile
cp etc/dot-cshrc .cshrc

# change owner
chown -R odisee:odisee ${ODISEE_HOME}

exit 0
