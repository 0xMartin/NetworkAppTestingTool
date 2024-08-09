@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  natt startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and NATT_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\natt.jar;%APP_HOME%\lib\guava-32.1.3-jre.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\extentreports-5.0.5.jar;%APP_HOME%\lib\snakeyaml-1.29.jar;%APP_HOME%\lib\greenmail-1.6.1.jar;%APP_HOME%\lib\javax.mail-1.6.2.jar;%APP_HOME%\lib\httpclient-4.5.13.jar;%APP_HOME%\lib\jsoup-1.14.3.jar;%APP_HOME%\lib\org.eclipse.paho.client.mqttv3-1.2.5.jar;%APP_HOME%\lib\moquette-broker-0.15.jar;%APP_HOME%\lib\metrics-librato-5.1.0.jar;%APP_HOME%\lib\librato-java-2.1.0.jar;%APP_HOME%\lib\bugsnag-3.7.1.jar;%APP_HOME%\lib\jackson-dataformat-xml-2.14.1.jar;%APP_HOME%\lib\jackson-core-2.14.1.jar;%APP_HOME%\lib\jackson-annotations-2.14.1.jar;%APP_HOME%\lib\jackson-databind-2.14.1.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.37.0.jar;%APP_HOME%\lib\error_prone_annotations-2.21.1.jar;%APP_HOME%\lib\rxjava-3.0.4.jar;%APP_HOME%\lib\freemarker-2.3.30.jar;%APP_HOME%\lib\lombok-1.18.12.jar;%APP_HOME%\lib\gson-2.8.6.jar;%APP_HOME%\lib\jakarta.mail-1.6.5.jar;%APP_HOME%\lib\slf4j-log4j12-1.7.25.jar;%APP_HOME%\lib\HikariCP-2.4.7.jar;%APP_HOME%\lib\metrics-jvm-3.2.2.jar;%APP_HOME%\lib\metrics-core-3.2.2.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\junit-4.13.1.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\httpcore-4.4.13.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.11.jar;%APP_HOME%\lib\woodstox-core-6.4.0.jar;%APP_HOME%\lib\stax2-api-4.2.1.jar;%APP_HOME%\lib\netty-codec-http-4.1.59.Final.jar;%APP_HOME%\lib\netty-handler-4.1.59.Final.jar;%APP_HOME%\lib\netty-codec-mqtt-4.1.59.Final.jar;%APP_HOME%\lib\netty-codec-4.1.59.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.59.Final-linux-x86_64.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.59.Final.jar;%APP_HOME%\lib\netty-transport-4.1.59.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.59.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.59.Final.jar;%APP_HOME%\lib\netty-common-4.1.59.Final.jar;%APP_HOME%\lib\h2-mvstore-1.4.199.jar;%APP_HOME%\lib\reactive-streams-1.0.3.jar;%APP_HOME%\lib\jakarta.activation-1.2.1.jar;%APP_HOME%\lib\log4j-1.2.17.jar


@rem Execute natt
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %NATT_OPTS%  -classpath "%CLASSPATH%" utb.fai.NetworkAppTestingTool %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable NATT_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%NATT_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
