@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "M2_HOME=D:\Dev\JiTrack\apache-maven-3.9.6"
set "PATH=%M2_HOME%\bin;%PATH%"

cd /d D:\Dev\JiTrack

call mvn.cmd spring-boot:run