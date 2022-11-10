#!/bin/sh

me=`basename "$0"`
temp=`dirname "$0"`
dir=`cd "$temp" && pwd -P`

usage() {
    echo "Configures the SNAP-Python interface 'esa_snappy'."
    echo ""
    echo "usage: $me <python> [<dir>]"
    echo ""
    echo "    <python>   Full path to Python executable to be used with SNAP, e.g. /user/bin/python3"
    echo "    <dir>      Directory where the 'esa_snappy' module should be installed. Defaults to ~/.snap/snap-python"
    echo ""
}

if [ "$#" = 0 ] || [ "$#" -ge 4 ]; then
    usage
    exit 1
fi
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    usage
    exit 0
fi

if [ -z "${SNAP_HOME}" ]; then
  echo "ERROR: Environment variable SNAP_HOME is not set."
  exit 1
fi

#"$dir"/snap --nogui --nosplash --snappy "$@"
"${SNAP_HOME}"/snap --nogui --nosplash --snappy "$@"
exit $?
