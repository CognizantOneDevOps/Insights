# Instructions

*Offline Installation requires all the artifacts

Note: It ia a must to copy the folder before runnin the scripts
1) Copy the folder Offline_Instalaltion into the RHEL7 box through WinSCP.
Copy the folder to /usr/ folder location.
Command : sudo cp -r /**location in which the foder is copied**/Offline_Installation/ /usr/   

2) After running the scripts
Open the server-config.json file

 cat /usr/INSIGHTS_HOME/.InSights/server-config.json
 Make changes in  "grafanaEndpoint": "http://localhost:3000" to "http://*Public IP*:3000"

3) Change the  is
offlineAgentPath 

"agentDetails": {
   "isOnlineRegistration" : true
   "docrootUrl":"http://platform.cogdevops.com/insights_install/release",
   "offlineAgentPath" : "****",
   "unzipPath" : "//opt//agent20//download",
   "agentExchange" : "iAgent",
   "agentPkgQueue" : "INSIGHTS.AGENTS.PACKAGE"
  },


 to

"agentDetails": {
   "isOnlineRegistration" : false,
   "docrootUrl":"http://platform.cogdevops.com/insights_install/release",
   "offlineAgentPath" : "//usr//Offline_Installation//Insight_Components//Version//",
   "unzipPath" : "//opt//agent20//download",
   "agentExchange" : "iAgent",
   "agentPkgQueue" : "INSIGHTS.AGENTS.PACKAGE"
  },

