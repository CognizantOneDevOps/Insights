pushd PlatformUI2.0
bower install --config.strict-ssl=false --config.proxy="http://proxy.cognizant.com:6050" --config.https-proxy="http://proxy.cognizant.com:6050" --force
npm install
pushd PlatformUI2.0
grunt
REM chage the below command for actual path
Xcopy /S /I /E /Y PlatformUI2.0\app C:\INSIGHTS_RELEASE\InSightsCodeBase\app