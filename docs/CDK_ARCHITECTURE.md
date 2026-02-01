# AWS CDK Infrastructure Architecture

This document describes the current AWS CDK infrastructure for the Lumina project, implemented in Java using the AWS CDK v2.

## Overview

The infrastructure is built using **AWS CDK v2.148.1** with **Java 25** and leverages the [Stratospheric CDK Constructs](https://github.com/stratospheric-dev/cdk-constructs) library (v0.1.15) for higher-level abstractions.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AWS Account (us-east-1)                             │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                        Internet                                        │ │
│  └────────────────────────────────┬───────────────────────────────────────┘ │
│                                   │                                         │
│                                   ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                    Application Load Balancer                           │ │
│  │                    (Public Subnets - 3 AZs)                            │ │
│  └────────────────────────────────┬───────────────────────────────────────┘ │
│                                   │                                         │
│                                   ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         VPC                                            │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐  │ │
│  │  │                    Private Subnets (3 AZs)                       │  │ │
│  │  │  ┌────────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │                  ECS Fargate Cluster                       │  │  │ │
│  │  │  │  ┌──────────────────────────────────────────────────────┐  │  │  │ │
│  │  │  │  │           meter-config-service                       │  │  │  │ │
│  │  │  │  │           (Spring Boot Container)                    │  │  │  │ │
│  │  │  │  │           SPRING_PROFILES_ACTIVE=aws                 │  │  │  │ │
│  │  │  │  └──────────────────────────────────────────────────────┘  │  │  │ │
│  │  │  └────────────────────────────────────────────────────────────┘  │  │ │
│  │  └──────────────────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                   │                                         │
│                                   ▼                                         │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                    NAT Gateways → Internet                             │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │   Amazon ECR                        CloudWatch Logs                    │ │
│  │   (meter-config-service)            (Application Logging)              │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Stack Architecture

The infrastructure is organized into **4 independent CDK applications**, each deployed as a separate CloudFormation stack. This modular approach allows for independent deployment and lifecycle management.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DEPLOYMENT ORDER                                  │
│                                                                             │
│   ┌───────────────────┐                                                     │
│   │  1. Bootstrap     │  Initial CDK environment setup                      │
│   │     Stack         │  (One-time setup)                                   │
│   └─────────┬─────────┘                                                     │
│             │                                                               │
│             ▼                                                               │
│   ┌───────────────────┐                                                     │
│   │  2. Docker        │  Creates ECR repository                             │
│   │     Repository    │  for container images                               │
│   │     Stack         │                                                     │
│   └─────────┬─────────┘                                                     │
│             │                                                               │
│             ▼                                                               │
│   ┌───────────────────┐                                                     │
│   │  3. Network       │  VPC, Subnets, ALB,                                 │
│   │     Stack         │  Security Groups, NAT                               │
│   └─────────┬─────────┘                                                     │
│             │                                                               │
│             │ Exports parameters to SSM Parameter Store                     │
│             ▼                                                               │
│   ┌───────────────────┐                                                     │
│   │  4. Service       │  ECS Fargate Service                                │
│   │     Stack         │  (reads Network outputs from SSM)                   │
│   └───────────────────┘                                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Stack Details

### 1. Bootstrap Stack

**File:** `BootstrapApp.java`
**Stack Name:** `Bootstrap`
**Purpose:** Prepares the AWS environment for CDK deployments

```
┌─────────────────────────────────────────┐
│           Bootstrap Stack               │
├─────────────────────────────────────────┤
│  • S3 Bucket for CDK assets             │
│  • IAM Roles for CDK deployment         │
│  • ECR Repository for CDK images        │
└─────────────────────────────────────────┘
```

**Context Variables Required:**
- `region` - AWS region (e.g., "us-east-1")
- `accountId` - AWS account ID

---

### 2. Docker Repository Stack

**File:** `DockerRepositoryApp.java`
**Stack Name:** `{applicationName}-DockerRepository`
**Purpose:** Creates ECR repository for storing container images

```
┌──────────────────────────────────────────┐
│      Docker Repository Stack             │
│   (meter-config-service-DockerRepository)│
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │
│  │         Amazon ECR                 │  │
│  │  ┌──────────────────────────────┐  │  │
│  │  │  Repository:                 │  │  │
│  │  │  meter-config-service        │  │  │
│  │  │                              │  │  │
│  │  │  • Max images: 10            │  │  │
│  │  │  • Mutable tags: true        │  │  │
│  │  └──────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │
│                                          │
└──────────────────────────────────────────┘
```

**Context Variables Required:**
- `accountId` - AWS account ID
- `region` - AWS region
- `applicationName` - Application name (used as repository name)

**Key Configuration:**
```java
new DockerRepository.DockerRepositoryInputParameters(
    applicationName,  // Repository name
    accountId,        // AWS account ID
    10,               // Max image retention count
    false             // Immutable images (false = mutable)
)
```

---

### 3. Network Stack

**File:** `NetworkApp.java`
**Stack Name:** `{environmentName}-Network`
**Purpose:** Creates VPC infrastructure and networking components

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Network Stack (development-Network)                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                              VPC                                   │ │
│  │                         (CIDR: Auto)                               │ │
│  │                                                                    │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │                    PUBLIC SUBNETS                           │   │ │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │   │ │
│  │  │  │   AZ-1a     │  │   AZ-1b     │  │   AZ-1c     │          │   │ │
│  │  │  │             │  │             │  │             │          │   │ │
│  │  │  │ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │          │   │ │
│  │  │  │ │   ALB   │ │  │ │   ALB   │ │  │ │   ALB   │ │          │   │ │
│  │  │  │ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │          │   │ │
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘          │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  │                              │                                     │ │
│  │                    ┌─────────┴─────────┐                           │ │
│  │                    │  Internet Gateway │                           │ │
│  │                    └───────────────────┘                           │ │
│  │                                                                    │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │                   PRIVATE SUBNETS                           │   │ │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │   │ │
│  │  │  │   AZ-1a     │  │   AZ-1b     │  │   AZ-1c     │          │   │ │
│  │  │  │             │  │             │  │             │          │   │ │
│  │  │  │ ┌─────────┐ │  │ ┌─────────┐ │  │ ┌─────────┐ │          │   │ │
│  │  │  │ │   ECS   │ │  │ │   ECS   │ │  │ │   ECS   │ │          │   │ │
│  │  │  │ │  Tasks  │ │  │ │  Tasks  │ │  │ │  Tasks  │ │          │   │ │
│  │  │  │ └─────────┘ │  │ └─────────┘ │  │ └─────────┘ │          │   │ │
│  │  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘          │   │ │
│  │  │         │                │                │                 │   │ │
│  │  │  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐          │   │ │
│  │  │  │ NAT Gateway │  │ NAT Gateway │  │ NAT Gateway │          │   │ │
│  │  │  └─────────────┘  └─────────────┘  └─────────────┘          │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                      Security Groups                               │ │
│  │  • ALB Security Group (allows HTTP/HTTPS from internet)            │ │
│  │  • ECS Security Group (allows traffic from ALB only)               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │              SSM Parameter Store (Output Parameters)               │ │
│  │  Exports: VPC ID, Subnet IDs, ALB ARN, Security Group IDs          │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Context Variables Required:**
- `environmentName` - Environment name (e.g., "development")
- `accountId` - AWS account ID
- `region` - AWS region
- `sslCertificateArn` - (Optional, currently commented out) SSL certificate ARN

**Cross-Stack Communication:**
The Network stack exports its outputs to AWS Systems Manager Parameter Store, allowing the Service stack to retrieve them without tight coupling.

---

### 4. Service Stack

**File:** `ServiceApp.java`
**Stack Name:** `{applicationName}-{environmentName}-Service`
**Purpose:** Deploys the containerized application on ECS Fargate

```
┌─────────────────────────────────────────────────────────────────────────┐
│          Service Stack (meter-config-service-development-Service)       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    ECS Fargate Cluster                             │ │
│  │                                                                    │ │
│  │  ┌──────────────────────────────────────────────────────────────┐  │ │
│  │  │                    Task Definition                           │  │ │
│  │  │  ┌────────────────────────────────────────────────────────┐  │  │ │
│  │  │  │                   Container                            │  │  │ │
│  │  │  │                                                        │  │  │ │
│  │  │  │  Image: 992382386289.dkr.ecr.us-east-1.amazonaws.com/  │  │  │ │
│  │  │  │         meter-config-service:latest                    │  │  │ │
│  │  │  │                                                        │  │  │ │
│  │  │  │  Environment Variables:                                │  │  │ │
│  │  │  │    SPRING_PROFILES_ACTIVE = aws                        │  │  │ │
│  │  │  │                                                        │  │  │ │
│  │  │  │  Health Check Interval: 30 seconds                     │  │  │ │
│  │  │  │                                                        │  │  │ │
│  │  │  └────────────────────────────────────────────────────────┘  │  │ │
│  │  └──────────────────────────────────────────────────────────────┘  │ │
│  │                                                                    │ │
│  │  ┌──────────────────────────────────────────────────────────────┐  │ │
│  │  │                    ECS Service                               │  │ │
│  │  │                                                              │  │ │
│  │  │  • Connected to ALB (from Network stack)                     │  │ │
│  │  │  • Runs in Private Subnets                                   │  │ │
│  │  │  • Auto-scaling enabled                                      │  │ │
│  │  │                                                              │  │ │
│  │  └──────────────────────────────────────────────────────────────┘  │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                     CloudWatch Logs                                │ │
│  │  Log Group: /ecs/meter-config-service-development                  │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Context Variables Required:**
- `environmentName` - Environment name
- `applicationName` - Application name
- `accountId` - AWS account ID
- `springProfile` - Spring Boot profile (e.g., "aws")
- `dockerImageUrl` - Full ECR image URL
- `region` - AWS region

**Key Configuration:**
```java
// Docker image source from ECR
Service.DockerImageSource dockerImageSource =
    new Service.DockerImageSource(dockerImageUrl);

// Read network outputs from Parameter Store
Network.NetworkOutputParameters networkOutputParameters =
    Network.getOutputParametersFromParameterStore(
        serviceStack,
        applicationEnvironment.getEnvironmentName()
    );

// Service configuration
Service.ServiceInputParameters serviceInputParameters =
    new Service.ServiceInputParameters(
        dockerImageSource,
        environmentVariables(springProfile)
    ).withHealthCheckIntervalSeconds(30);
```

## Project Structure

```
infrastructure/
├── src/main/java/com/lumina/cdk/
│   ├── BootstrapApp.java          # CDK bootstrap application
│   ├── DockerRepositoryApp.java   # ECR repository stack
│   ├── NetworkApp.java            # VPC and networking stack
│   ├── ServiceApp.java            # ECS Fargate service stack
│   └── Validations.java           # Input validation utility
├── build.gradle.kts               # Gradle build configuration
├── cdk.json                       # CDK context variables
├── cdk.context.json               # CDK context cache (AZ mapping)
└── package.json                   # npm scripts for deployment
```

## Configuration

### Context Variables (cdk.json)

```json
{
  "context": {
    "applicationName": "meter-config-service",
    "region": "us-east-1",
    "accountId": "992382386289",
    "springProfile": "aws",
    "environmentName": "development",
    "dockerImageUrl": "992382386289.dkr.ecr.us-east-1.amazonaws.com/meter-config-service:latest"
  }
}
```

### Build Configuration (build.gradle.kts)

Key dependencies:
- `dev.stratospheric:cdk-constructs:0.1.15` - Stratospheric CDK constructs library
- `io.soabase.record-builder:record-builder-core:41` - Record builder for Java

Key settings:
- Java 25 with preview features enabled
- Main class specified via `-DmainClass` system property

## Deployment Commands

All deployment commands are defined in `package.json`:

```bash
# One-time bootstrap setup
npm run bootstrap

# Deploy stacks (in order)
npm run repository:deploy   # Deploy ECR repository
npm run network:deploy      # Deploy VPC and networking
npm run service:deploy      # Deploy ECS Fargate service

# Destroy stacks (reverse order)
npm run service:destroy     # Destroy ECS service
npm run network:destroy     # Destroy networking
npm run repository:destroy  # Destroy ECR repository
```

## Data Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           REQUEST FLOW                                   │
│                                                                          │
│   User Request                                                           │
│        │                                                                 │
│        ▼                                                                 │
│   ┌─────────┐                                                            │
│   │ Internet│                                                            │
│   └────┬────┘                                                            │
│        │                                                                 │
│        ▼                                                                 │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                Application Load Balancer                         │   │
│   │                    (HTTP:80 / HTTPS:443)                         │   │
│   │                                                                  │   │
│   │  • Health checks: /actuator/health                               │   │
│   │  • Target Group: ECS Tasks                                       │   │
│   └────────────────────────────┬─────────────────────────────────────┘   │
│                                │                                         │
│                                ▼                                         │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                    Security Group                                │   │
│   │                 (ALB → ECS Traffic Only)                         │   │
│   └────────────────────────────┬─────────────────────────────────────┘   │
│                                │                                         │
│                                ▼                                         │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                   ECS Fargate Tasks                              │   │
│   │               (meter-config-service:8080)                        │   │
│   │                                                                  │   │
│   │  Spring Boot Application                                         │   │
│   │    • Profile: aws                                                │   │
│   │    • Endpoints: REST API                                         │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

## Cross-Stack Communication

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    CROSS-STACK COMMUNICATION                             │
│                                                                          │
│   ┌────────────────────┐                                                 │
│   │    Network Stack   │                                                 │
│   │                    │                                                 │
│   │  Creates:          │                                                 │
│   │  • VPC             │                                                 │
│   │  • Subnets         │──────┐                                          │
│   │  • ALB             │      │                                          │
│   │  • Security Groups │      │                                          │
│   └────────────────────┘      │                                          │
│                               │                                          │
│                               ▼                                          │
│                    ┌──────────────────────┐                              │
│                    │  SSM Parameter Store │                              │
│                    │                      │                              │
│                    │  Stores:             │                              │
│                    │  • VPC ID            │                              │
│                    │  • Subnet IDs        │                              │
│                    │  • ALB ARN           │                              │
│                    │  • Security Group    │                              │
│                    │    IDs               │                              │
│                    └──────────┬───────────┘                              │
│                               │                                          │
│                               │                                          │
│   ┌────────────────────┐      │                                          │
│   │   Service Stack    │      │                                          │
│   │                    │◄─────┘                                          │
│   │  Reads:            │                                                 │
│   │  Network.getOutput │                                                 │
│   │  ParametersFrom    │                                                 │
│   │  ParameterStore()  │                                                 │
│   │                    │                                                 │
│   │  Creates:          │                                                 │
│   │  • ECS Cluster     │                                                 │
│   │  • Task Definition │                                                 │
│   │  • Service         │                                                 │
│   └────────────────────┘                                                 │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

## Stratospheric Constructs Library

The infrastructure uses the [Stratospheric CDK Constructs](https://github.com/stratospheric-dev/cdk-constructs) library which provides opinionated, high-level constructs:

| Construct | Purpose | Wraps |
|-----------|---------|-------|
| `Network` | VPC infrastructure | VPC, Subnets, NAT, ALB, Security Groups |
| `DockerRepository` | Container registry | ECR Repository with lifecycle policies |
| `Service` | Container service | ECS Fargate Cluster, Task Definition, Service |
| `ApplicationEnvironment` | Naming helper | Consistent stack/resource naming |

## Validation Pattern

All CDK applications use a common validation utility to ensure required context variables are provided:

```java
public class Validations {
  public static void requireNonEmpty(String string, String message) {
    if (string == null || string.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
```

This ensures fast failure with clear error messages if configuration is missing.

## AWS Services Summary

| Service | Stack | Purpose |
|---------|-------|---------|
| Amazon ECR | DockerRepository | Container image storage |
| Amazon VPC | Network | Isolated network environment |
| Application Load Balancer | Network | Traffic distribution |
| NAT Gateway | Network | Outbound internet for private subnets |
| Internet Gateway | Network | Public internet access |
| Security Groups | Network | Network access control |
| Amazon ECS | Service | Container orchestration |
| AWS Fargate | Service | Serverless container compute |
| CloudWatch Logs | Service | Application logging |
| SSM Parameter Store | Network/Service | Cross-stack parameter sharing |
