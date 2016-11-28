@echo off&setlocal enabledelayedexpansion
color 02
set curdir=%~dp0
set url=http://10.109.5.198:8081/nexus/content/repositories/thirdparty/
cd %curdir%

for /f  "tokens=3 delims=>,<" %%i in ('findstr /c:"<groupId>" "pom.xml"') do (
      if not "%%i"=="" (
        set groupId=%%i
        goto _break1
      )
    )
:_break1

for /f  "tokens=3 delims=>,<" %%i in ('findstr /c:"<artifactId>" "pom.xml"') do (
      if not "%%i"=="" (
        set artifactId=%%i
        goto _break2
      )
    )
:_break2

for /f  "tokens=3 delims=>,<" %%i in ('findstr /c:"<version>" "pom.xml"') do (
      if not "%%i"=="" (
        set version=%%i
        goto _break3
      )
    )
:_break3

for /f  "tokens=3 delims=>,<" %%i in ('findstr /c:"<packaging>" "pom.xml"') do (
      if not "%%i"=="" (
        set package=%%i
        goto _break4
      )
    )
:_break4

set file=%curdir%target\%artifactId%-%version%.%package%
set pomFile=%curdir%pom.xml
if not "%1%"=="" (
        set url=%1%
      )
set repository=thirdparty
if not "%2%"=="" (
        set repository=%2%
      )
      
echo start to deploy Maven project...
echo Group: %groupId%
echo artifactId: %artifactId%
echo version: %version%
echo file: %file%
echo url: %url%
echo repository: %repository%

echo clean...
call mvn clean

echo package...
call mvn package install -Dmaven.test.skip=true

echo install...
call mvn install

echo deploy...
call mvn deploy:deploy-file -DgroupId=%groupId% -DartifactId=%artifactId% -Dversion=%version% -Dpackaging=%package% -Dfile=%file% -Durl=%url% -DpomFile=%pomFile% -DrepositoryId=%repository%
echo done.
pause