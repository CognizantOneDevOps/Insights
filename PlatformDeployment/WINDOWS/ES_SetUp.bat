echo "Setting up Elastic Search as windows service"
call %~dp0elasticsearch-5.6.4\bin\elasticsearch-service install
Timeout 2