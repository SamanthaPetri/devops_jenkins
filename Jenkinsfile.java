pipeline {
  agent any
    
  tools {nodejs "node"}
    
  stages {
        
    stage('Git') {
      steps {
        git 'https://github.com/SamanthaPetri/Noisetracker-Website.git'
      }
    }
     
    stage('Build') {
      steps {
        bat 'npm install'
        archiveArtifacts artifacts: 'build/libs/**/*.jar', allowEmptyArchive: true, onlyIfSuccessful: true
      }
    }

    stage('Test') {
      steps {
        bat 'npm selenium.js'
      }
    }

    stage('SonarQube Analysis') {
        steps {
        withSonarQubeEnv('SonarQube') {
            bat '"C:/Users/Sammy/sonar-scanner-cli-4.8.0.2856-windows/sonar-scanner-4.8.0.2856-windows/bin/sonar-scanner.bat" -Dsonar.projectKey=<YOUR_PROJECT_KEY> -Dsonar.host.url=http://localhost:9000/ -Dsonar.login=<SONAR_KEY>'
            }
        }
    }

    stage('Deploy to Elastic Beanstalk') {
        steps {
            bat 'aws configure set aws_access_key_id <YOUR_ACCESS_KEY>'
            bat 'aws configure set aws_secret_access_key <YOUR_SECRET_KEY>'
            bat 'aws elasticbeanstalk create-application-version --application-name sit --version-label 1.5"'
            }
        }

    stage('Deploy to Octopus') {
            steps {
                bat 'octo create-release --project=sit --packageVersion=1.0 --server=https://petri.octopus.app --apiKey=<YOUR_API_KEY>'
                bat 'octo deploy-release --project=sit --releaseNumber=0.0.9 --deployTo=Production --server=https://petri.octopus.app --apiKey=<YOUR_API_KEY>'
            }
        }

  stage('Monitor in Datadog') {
            steps {
                bat '''
                    setlocal EnableDelayedExpansion

                    set datadogAPIKey=<YOUR_API_KEY>
                    set metricValue=123.45
                    set metricName=custom.metric.name

                    rem Send a custom metric to Datadog
                    curl -X POST -H "Content-type: application/json" -d "{""series"": [{""metric"": ""!metricName!"", ""points"": [[!date /t! !time /t!], !metricValue!], ""type"": ""gauge"", ""host"": ""my-hostname""}]}" "https://api.us5.datadoghq.com/api/v1/series?api_key=!datadogAPIKey!"

                    rem Send an event to Datadog
                    curl -X POST -H "Content-type: application/json" -d "{""title"": ""My Event"", ""text"": ""This is a sample event."", ""tags"": [""tag1"", ""tag2""]}" "https://api.us5.datadoghq.com/api/v1/events?api_key=!datadogAPIKey!"
                '''
            }
        }
    
    }
}