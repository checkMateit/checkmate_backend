pipeline {
  agent any

  environment {
    ORG = "checkMateit"                 // GitHub org
    REGISTRY = "ghcr.io/${ORG}"         // GHCR org registry
    IMAGE_TAG = "${env.BUILD_NUMBER}"   // 태그 전략 (원하면 git sha로 바꿔도 됨)
  }

  stages {
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

          // HEAD~1 존재 여부를 returnStatus 로 체크 (0이면 존재)
          def hasPrevCommit = (sh(script: 'git rev-parse --verify HEAD~1 >/dev/null 2>&1', returnStatus: true) == 0)

          if (!hasPrevCommit) {
            echo "No previous commit detected (first build). Building ALL services."
            env.CHANGED_SERVICES = allServices.join(" ")
            env.BUILD_ALL = "true"
          } else {
            def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
            echo "Changed files:\n${changedFiles}"

            def changed = []
            def lines = changedFiles ? changedFiles.readLines() : []

            for (svc in allServices) {
              if (lines.any { it.startsWith("${svc}/") }) {
                changed << svc
              }
            }

            if (changed.isEmpty()) {
              echo "No service changes detected. Building ALL services (bootstrap build)."
              env.CHANGED_SERVICES = allServices.join(" ")
              env.BUILD_ALL = "true"
            } else {
              echo "Changed services: ${changed}"
              env.CHANGED_SERVICES = changed.join(" ")
              env.BUILD_ALL = "false"
            }
          }
        }
      }
    }

    stage('Login to GHCR') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'ghcr-credentials',
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
      // 필요시 로컬 디스크 정리(에이전트가 노드에 남는 환경이면 유용)
      sh 'docker image prune -f || true'
    }
    success {
      echo "Pipeline SUCCESS"
    }
    failure {
      echo "Pipeline FAILED"
    }
  }
}