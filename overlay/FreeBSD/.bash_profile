#!/usr/bin/env bash
# 
# Odisee(R)
# Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
# Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
#
# Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
# All rights reserved. Use is subject to license terms.
# 

# Set Odisee home directory
# Use $HOME or /usr/local/odisee
ODISEE_HOME=$HOME
export ODISEE_HOME

# Use Java 7
JAVA_VERSION=1.7
export JAVA_VERSION

# Load environment
# For local settings edit $ODISEE_HOME/etc/local.odienv
. ${ODISEE_HOME}/etc/odienv
