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
if not [%3] == [] goto Usage

IF "%SNAP_HOME%"=="" ECHO ERROR: Variable SNAP_HOME is NOT defined. & exit /b 1

:: The following only works if this batch file is located in %SNAP_HOME%\bin (as the previous snappy-conf.bat)
:: (see e.g. https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/call).
:: "%~dp0\snap64.exe" --nogui --nosplash --snappy %1 %2

:: To run from anywhere for the moment, use this instead:
"%SNAP_HOME%\bin\snap64.exe" --nogui --nosplash --snappy %1 %2
goto End

:Usage
@echo Configures the SNAP-Python interface 'esa_snappy'.
@echo.
@echo %~n0 Python [Dir] ^| [/?]
@echo.
@echo     Python: Full path to Python executable to be used with SNAP, e.g. C:\Python34\python.exe
@echo     Dir:    Directory where the 'esa_snappy' module should be installed. Defaults to %USERPROFILE%\.snap\snap-python
@echo.
:End