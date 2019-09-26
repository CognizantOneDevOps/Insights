echo "Setting up InSIghts WebhookEngine as Windows service"
call %~dp0nssm-2.24\win64\nssm install InSightsWebhookEngine "%JAVA_HOME%\bin\java.exe" "-jar %~dp0Insights_WebhookEngine\PlatformWebhookEngine.jar"
Timeout 5