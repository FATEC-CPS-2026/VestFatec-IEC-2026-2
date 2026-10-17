pipeline{
    agent any
    
    tools{
        jdk 'JDK21'
        maven 'Maven3'
    }

    triggers{
        pollSCM('H/5 * * * *')
    }
    
    stages{
        stage('Baixar o Código'){
            steps{
                git branch: 'feat-pipeline-jenkins',
                    url: 'https://github.com/FATEC-CPS-2026/VestFatec-IEC-2026-2.git'
            }
        }
        stage('Compilar e Testar'){
            steps{
                script{
                    if(isUnix()){
                        sh 'mvn -B -ntp clean package'
                    }else{
                        bat 'mvn -B ntp clean package'
                    }
                }
            }
            post{
                always{
                    junit testResults: 'target/surefire-reports/TEST-*.xml',
                    allowEmptyResults: false
                }
            }
        }
        stage('Guardar o Build'){
            steps{
                archiveArtifacts artifacts: 'target/*.jar',
                    allowEmptyArchive:false
            }
        }
        
    }
}