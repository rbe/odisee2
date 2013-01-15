rem @echo off

set ODI_HOST=127.0.0.1
set ODI_PORT=2001

if "x%1x" == "xcreate-instx" goto createinst
if "x%1x" == "xdelete-instx" goto deleteinst
if "x%1x" == "xstart-instx" goto startinst
if "x%1x" == "xstop-instx" goto stopinst

:usage
echo Odisee Server
echo odictl.bat
echo       start-inst
echo       stop-inst
goto end

:createinst
set ODI_LO_DRIVE=C:
if %PROCESSOR_ARCHITECTURE% == x86 set ODI_LO_PRG=%ODI_LO_DRIVE%\Program Files
if %PROCESSOR_ARCHITECTURE% == AMD64 set ODI_LO_PRG=%ODI_LO_DRIVE%\Program Files (x86)
set ODI_LO_HOME=%ODI_LO_PRG%\LibreOffice 3.6
set ODI_USER_INST=file:///%~dp0var/temp/odisee_port%ODI_PORT%
set ODI_USER_INST=%ODI_USER_INST:\=/%
set ODI_LO_START=%ODI_LO_HOME%\program\soffice.exe -env:UserInstallation="%ODI_USER_INST%" --accept="socket,host=%ODI_HOST%,port=%ODI_PORT%;urp;StarOffice.ServiceManager" --invisible
sc \\%COMPUTERNAME% create OdiseeInst1 binPath= "%ODI_LO_START%" type= own depend= "tcpip" error= "ignore" DisplayName= "Odisee Instance 1"
goto end

:deleteinst
sc \\%COMPUTERNAME% delete OdiseeInst1
goto end

:startinst
sc start OdiseeInst1
goto end

:stopinst
rem taskkill /F /IM soffice.bin
rem taskkill /F /IM soffice.exe
sc stop OdiseeInst1
goto end

:end
