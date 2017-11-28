env.dockerimagename="devopsbasservice/buildonframework:insights"
node {
   stage ('Insight_Build') {
   //If some other Repository is to be given apart from current repo, provide git  URL as below.. 
   //git url:'http://IP/root/insights.git'       
   
    checkout scm
    sh 'mvn clean package -DskipTests=True'
    buildSuccess=true
    }
    stage ('CodeMerge') {
    if (buildSuccess == true)
    {
    sh 'git config --global user.email sowmiya.ranganathan@cognizant.com'
    sh 'git config --global user.name SowmiyaRanganathan'
    
    sh 'git checkout latest'
    sh 'git pull origin latest'
    sh 'git merge origin/branchmerging'
    sh 'git push origin latest'
    }
  }
   stage ('Insight_CodeAnalysis') {
    sh 'mvn sonar:sonar -Dmaven.test.failure.ignore=true -DskipTests=true -Dsonar.sources=src/main/java'
   }

   stage ('Insight_NexusUpload') {
    
    sh 'mvn deploy -Dfile=/var/jenkins/jobs/$commitID/workspace/PlatformService/target/PlatformService.war -DskipTests=true'
   }
}
