env.dockerimagename="devopsbasservice/buildonframework:insightsPUI3"
node {

//Parse commitID (E.g, buildon-abc1234 to abc1234)
gitCommitID = sh (
    script: 'echo $commitID | cut -d "-" -f2',
    returnStdout: true
).trim()
	
	stage('SCM Checkout') {
	checkout scm	
	}
// All single and double quotes in this file are used in a certain format.Do not alter in any step build
	//ApacheLicense Check in java and Python files . License for Enterprise.
	/*
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
    		slackSend channel: '#insightsjenkins', color: 'good', message: "Insights Enterprise Build Failed BuildFailed for commitID - *$gitCommitID*, Branch - *$branchName* because Apache License is not updated in few files. \n List of files can be found at the bottom of the page @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID",  teamDomain: 'insightscogdevops',  token: slackToken
    		sh 'rm -rf files.txt'
    		sh 'exit 1'
	} else {
    		echo 'License is up to date'
    		}
  	} //License Check ends	
	
	*/
   // Platform Service Starts
	try{
	//Build for the pom profile enterprise
  	stage ('Insight_PS_Build') {
        sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI3 && npm install'
	sh 'cd /var/jenkins/jobs/$commitID/workspace && mvn clean install -DskipTests -P enterprise'
	   }	
	
	//Below step will be enabled in next release to include security analysis.
	stage ('Insight_PS_IQ') {	
	sh 'mvn com.sonatype.clm:clm-maven-plugin:evaluate -Dclm.applicationId=Insights -P enterprise'
   	}

	stage ('Insight_PS_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java -pl !PlatformUI3 -P enterprise'		
        }		
        
        stage ('Insight_PUI3_CodeAnalysis') {		
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI3 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/app/com/cognizant/devops/platformui/modules -Dsonar.language=js -Dsonar.javascript.file.suffixes=.ts -P enterprise'
	}
		
	stage ('Insight_PS_NexusUpload') {		
		sh 'mvn clean deploy -DskipTests -P enterprise'		
		}
	
	}
	catch (err){
		
	slackSend channel: '#insightsjenkins', color: 'bad', message: "Insights Enterprise Build Failed for commitID - *$gitCommitID*, Branch - *$branchName* \n Build Log can be found @ https://buildon.cogdevops.com/buildon/HistoricCIWebController?commitId=$gitCommitID", teamDomain: "insightscogdevops", token: slackToken
	sh 'exit 1'
	}	
	// Platform Service Ends	   
	   
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
		

		//PlatformAuditEngine version
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformAuditEngine && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformAuditEngine/version"
	    	pomversionAuditEngine=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformAuditEngine/version").trim()  
			
		//PlatformAuditService version
	    	//sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformAuditService && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformAuditService/version"
	   	   // pomversionAuditService=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformAuditService/version").trim()  
		
		
		
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp													
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformUI3 && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformUI3/version"
       		pomUI3version=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI3/version").trim()  
		
		if(pomversionService.contains("SNAPSHOT") && pomversionEngine.contains("SNAPSHOT") && pomversion.contains("SNAPSHOT") && pomUIversion.contains("SNAPSHOT") && pomUI3version.contains("SNAPSHOT")){
		
			NEXUSREPO="https://repo.cogdevops.com/repository/buildonInsightsEnterprise"
		
		} else {
		
		    NEXUSREPO="https://repo.cogdevops.com/repository/InsightsEnterpriseRelease"	
				
		}	
		
		//Platform Service
		PS_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformService/${pomversionService}/PlatformService-${pomversionService}.war"		
		
		//Platform Engine		
		PE_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformEngine/${pomversionEngine}/PlatformEngine-${pomversionEngine}.jar"
		
		//Platform Insights		
		PI_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformInsights/${pomversion}/PlatformInsights-${pomversion}.jar"
		
		//Platform UI 3		
		PUI3_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformUI3/${pomUI3version}/PlatformUI3-${pomUI3version}.zip"
		
		//Platform AuditEngine
		PAE_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformAuditEngine/${pomversionAuditEngine}/PlatformAuditEngine-${pomversionAuditEngine}.jar"
				
		//Platform AuditService
		//PAS_artifact="${NEXUSREPO}/com/cognizant/devops/PlatformAuditService/${pomversionAuditService}/PlatformAuditService-${pomversionAuditService}.war"
	
	
   	    slackSend channel: '#insightsjenkins', color: 'good', message: "New Insights Enterprise artifacts are uploaded to Nexus for commitID : *${env.commitID}* ,Branch - *${env.branchName}* \n *PlatformService* ${PS_artifact} \n *PlatformEngine* ${PE_artifact} \n *PlatformInsights*  ${PI_artifact} \n *PlatformUI3* ${PUI3_artifact} \n *PlatformAuditEngine* ${PAE_artifact}", teamDomain: 'insightscogdevops', token: slackToken // "*" is for making the text bold in slack notification
  	}
}
