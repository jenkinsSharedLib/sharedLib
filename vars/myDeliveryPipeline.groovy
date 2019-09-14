def call() {
    
    pipeline {
        agent any
        stages {      
            stage('Clean Lifcycle') {
                steps {
                    script {
                        props = readProperties  file:'user.properties'
                        sh "mvn ${props['mavenClean']}" 
                    }    
                }
            }        
            stage('Build Lifecycle') {
                steps {
                    script {                      
                        sh "mvn ${props['mavenCompile']}"
                   }              
                }
            }
         
            stage ('Unit Test') {
                when { 
                    expression {
                        script {
                            echo "I am entering unit test expression"
                            if("${props['runUnitTestAsGoal']}" == "true") 
                                return true
                        } 
                    }
                }
                steps {
                    script {
                        sh "mvn ${props['mavenTest']}"
                }
            }
         }
            stage ('Package Creation') {
                steps {
                    script {
                        sh "mvn ${props['mavenPackage']}"
                   } 
                }
            }
            
            stage('sonar code quality'){
                when { 
                    expression {
                        script {
                            echo "I am entering sonar expression"
                            if("${props['runSonarAsGoal']}" == "true") 
                                return true
                        } 
                    }
                }
                steps {
                    script {
                            sh """
                            mvn sonar:sonar \
                            -Dsonar.projectKey="${props['sonarProjectKey']}" \
                            -Dsonar.host.url="${props['sonarUrl']}" \
                            -Dsonar.login="${props['sonarLogin']}" \
                            -Dsonar.projectName="${props['sonarProjectName']}"
                             """ 
                    }
                }
            }            
            
            stage ('Publish Artifacts') {
                when { 
                    expression {
                        script {
                            echo "I am entering publish expression"
                            if("${props['runDeployAsGoal']}" == "true") 
                                return true
                        } 
                    }
                }
                steps {
                     script {
                            sh "mvn ${props['mavenDeploy']}" 
                   } 
                }
            }
            stage ('send_input_start')
             {
                 jobtype = "build"
                 branch = "dev"
                 application = "My Application"
                 component = "My Component"
                 release = "My Release"
                 jstatus = "Started"
                 jresult = "Building"
                 msg = "BNum="+BUILD_NUMBER+",JName="+JOB_NAME+",AppName="+application+",CompName="+component+",BranchName="+branch+",RelNumber="+release+",JobStatus="+jstatus+",JobResult="+jresult+",JType="+jobtype
                 splunkins.send(msg)
             }
        }
        post {
            always {
               script {
                    mail(body: 
                    """
                    JOB NAME: ${env.JOB_NAME}
                    BUILD NUMBER: ${env.BUILD_NUMBER}. 
                    BUILD STATUS: ${currentBuild.result}.
                    To get more details, visit the build results page: ${env.BUILD_URL}.""",
                    cc: "${props['ccEmail']}",
                    subject: "Jenkins Build Status: ${currentBuild.result}",
                    to: "${props['toEmail']}")
             }
           }
        }
    }
}
