env.dockerimagename="devopsbasservice/buildonframework:boins3"
node {
   stage ('Insight_Build') {
        checkout scm
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0 && bower install && tsd install && npm install && grunt'
		buildSuccess=true
    }
	
	stage ('Insight_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccess=true
    }
	
	
	stage ('Deployment_App_Tomcat') {
		sh 'ssh -f -i  /var/jenkins/insights.pem ec2-user@35.153.180.19  "systemctl stop tomcat"'
		sh 'scp -o "StrictHostKeyChecking no" -i /var/jenkins/insights.pem /var/jenkins/jobs/$commitID/workspace/PlatformUI2.0/app ec2-user@35.153.180.19:/var/lib/tomcat/webapps/app'
		sh 'ssh -f -i  /var/jenkins/insights.pem ec2-user@35.153.180.19  "systemctl start tomcat"'
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
