echo "Setting up InSIghts Engine as Windows service"
call %~dp0nssm-2.24\win64\nssm install InSightsEngine "%JAVA_HOME%\bin\java.exe" "-jar %~dp0Insights_Engine\PlatformEngine.jar"
Timeout 5