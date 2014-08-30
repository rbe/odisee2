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

# bash completion
#_completion_loader()
#{
#    . "${ODISEE_HOME}/etc/bash_completion.d/$1.sh" >/dev/null 2>&1 && return 124
#}
#complete -D -F _completion_loader

# Load environment
# For local settings edit $ODISEE_HOME/etc/local.odienv
. ${ODISEE_HOME}/etc/odienv
