#!/usr/local/bin/bash

set -o nounset

declare -i corefactor
corefactor=0

declare -i maxcpuidx
maxcpuidx=$(cpuset -g | awk '{print $(NF)}')

declare -i cpunum
cpunum=0

declare verbose
verbose="false"

e() {
    [ "${verbose}" = "true" ] && echo $*
}

_nextcpunum() {
    # Just 1 core?
    if [ ${maxcpuidx} -eq 0 ]
    then
        cpunum=0
    else
        cpunum=$((cpunum + 1))
        # Reached maxcpu?
        [ ${cpunum} -gt ${maxcpuidx} ] && cpunum=0
    fi
}

per_core() {
    local corefactor=$1
    local pid=$2
    local newcpu=""
    if [ ${corefactor} -eq 1 ]
    then
        _nextcpunum
        newcpu="${cpunum}"
    elif [ ${corefactor} -gt 1 ]
    then
        local i=0
        while [ ${i} -gt ${corefactor} ]
        do
            _nextcpunum
            newcpu="${newcpu},${cpunum}"
            i=$((i + 1))
        done
        newcpu=${newcpu#,}
    fi
    e "Assigning CPU #${newcpu} to Odisee process #${pid}"
    cpuset -l ${newcpu} -p ${pid}
}

while getopts ":c:v" opt
do
    case "${opt}" in
        c) corefactor=$OPTARG ;;
        v) verbose="true" ;;
    esac
done

if [ ${corefactor} -gt 0 ]
then
    if [ ${corefactor} -gt $((maxcpuidx + 1)) ]
    then
        echo "No action taken, you don't have ${corefactor} cores, sorry. CPU count is $((maxcpuidx + 1))."
    else
        # Get PID list of running instances 
        for p in $(ps ax | grep soffice.bin | grep -v grep | awk '{print $1}')
        do
            e "Process ${p} actually uses CPUs: $(cpuset -g -p ${p} | awk '{print $(NF)}')"
            per_core ${corefactor} ${p}
        done
    fi
else
    echo "usage: $0 [-v] -c <CPU core factor>"
    echo "       -c N   Assign exactly N Odisee processes to one CPU"
    echo "Examples:"
    echo "       -c 1   Assign exactly one Odisee process to one CPU"
    echo "       -c 2   Assign exactly two Odisee processes to one CPU"
    echo "       -c 4   Assign exactly four Odisee processes to one CPU"
    exit 1
fi

exit 0
