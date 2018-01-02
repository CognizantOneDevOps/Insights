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
		//sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn -P NexusUpload deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DskipTests=true'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn -P NexusUpload deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DgroupId="com.cognizant.devops" -DartifactId="PlatformInsights" -Dpackaging=jar -Dversion=1.0.0.1-SNAPSHOT -DrepositoryId=nexus -Durl=http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights -DskipTests=true'
	}
	
	// Platform Insights Ends
	
	// Platform UI2.0 Starts
	stage ('Insight_PUI2.0_Build') {
        
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install --allow-root && tsd install && npm install && grunt'
	}
	
	stage ('Insight_PUI2.0_CodeAnalysis') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
	}
	
	stage ('Insight_PUI2.0_NexusUpload') {
		//sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app -DskipTests=true'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && zip -r app.zip app'
		sh 'mvn -P NexusUpload deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app.zip -DgroupId="com.cognizant.devops" -DartifactId="PlatformUI2.0" -Dpackaging=zip -Dversion=1.0.0.1-SNAPSHOT -DrepositoryId=nexus -Durl=http://insightsplatformnexusrepo.cogdevops.com:8001/nexus/content/repositories/buildonInsights -DskipTests=true'
	}
	
	// Platform UI2.0 Ends
	stage ('SlackNotification') {
   	    slackSend channel: '#insightsjenkins', color: 'good', message: "New Insights artifacts are uploaded to Nexus for commitID ${env.commitID}", teamDomain: 'ctsdevopsbot', token: slackToken
  	}

}
