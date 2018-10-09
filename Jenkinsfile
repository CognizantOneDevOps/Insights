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
	for file in $(find . -print | grep -i -e .*[.]java -e .*[.]py -e .*[.]sh -e .*[.]bat | grep -Eiv "*__init__.py*" )
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
		echo "#################################################################################################################"
		echo "**********************LICENSE IS NOT UPDATED IN THE FOLLOWING LIST OF COMMA SEPARATED FILES *********************"
		sh 'cat files.txt'
		echo "*****************************************************************************************************************"
		echo "#################################################################################################################"
    		slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* because Apache License is not updated in few files. \n List of files can be found at the bottom of the page @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID",  teamDomain: 'insightscogdevops',  token: slackToken
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
		sh 'mvn deploy -DskipTests=true'		
		}
	
	}
	catch (err){
		
	slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
	sh 'exit 1'
	}	
	// Platform Service Ends	
	
	
	// Platform UI2.0 Starts
	try{
	stage ('Insight_PUI2.0_Build') {
        
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install --allow-root && tsd install && npm install && grunt'
	}
	
	stage ('Insight_PUI2.0_CodeAnalysis') {		
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=app/src/modules -Dsonar.language=js -Dsonar.javascript.file.suffixes=.ts'
	}
	
	stage ('Insight_PUI2.0_NexusUpload') {		
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp													
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version"
       		pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version").trim()   //Get version from pom.xml to form the nexus repo URL
		
		if(pomversion.contains("SNAPSHOT")){		
			NEXUSREPO="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights"			
		
		} else{		
		    NEXUSREPO="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/InsightsRelease"		
		
		}		
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0  && zip -r app.zip app'
	 	sh "mvn deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app.zip -DgroupId='com.cognizant.devops' -DartifactId='PlatformUI2.0' -Dpackaging=zip -Dversion=${pomversion} -DrepositoryId=nexus -Durl=${NEXUSREPO} -DskipTests=true"		
	     } 
	   
	   }
	 catch (err){
		
		slackSend channel: '#insightsjenkins', color: 'good', message: "BuildFailed for commitID - *$gitCommitID*,Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
		sh 'exit 1'
		}
	   
	   // Platform UI2.0 Ends
	   
	   
        //Send Notification to Slack Channel
	stage ('SlackNotification') {
	
	//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp
		//PlatformService version
	    	sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformService/version"
	   	pomversionService=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/version").trim()  //Get version from pom.xml to form the nexus repo URL
		
		//PlatformEngine version
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformEngine && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformEngine/version"
	    	pomversionEngine=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformEngine/version").trim()  //Get version from pom.xml to form the nexus repo URL
			
		//PlatformInsights version
			//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformInsights/version"
       		pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformInsights/version").trim()  //Get version from pom.xml to form the nexus repo URL
	   
	    //PlatformUI version
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp													
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version"
       		pomUIversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version").trim()  
		
		if(pomversionService.contains("SNAPSHOT") && pomversionEngine.contains("SNAPSHOT") && pomversion.contains("SNAPSHOT") && pomUIversion.contains("SNAPSHOT")){
		
			NEXUSREPO="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights"
			
			//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformService/${pomversionService}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.war/' > /var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact"
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformEngine/${pomversionEngine}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformEngine/PE_artifact"
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformInsights/${pomversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformInsights/PI_artifact"

		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
	   	sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformUI2.0/${pomUIversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.zip/' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact"		
		
		
		} else {
		
		    NEXUSREPO="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/InsightsRelease"
			
			//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformService/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<release>).*?(?=</release>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.war/' > /var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact"
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformEngine/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<release>).*?(?=</release>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformEngine/PE_artifact"
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformInsights/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<release>).*?(?=</release>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.jar/' > /var/jenkins/jobs/$commitID/workspace/PlatformInsights/PI_artifact"
		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
	   	sh "curl -s ${NEXUSREPO}/com/cognizant/devops/PlatformUI2.0/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<release>).*?(?=</release>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.zip/' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact"	
		
		
		}	
		
		//Platform Service
		PS_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact").trim()
		PS_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformService/${pomversionService}/${PS_artifactName}"		
		
		//Platform Engine
		PE_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformEngine/PE_artifact").trim()
		PE_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformEngine/${pomversionEngine}/${PE_artifactName}"
		
		//Platform Insights
		PI_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformInsights/PI_artifact").trim()
		PI_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformInsights/${pomversion}/${PI_artifactName}"
		
		//Platform UI 2.0
		PUI_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact").trim()
		PUI_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformUI2.0/${pomUIversion}/${PUI_artifactName}"
	
	
   	    slackSend channel: '#insightsjenkins', color: 'good', message: "New Insights artifacts are uploaded to Nexus for commitID : *${env.commitID}* ,Branch - *${env.branchName}* \n *PlatformService* ${PS_artifact} \n *PlatformEngine* ${PE_artifact} \n *PlatformInsights*  ${PI_artifact} \n *PlatformUI2.0* ${PUI_artifact}", teamDomain: 'insightscogdevops', token: slackToken // "*" is for making the text bold in slack notification
  	}
}
