#
# Odisee(R) Server
#

# Set Odisee home directory
# Use $HOME or e.g. /opt/odisee on Linux, /usr/local/odisee on *BSD
ODISEE_HOME=$HOME
export ODISEE_HOME

# bash completion
_completion_loader()
{
    . "${ODISEE_HOME}/etc/bash_completion.d/$1.sh" >/dev/null 2>&1 && return 124
}
complete -D -F _completion_loader

# Load environment
# For local settings edit $ODISEE_HOME/etc/local.odienv
. ${ODISEE_HOME}/etc/odienv