env.dockerimagename="devopsbasservice/buildonframework:boins"
node {
   stage ('Insight_Build') {
        checkout scm
		sh 'mvn clean install -DskipTests'
		buildSuccess=true
    }
	
	stage ('Insight_CodeAnalysis') {
		sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
		codeQualitySuccess=true
    }
	
	stage ('Insight_NexusUpload') {
		sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformService/target/PlatformService.war -DskipTests=true'
		nexusSuccess=true
	}
	
	stage ('Deployment_QA') {
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn tomcat7:undeploy -DskipTests'
		sh 'cd /var/jenkins/jobs/$commitID/workspace/PlatformService && mvn tomcat7:redeploy -DskipTests'
		deploymentSuccess=true
	}
	
	stage ('CodeMerge') {
    //Merge code only if Build succeeds..
    
    if (buildSuccess == true && codeQualitySuccess == true && nexusSuccess == true && deploymentSucess == true)
    {
    echo 'CodeMerge can be done'
    }
    }
}
