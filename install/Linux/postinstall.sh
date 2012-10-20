#!/usr/bin/env bash

ODISEE_HOME=/opt/odisee

# check for sudo

# add group and user
sudo groupadd -n odisee -g 5000
sudo useradd -n odisee -u 5000 -g odisee -m -d ${ODISEE_HOME}

# copy configuration files
cd ${ODISEE_HOME}
cp etc/dot-profile .profile

# change owner
chown -R odisee:odisee ${ODISEE_HOME}

# download LibreOffice
wget http://download.documentfoundation.org/libreoffice/stable/3.6.2/deb/x86_64/LibO_3.6.2_Linux_x86-64_install-deb_en-US.tar.gz

exit 0
