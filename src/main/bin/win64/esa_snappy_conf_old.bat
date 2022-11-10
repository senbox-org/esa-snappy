@echo off

:: Runs the configuration of Python 'esa_snappy' module.
:: Requires proper setup of the environment, i.e.
:: - SNAP and Python need to be installed
:: - external plugin 'esa_snappy' must be installed in SNAP
:: - SNAP_HOME needs to be set

:: e.g. 
:: SNAP_HOME=D:\\snap\9.0\snap

if [%1] == [] goto Usage
if [%1] == [/?] goto Usage
if not [%2] == [] goto Usage

IF "%SNAP_HOME%"=="" ECHO ERROR: Variable SNAP_HOME is NOT defined. & exit /b 1

set JPY_JAR=%SNAP_HOME%\snap\modules\ext\org.esa.snap.esa_snappy\org-jpy\jpy.jar
set SNAP_RUNTIME_JAR=%SNAP_HOME%\snap\modules\ext\org.esa.snap.snap-rcp\org-esa-snap\snap-runtime.jar

set JARS=%SNAP_HOME%\snap\modules\*;%JPY_JAR%;%SNAP_RUNTIME_JAR%

%SNAP_HOME%\jre\bin\java.exe -cp "%JARS%" org.esa.snap.main.EsaSnappyConfigurator %1

goto End

:Usage
@echo Configures the ESA SNAP-Python interface 'esa_snappy'.
@echo Usage:
@echo.
@echo %~n0 Python
@echo.
@echo     Python: Full path to Python executable to be used with SNAP, e.g. C:\Miniconda\python.exe
@echo.
:End