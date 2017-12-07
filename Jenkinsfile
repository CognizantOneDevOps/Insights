env.dockerimagename="devopsbasservice/buildonframework:insights"
node {
   // Platform Service Starts
   stage ('Insight_PS_Build') {
        checkout scm
		sh 'mvn clean install -DskipTests'
		buildSuccessPS=true
    }
	
	stage ('Insight_PS_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccessPS=true
    }
	
	stage ('Insight_PS_NexusUpload') {
		sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformService/target/PlatformService.war -DskipTests=true'
		nexusSuccessPS=true
	}
	
	stage ('Deployment_PS_QA_Tomcat') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn tomcat7:undeploy -DskipTests'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn tomcat7:redeploy -DskipTests'
		deploymentSuccessPS=true
	}
	// Platform Service Ends
	
	// Platform Insights Starts
	stage ('Insight_PI_Build') {
        checkout scm
	   	sh 'mvn clean install -DskipTests'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn clean install -DskipTests'
		buildSuccessPI=true
    }
	
	stage ('Insight_PI_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccessPI=true
    }
	
	stage ('Insight_PI_NexusUpload') {
		//sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DskipTests=true'
		sh 'mvn -P NexusUpload deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DskipTests=true
		nexusSuccessPI=true
	}
	
	stage ('Deployment_PI_QA_SparkServer') {
		sh 'chmod +x /var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar && scp -o "StrictHostKeyChecking no" -i /var/jenkins/insights.pem /var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar ubuntu@54.87.224.77:/tmp/'
		sh 'ssh -f -i /var/jenkins/insights.pem ubuntu@54.87.224.77 "ps -ef | grep /tmp/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar | cut -c 10-15 | xargs kill -9 | exit 0"'
		sh 'ssh -f -i  /var/jenkins/insights.pem ubuntu@54.87.224.77  "nohup java -jar /tmp/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar &" '
		deploymentSuccessPI=true
	}
	// Platform Insights Ends
	
	// Platform UI2.0 Starts
	stage ('Insight_PUI2.0_Build') {
        checkout scm
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install --allow-root && tsd install && npm install && grunt'
		buildSuccessUI=true
    }
	
	stage ('Insight_PUI2.0_CodeAnalysis') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccessUI=true
    }
	
	stage ('Insight_PUI2.0_NexusUpload') {
		//sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app -DskipTests=true'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && zip app.zip app'
		sh 'mvn -P NexusUpload deploy:deploy-file -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app.zip -DgroupId="com.cognizant.devops" -DartifactId="PlatformUI2.0" -Dpackaging=zip -Dversion=1.0.0.1-SNAPSHOT -DrepositoryId=nexus -Durl=http://54.209.104.148:8081/nexus/content/repositories/buildonInsights -DskipTests=true
		nexusSuccessPUI2=true
	}
	stage ('Deployment_PUI2.0_App_QA_Tomcat') {
		sh 'scp -r -o "StrictHostKeyChecking no" -i /var/jenkins/insights.pem /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app ec2-user@35.153.180.19:/var/lib/tomcat/webapps/'
		deploymentSuccessUI=true
	}
	// Platform UI2.0 Ends
	stage ('CodeMerge') {
    	//Merge code only if Build succeeds...
    
	   // if (buildSuccessPS == true && codeQualitySuccessPS == true && nexusSuccessPS == true && deploymentSuccessPS == true && buildSuccessPI == true && codeQualitySuccessPI == true && nexusSuccessPI == true && deploymentSuccessPI == true && buildSuccessUI == true && codeQualitySuccessUI == true && nexusSuccessPUI2 ==true && deploymentSuccessUI == true) 
	   // {
	    	//need to create service account and replace the following git config to docker image
		//sh 'git config --global user.email sowmiya.ranganathan@cognizant.com'
		//sh 'git config --global user.name SowmiyaRanganathan'

		//sh 'git checkout finalTest'
		//sh 'git pull origin finalTest'
		//Takes current pull request branchName to merge
		//sh 'git merge origin/$branchName'
		//sh 'git push origin finalTest'
		echo 'Merge can be done'
	    //}
    }
	
}
