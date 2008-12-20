@echo off

setlocal 

if not "%WORKBENCH_JDK%"=="" set JAVA_HOME=%WORKBENCH_JDK%
if not "%JAVA_HOME%"=="" set JAVA_BINPATH=%JAVA_HOME%\bin\

set wbdir=%~dp0

if "%1"=="console" goto console_mode

:gui
start %JAVA_BINPATH%javaw -Xmx256m -jar %wbdir%sqlworkbench.jar %*
goto :eof

:console_mode
title SQL Workbench/J
%JAVA_BINPATH%java -Xmx256m -cp %wbdir%sqlworkbench.jar;%wbdir%poi.jar workbench.console.SQLConsole %*  
goto :eof

