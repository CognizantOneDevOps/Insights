env.dockerimagename="devopsbasservice/buildonframework:insights1.0"
node {
   // Platform Service Starts.
   stage ('Insight_PS_Build') {
        checkout scm
		sh 'mvn clean install -DskipTests'
	   }
	
	stage ('Insight_PS_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		
    }
	
	stage ('Insight_PS_NexusUpload') {
		sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformService/target/PlatformService.war -DskipTests=true'
		
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp
	       sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn -B help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformService/version"
	       pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/version").trim()  //Get version from pom.xml to form the nexus repo URL
		sh 'sleep 3m'
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
		echo "******************** printing pomversion ${pomversion} ************************"
		echo "******************printing curl command http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformService/${pomversion}/maven-metadata.xml ******"
		sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformService/${pomversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.war/' > /var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact"
		PS_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformService/PS_artifact").trim()
		PS_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformService/${pomversion}/${PS_artifactName}"
	}
	
	// Platform Service Ends
	
	// Platform Insights Starts 
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
    	} // Platform Insights Ends
	
	// Platform UI2.0 Starts 
	stage ('Insight_PUI2.0_Build') {
        
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install --allow-root && tsd install && npm install && grunt'
	}
	
	stage ('Insight_PUI2.0_CodeAnalysis') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
	}
	
	stage ('Insight_PUI2.0_NexusUpload') {
	        
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn -B help:evaluate -Dexpression=project.version | grep -e "^[^[]" > version && zip -r app.zip app'
	        pomversion = readFile 'version'
		sh 'mvn -P NexusUpload deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app.zip -DgroupId="com.cognizant.devops" -DartifactId="PlatformUI2.0" -Dpackaging=zip -Dversion=${pomversion} -DrepositoryId=nexus -Durl=http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights -DskipTests=true'
		
		//Framing Nexus URL for artifact uploaded to Nexus with unique timestamp													
		sh "cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version"
       		pomversion=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/version").trim()   //Get version from pom.xml to form the nexus repo URL
       		
		//get artifact info (artifactID,classifier,timestamp, buildnumber,version) from maven-metadata.xml
	   	sh "curl -s http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformUI2.0/${pomversion}/maven-metadata.xml  | grep -oP '(?<=<artifactId>).*?(?=</artifactId>)|(?<=<version>).*?(?=</version>)|(?<=<timestamp>).*?(?=</timestamp>)|(?<=<buildNumber>).*?(?=</buildNumber>)|(?<=<classifier>).*?(?=</classifier>)' | paste -sd- - | sed 's/-SNAPSHOT//g' | sed 's/--/-/g' | sed 's/\$/.zip/' > /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact"
		PUI_artifactName=readFile("/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/PUI_artifact").trim()
		PUI_artifact="http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights/com/cognizant/devops/PlatformUI2.0/${pomversion}/${PUI_artifactName}"
	   } // Platform UI2.0 Ends

        //Send Notification Slack Channel
	stage ('SlackNotification') {
   	    slackSend channel: '#insightsjenkins', color: 'good', message: "New Insights artifacts are uploaded to Nexus for commitID ${env.commitID} - ${PS_artifact} , ${PI_artifact} , ${PUI_artifact}", teamDomain: 'ctsdevopsbot', token: slackToken
  	}
}
