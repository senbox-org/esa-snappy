#!/bin/bash

# Runs the configuration of Python 'esa_snappy' module.
# Requires proper setup of the environment, i.e.
# - SNAP and Python need to be installed
# - external plugin 'esa_snappy' must be installed in SNAP
# - SNAP_HOME and PYTHONHOME need to be set
#
# e.g.
# SNAP_HOME=/home/olafd/snap_9.0.0
# PYTHONHOME=PYTHONHOME=/home/olafd/miniconda3

if [[ -z "${SNAP_HOME}" ]]; then
  echo "ERROR: Environment variable SNAP_HOME is not set."
  exit 1
fi

if [[ -z "${PYTHONHOME}" ]]; then
  echo "ERROR: Environment variable PYTHONHOME is not set."
  exit 1
fi

JPY_JAR=${SNAP_HOME}/snap/modules/ext/org.esa.snap.esa_snappy/org-jpy/jpy.jar
SNAP_RUNTIME_JAR=${SNAP_HOME}/snap/modules/ext/org.esa.snap.snap-rcp/org-esa-snap/snap-runtime.jar

JARS=${SNAP_HOME}/snap/modules/*:${JPY_JAR}:${SNAP_RUNTIME_JAR}

echo "${SNAP_HOME}/jre/bin/java -cp ${JARS} org.esa.snap.main.EsaSnappyConfigurator ${PYTHONHOME}/bin/python"
${SNAP_HOME}/jre/bin/java -cp ${JARS} org.esa.snap.main.EsaSnappyConfigurator ${PYTHONHOME}/bin/python
