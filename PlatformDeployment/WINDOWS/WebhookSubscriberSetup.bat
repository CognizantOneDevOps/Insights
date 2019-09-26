echo "Setting up InSIghts WebhookEngine as Windows service"
call %~dp0nssm-2.24\win64\nssm install InSightsWebhookSubscriber "%JAVA_HOME%\bin\java.exe" "-jar %~dp0Insights_Webhook\PlatformInsightsWebHook.jar"
Timeout 5