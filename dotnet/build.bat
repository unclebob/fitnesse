@echo off
:start
echo Building fit-dotnet with nant...
tools\nant\nant %*
pause
goto :start
