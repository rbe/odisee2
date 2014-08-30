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
sudo groupadd -n odisee -g 5000
sudo useradd -n odisee -u 5000 -g odisee -m -d ${ODISEE_HOME}
mkdir -p ${ODISEE_HOME}

# copy configuration files
mv .bash_profile ${ODISEE_HOME}/.bash_profile
mv * ${ODISEE_HOME} 

# change owner
chown -R odisee:odisee ${ODISEE_HOME}

exit 0
