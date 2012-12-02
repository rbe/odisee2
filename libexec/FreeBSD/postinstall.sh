#!/usr/bin/env bash

ODISEE_HOME=/usr/local/odisee

set -o nounset

# check for sudo
if [[ ! -x sudo ]]
then
    echo "'sudo' missing"
    exit 1
fi

# add group and user
sudo pw group add -n odisee -g 5000
sudo pw user add -n odisee -u 5000 -g odisee -m -d ${ODISEE_HOME} -s "/usr/local/bin/bash" -c "Odisee(R) Server"
mkdir -p ${ODISEE_HOME}

# copy configuration files
mv .bash_profile ${ODISEE_HOME}/.bash_profile
mv * ${ODISEE_HOME} 

# change owner
chown -R odisee:odisee ${ODISEE_HOME}

exit 0
