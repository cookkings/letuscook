:: Helper script for testing. Requires Windows Terminal.
@echo off
setlocal enableDelayedExpansion

rem Read configuration file.
set testcmd_jvm_flags=-ea
set testcmd_delete_logs=1
set testcmd_gradle_task=clean test jar
set testcmd_nickname=$
set testcmd_enable_second_client=1
set testcmd_address=localhost
set testcmd_port=9999
set testcmd_first_client_delay=2
set testcmd_second_client_delay=3
for /f "usebackq delims=" %%A in ("%~dp0test.ini") do (
  set "%%A"
)
if Not Defined testcmd_jar (
  echo [91mtestcmd_jar not defined. Aborting.[0m
  exit /b
)

set "cmdline=cd /d %~dp0 && chcp 65001 && java %testcmd_jvm_flags% -jar %testcmd_jar%"

if "%testcmd_delete_logs%" Equ "1" (
  del /q logs
)
call gradle %testcmd_gradle_task% || exit /b

set _second_client=
if "%testcmd_enable_second_client%" Equ "1" (
  set _second_client=; sp -V cmd /k "timeout /nobreak /t %testcmd_second_client_delay% & %cmdline% client %testcmd_address%:%testcmd_port%" %testcmd_nickname%
)

wt -M ^
	        cmd /k                 "%cmdline% server           %testcmd_port%" ^
	; sp -H cmd /k "timeout /nobreak /t %testcmd_first_client_delay% & %cmdline% client %testcmd_address%:%testcmd_port%" %testcmd_nickname%^
	%_second_client%^

rem ^ Zeile leer lassen!

popd

endlocal
