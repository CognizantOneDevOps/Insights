env.dockerimagename="devopsbasservice/buildonframework:insights"
node {

//Parse commitID (E.g, buildon-abc1234 to abc1234)
gitCommitID = sh (
    script: 'echo $commitID | cut -d "-" -f2',
    returnStdout: true
).trim()
// All single and double quotes in this file are used in a certain format.Do not alter in any step 
	//ApacheLicense Check in java and Python files
	stage ('LicenseCheck') {
           checkout scm
    	   def commit = sh (returnStdout: true, script: '''var=''
	for file in $(find . -print | grep -i -e .*[.]java -e .*[.]py)
	do
   	    if grep -q "Apache License" $file; then
        	updated="License is updated $file" ##Dummy line
    	    else
        	file_temp=`echo $file | cut -c 3-`
        	fileName="$fileName,$file_temp"
    	    fi
	done
	if [ ! -z $fileName ]; then
    		echo $fileName > files.txt 
	fi
	echo " " ''').split()
	if (fileExists('files.txt')) {
		echo "**********************License is not updated in the following files**********************"
    		sh 'cat files.txt'
    		echo "*****************************************************************************************"
    		slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* because Apache License is not updated in few files. \n List of files be found at the bottom of the page @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID",  teamDomain: 'insightscogdevops',  token: slackToken
    		sh 'rm -rf files.txt'
    		sh 'exit 1'
	} else {
    		echo 'License is up to date'
    		}
  	} //License Check ends	
   // Platform Service Starts
	try{
	
   stage ('Insight_PS_Build') {
        sh 'mvn clean install -DskipTests'
	   }
	
	stage ('Insight_PS_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		
    }
	
	stage ('Insight_PS_NexusUpload') {
		sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformService/target/PlatformService.war -DskipTests=true'
		
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp
		//PlatformService version
	    	sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformService/version"
	   	pomversionService=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/version").trim()  //Get version from pom.xml to form the nexus repo URL
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformService/${pomversionService}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.war/' > /var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact"
		PS_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact").trim()
		PS_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformService/${pomversionService}/${PS_artifactName}"
		
		//PlatformEngine version
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformEngine && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformEngine/version"
	    	pomversionEngine=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformEngine/version").trim()  //Get version from pom.xml to form the nexus repo URL
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformEngine/${pomversionEngine}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformEngine/PE_artifact"
		PE_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformEngine/PE_artifact").trim()
		PE_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformEngine/${pomversionEngine}/${PE_artifactName}"
	}
	}
	catch (err){
		
	slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
	sh 'exit 1'
	}
	
	// Platform Service Ends
	
	// Platform Insights Starts 
	try {
	
	stage ('Insight_PI_Build') {
      		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn clean install -DskipTests'
	}
	
	stage ('Insight_PI_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
	}
	
	stage ('Insight_PI_NexusUpload') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn -P NexusUpload deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DskipTests=true'
	
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformInsights/version"
       		pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformInsights/version").trim()  //Get version from pom.xml to form the nexus repo URL
	   
	   	//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformInsights/${pomversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformInsights/PI_artifact"
		PI_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformInsights/PI_artifact").trim()
		PI_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformInsights/${pomversion}/${PI_artifactName}"
    	}
	} //try ends
	catch (err){
		
	slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
	sh 'exit 1'
	
	} //Platform Insights Ends
	
	// Platform UI2.0 Starts
	try{
	stage ('Insight_PUI2.0_Build') {
        
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install --allow-root && tsd install && npm install && grunt'
	}
	
	stage ('Insight_PUI2.0_CodeAnalysis') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
	}
	
	stage ('Insight_PUI2.0_NexusUpload') {
	        //Framing Nexus URL for artifact uploaded to Nexus with unique timestamp													
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version"
       		pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version").trim()   //Get version from pom.xml to form the nexus repo URL
		
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0  && zip -r app.zip app'
	 	sh "mvn -P NexusUpload deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app.zip -DgroupId='com.cognizant.devops' -DartifactId='PlatformUI2.0' -Dpackaging=zip -Dversion=${pomversion} -DrepositoryId=nexus -Durl=http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights -DskipTests=true"
		
       		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
	   	sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformUI2.0/${pomversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.zip/' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact"
		PUI_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact").trim()
		PUI_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformUI2.0/${pomversion}/${PUI_artifactName}"
	   } 
	   }
	 catch (err){
		
		slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*,Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
		sh 'exit 1'
		}
	   
	   // Platform UI2.0 Ends

        //Send Notification to Slack Channel
	stage ('SlackNotification') {
   	    slackSend channel: '#insightsjenkins', color: 'good', message: "New Insights artifacts are uploaded to Nexus for commitID : *${env.commitID}* ,Branch - *${env.branchName}* \n *PlatformService* ${PS_artifact} \n *PlatformEngine* ${PE_artifact} \n *PlatformInsights*  ${PI_artifact} \n *PlatformUI2.0* ${PUI_artifact}", teamDomain: 'insightscogdevops', token: slackToken // "*" is for making the text bold in slack notification
  	}
}
