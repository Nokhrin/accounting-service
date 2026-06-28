pipeline {
    agent any
    
    environment {
        JAVA_HOME = tool name: 'JDK25', type: 'jdk'
        PATH = '${JAVA_HOME}/bin:${env.PATH}'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/Nokhrin/accounting-service.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    allure results: [[path: 'target/allure-results']]
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify'
            }
        }
        
        stage('Coverage Report') {
            steps {
                sh 'mvn jacoco:report'
                jacoco execPattern: 'target/jacoco.exec'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    docker.build('accounting-service:${env.BUILD_ID}')
                }
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}