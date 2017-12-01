env.dockerimagename="devopsbasservice/buildonframework:boins2"
node {
   stage ('Insight_Build') {
        checkout scm
	   	sh 'mvn clean install -DskipTests'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformInsights && mvn clean install -DskipTests'
		buildSuccess=true
    }
	
	stage ('Insight_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccess=true
    }
	
	stage ('Insight_NexusUpload') {
		sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DskipTests=true'
		nexusSuccess=true
	}
	
	stage ('Deployment_SparkServer_QA') {
		sh 'chmod +x /var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar && scp -o "StrictHostKeyChecking no" -i /var/jenkins/insights.pem /var/jenkins/jobs/$commitID/workspace/PlatformInsights/target/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar ubuntu@54.87.224.77:/tmp/'
		sh 'ssh -i /var/jenkins/insights.pem ubuntu@54.87.224.77 "kill $(ps -ef | grep PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar | awk '{ print $2}' | head -1)"'
		sh 'ssh -f -i  /var/jenkins/insights.pem ubuntu@54.87.224.77  "nohup java -jar /tmp/PlatformInsights-0.0.1-SNAPSHOT-jar-with-dependencies.jar &" '
		deploymentSuccess=true
	}
	
	stage ('CodeMerge') {
	    //Merge code only if Build succeeds..

	    if (buildSuccess == true && codeQualitySuccess == true && nexusSuccess == true && deploymentSuccess == true)
	    {
	    echo 'CodeMerge can be done'
	    }
	    }
}
