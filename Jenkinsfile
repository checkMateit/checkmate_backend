pipeline {
  agent {
    kubernetes {
      label 'docker'
      defaultContainer 'docker'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:27-cli
    command: ["cat"]
    tty: true
    env:
    - name: DOCKER_HOST
      value: tcp://localhost:2375

  - name: dind
    image: docker:27-dind
    securityContext:
      privileged: true
    args:
    - --host=tcp://0.0.0.0:2375
    - --host=unix:///var/run/docker.sock
"""
    }
  }

  environment {
    ORG = "checkMateit"
    REGISTRY = "ghcr.io/${ORG}"
    IMAGE_TAG = "${env.BUILD_NUMBER}"
  }

  stages {

    stage('Docker test') {
      steps {
        sh 'docker version'
        sh 'docker info'
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Detect changed services') {
      steps {
        script {
          def allServices = [
            "gateway-service",
            "user-service",
            "community-service",
            "store-service",
            "study-service",
            "eureka-service"
          ]

          def hasPrevCommit = (sh(script: 'git rev-parse --verify HEAD~1 >/dev/null 2>&1', returnStatus: true) == 0)

          if (!hasPrevCommit) {
            echo "No previous commit detected (first build). Building ALL services."
            env.CHANGED_SERVICES = allServices.join(" ")
          } else {
            def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
            echo "Changed files:\n${changedFiles}"

            def lines = changedFiles ? changedFiles.readLines() : []
            def changed = []

            for (svc in allServices) {
              if (lines.any { it.startsWith("${svc}/") }) {
                changed << svc
              }
            }

            if (changed.isEmpty()) {
              echo "No service changes detected. Building ALL services (bootstrap build)."
              env.CHANGED_SERVICES = allServices.join(" ")
            } else {
              echo "Changed services: ${changed}"
              env.CHANGED_SERVICES = changed.join(" ")
            }
          }
        }
      }
    }

    stage('Login to GHCR') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'github-credentials',
          usernameVariable: 'GITHUB_USER',
          passwordVariable: 'GITHUB_TOKEN'
        )]) {
          sh '''
            echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USER --password-stdin
          '''
        }
      }
    }

    stage('Build & Push Images') {
      steps {
        script {
          def services = env.CHANGED_SERVICES.split("\\s+")

          for (svc in services) {
            def imageName = "${REGISTRY}/checkmate-${svc.replace('-service','')}:${IMAGE_TAG}"

            sh """
              echo "=== Building ${svc} -> ${imageName} ==="
              docker build --build-arg SERVICE=${svc} -t ${imageName} .
              docker push ${imageName}
            """
          }
        }
      }
    }

    stage('Done') {
      steps {
        echo "Build/Push done. ArgoCD Image Updater will update tags (write-back) if configured."
      }
    }
  }

  post {
    always {
      sh 'docker image prune -f || true'
    }
    success { echo "Pipeline SUCCESS" }
    failure { echo "Pipeline FAILED" }
  }
}