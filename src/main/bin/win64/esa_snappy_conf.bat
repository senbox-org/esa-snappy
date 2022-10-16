@echo off

:: Runs the configuration of Python 'esa_snappy' module.
:: Requires proper setup of the environment, i.e.
:: - SNAP and Python need to be installed
:: - external plugin 'esa_snappy' must be installed in SNAP
:: - SNAP_HOME and PYTHONHOME need to be set

:: e.g. 
:: SNAP_HOME=D:\olaf\bc\snap-snapshots\9\9.0-release\snap
:: PYTHONHOME=D:\olaf\Anaconda3

IF "%SNAP_HOME%"=="" ECHO ERROR: Variable SNAP_HOME is NOT defined. & exit /b 1
IF "%PYTHONHOME%"=="" ECHO ERROR: Variable PYTHONHOME is NOT defined. & exit /b 1

set JPY_JAR=%SNAP_HOME%\snap\modules\ext\org.esa.snap.esa_snappy\org-jpy\jpy.jar
set SNAP_RUNTIME_JAR=%SNAP_HOME%\snap\modules\ext\org.esa.snap.snap-rcp\org-esa-snap\snap-runtime.jar

set JARS=%SNAP_HOME%\snap\modules\*;%JPY_JAR%;%SNAP_RUNTIME_JAR%

%SNAP_HOME%\jre\bin\java.exe -cp "%JARS%" org.esa.snap.main.EsaSnappyConfigurator %PYTHONHOME%\python.exe