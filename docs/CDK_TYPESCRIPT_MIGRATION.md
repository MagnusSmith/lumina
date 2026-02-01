# AWS CDK Migration Guide: Java to TypeScript

This document outlines the approach for migrating the Lumina AWS CDK infrastructure from Java to TypeScript.

## Why Migrate to TypeScript?

| Aspect | Java CDK | TypeScript CDK |
|--------|----------|----------------|
| **First-class support** | Translated via JSII | Native implementation |
| **Community** | Smaller | Largest CDK community |
| **Documentation** | Examples often need translation | Most examples in TypeScript |
| **Iteration speed** | Compile step required | Fast hot-reload |
| **Type inference** | Verbose | Concise with inference |
| **npm ecosystem** | Separate build (Gradle) | Single build system |
| **IDE support** | Good | Excellent autocomplete |

## Migration Strategy Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MIGRATION PHASES                                     │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │  Phase 1: PREPARATION                                                │   │
│  │  • Set up TypeScript CDK project structure                           │   │
│  │  • Evaluate Stratospheric constructs alternative or recreation       │   │
│  │  • Document current stack outputs and dependencies                   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Phase 2: PARALLEL IMPLEMENTATION                                   │    │
│  │  • Implement TypeScript versions of all stacks                      │    │
│  │  • Maintain same stack names and resource logical IDs               │    │
│  │  • Test in isolated environment                                     │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Phase 3: VALIDATION                                                │    │
│  │  • Run cdk diff against existing stacks                             │    │
│  │  • Ensure synthesized templates match                               │    │
│  │  • Perform integration testing                                      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  Phase 4: CUTOVER                                                   │    │
│  │  • Deploy TypeScript stacks to production                           │    │
│  │  • Remove Java infrastructure code                                  │    │
│  │  • Update CI/CD pipelines                                           │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Phase 1: Preparation

### 1.1 Set Up TypeScript Project Structure

Create a new TypeScript CDK project alongside the existing Java code:

```
infrastructure/
├── src/main/java/...       # Existing Java code (keep during migration)
├── build.gradle.kts        # Existing Gradle config
├── cdk.json               # Update for TypeScript
├── package.json           # Update with TypeScript deps
├── tsconfig.json          # NEW: TypeScript config
├── jest.config.js         # NEW: Test config
├── bin/                   # NEW: TypeScript entry points
│   ├── bootstrap.ts
│   ├── docker-repository.ts
│   ├── network.ts
│   └── service.ts
├── lib/                   # NEW: Stack definitions
│   ├── bootstrap-stack.ts
│   ├── docker-repository-stack.ts
│   ├── network-stack.ts
│   ├── service-stack.ts
│   └── constructs/        # Custom constructs
│       └── validations.ts
└── test/                  # NEW: Tests
    ├── bootstrap.test.ts
    ├── docker-repository.test.ts
    ├── network.test.ts
    └── service.test.ts
```

### 1.2 Initialize TypeScript CDK

```bash
# In infrastructure directory
npm init -y
npm install aws-cdk-lib constructs
npm install -D typescript ts-node @types/node jest ts-jest @types/jest
npx tsc --init
```

### 1.3 Stratospheric Constructs Decision

The current Java implementation relies on `dev.stratospheric:cdk-constructs:0.1.15`. You have two options:

**Option A: Check for TypeScript Version**
```bash
npm search stratospheric
# Check if there's a TypeScript/JavaScript version
```

**Option B: Recreate Constructs (Recommended)**

Since Stratospheric constructs are relatively simple wrappers, recreating them gives you:
- Full control over the implementation
- No external dependencies
- Customization opportunities

```
┌─────────────────────────────────────────────────────────────────────────┐
│                STRATOSPHERIC CONSTRUCT MAPPING                          │
│                                                                         │
│   Java (Stratospheric)              TypeScript (Native)                 │
│   ────────────────────              ─────────────────────               │
│                                                                         │
│   Network                     →     Custom NetworkConstruct             │
│   └─ Creates VPC, ALB, etc.         └─ Use aws-cdk-lib/aws-ec2          │
│                                         aws-cdk-lib/aws-elasticloadbalan│
│                                                                         │
│   DockerRepository            →     ecr.Repository                      │
│   └─ Creates ECR                    └─ Native CDK construct             │
│                                                                         │
│   Service                     →     ecs_patterns.ApplicationLoad        │
│   └─ Creates ECS Fargate            BalancedFargateService              │
│                                     └─ Higher-level CDK pattern         │
│                                                                         │
│   ApplicationEnvironment      →     Simple TypeScript class/interface   │
│   └─ Naming helper                                                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Phase 2: Parallel Implementation

### 2.1 Configuration (tsconfig.json)

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020"],
    "declaration": true,
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "noImplicitThis": true,
    "alwaysStrict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noImplicitReturns": true,
    "noFallthroughCasesInSwitch": false,
    "inlineSourceMap": true,
    "inlineSources": true,
    "experimentalDecorators": true,
    "strictPropertyInitialization": false,
    "outDir": "dist",
    "rootDir": "."
  },
  "include": ["bin/**/*", "lib/**/*", "test/**/*"],
  "exclude": ["node_modules", "cdk.out"]
}
```

### 2.2 Update cdk.json for TypeScript

```json
{
  "app": "npx ts-node bin/service.ts",
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

### 2.3 Stack Conversion Examples

#### Validations Utility

**Java (Original):**
```java
public class Validations {
  public static void requireNonEmpty(String string, String message) {
    if (string == null || string.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
```

**TypeScript:**
```typescript
// lib/constructs/validations.ts
export function requireNonEmpty(value: string | undefined, message: string): asserts value is string {
  if (!value || value.trim() === '') {
    throw new Error(message);
  }
}

// Type-safe context getter
export function getRequiredContext(app: cdk.App, key: string): string {
  const value = app.node.tryGetContext(key);
  requireNonEmpty(value, `context variable '${key}' must not be null`);
  return value;
}
```

---

#### Bootstrap Stack

**Java (Original):**
```java
public class BootstrapApp {
  public static void main(final String[] args) {
    App app = new App();
    String region = (String) app.getNode().tryGetContext("region");
    Validations.requireNonEmpty(region, "context variable 'region' must not be null");
    String accountId = (String) app.getNode().tryGetContext("accountId");
    Validations.requireNonEmpty(accountId, "context variable 'accountId' must not be null");
    Environment awsEnvironment = makeEnv(accountId, region);
    new Stack(app, "Bootstrap", StackProps.builder().env(awsEnvironment).build());
    app.synth();
  }
}
```

**TypeScript:**
```typescript
// bin/bootstrap.ts
#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { getRequiredContext } from '../lib/constructs/validations';

const app = new cdk.App();

const region = getRequiredContext(app, 'region');
const accountId = getRequiredContext(app, 'accountId');

new cdk.Stack(app, 'Bootstrap', {
  env: {
    account: accountId,
    region: region,
  },
});

app.synth();
```

---

#### Docker Repository Stack

**Java (Original):**
```java
public class DockerRepositoryApp {
  public static void main(final String[] args) {
    App app = new App();
    // ... context variables ...
    DockerRepository dockerRepository = new DockerRepository(
        dockerRepositoryStack,
        "DockerRepository",
        awsEnvironment,
        new DockerRepository.DockerRepositoryInputParameters(
            applicationName, accountId, 10, false));
    app.synth();
  }
}
```

**TypeScript:**
```typescript
// lib/docker-repository-stack.ts
import * as cdk from 'aws-cdk-lib';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import { Construct } from 'constructs';

export interface DockerRepositoryStackProps extends cdk.StackProps {
  applicationName: string;
  maxImageCount: number;
  imageTagMutability?: boolean;
}

export class DockerRepositoryStack extends cdk.Stack {
  public readonly repository: ecr.Repository;

  constructor(scope: Construct, id: string, props: DockerRepositoryStackProps) {
    super(scope, id, props);

    this.repository = new ecr.Repository(this, 'DockerRepository', {
      repositoryName: props.applicationName,
      imageScanOnPush: true,
      imageTagMutability: props.imageTagMutability
        ? ecr.TagMutability.IMMUTABLE
        : ecr.TagMutability.MUTABLE,
      lifecycleRules: [
        {
          maxImageCount: props.maxImageCount,
          description: 'Limit number of images',
        },
      ],
      removalPolicy: cdk.RemovalPolicy.RETAIN,
    });

    // Output the repository URI
    new cdk.CfnOutput(this, 'RepositoryUri', {
      value: this.repository.repositoryUri,
      exportName: `${props.applicationName}-repository-uri`,
    });
  }
}

// bin/docker-repository.ts
#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { DockerRepositoryStack } from '../lib/docker-repository-stack';
import { getRequiredContext } from '../lib/constructs/validations';

const app = new cdk.App();

const accountId = getRequiredContext(app, 'accountId');
const region = getRequiredContext(app, 'region');
const applicationName = getRequiredContext(app, 'applicationName');

new DockerRepositoryStack(app, 'DockerRepositoryStack', {
  stackName: `${applicationName}-DockerRepository`,
  env: {
    account: accountId,
    region: region,
  },
  applicationName: applicationName,
  maxImageCount: 10,
  imageTagMutability: false,
});

app.synth();
```

---

#### Network Stack

**TypeScript:**
```typescript
// lib/network-stack.ts
import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as ssm from 'aws-cdk-lib/aws-ssm';
import { Construct } from 'constructs';

export interface NetworkStackProps extends cdk.StackProps {
  environmentName: string;
  sslCertificateArn?: string;
}

export class NetworkStack extends cdk.Stack {
  public readonly vpc: ec2.Vpc;
  public readonly alb: elbv2.ApplicationLoadBalancer;
  public readonly ecsSecurityGroup: ec2.SecurityGroup;

  constructor(scope: Construct, id: string, props: NetworkStackProps) {
    super(scope, id, props);

    // Create VPC with public and private subnets
    this.vpc = new ec2.Vpc(this, 'Vpc', {
      maxAzs: 3,
      natGateways: 3,
      subnetConfiguration: [
        {
          name: 'Public',
          subnetType: ec2.SubnetType.PUBLIC,
          cidrMask: 24,
        },
        {
          name: 'Private',
          subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
          cidrMask: 24,
        },
      ],
    });

    // Create ALB Security Group
    const albSecurityGroup = new ec2.SecurityGroup(this, 'AlbSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for ALB',
      allowAllOutbound: true,
    });
    albSecurityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      'Allow HTTP'
    );
    albSecurityGroup.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(443),
      'Allow HTTPS'
    );

    // Create Application Load Balancer
    this.alb = new elbv2.ApplicationLoadBalancer(this, 'Alb', {
      vpc: this.vpc,
      internetFacing: true,
      securityGroup: albSecurityGroup,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
    });

    // Create ECS Security Group
    this.ecsSecurityGroup = new ec2.SecurityGroup(this, 'EcsSecurityGroup', {
      vpc: this.vpc,
      description: 'Security group for ECS tasks',
      allowAllOutbound: true,
    });
    this.ecsSecurityGroup.addIngressRule(
      albSecurityGroup,
      ec2.Port.allTcp(),
      'Allow traffic from ALB'
    );

    // Store outputs in Parameter Store for cross-stack reference
    this.storeParameters(props.environmentName);
  }

  private storeParameters(environmentName: string): void {
    const prefix = `/${environmentName}/network`;

    new ssm.StringParameter(this, 'VpcIdParam', {
      parameterName: `${prefix}/vpc-id`,
      stringValue: this.vpc.vpcId,
    });

    new ssm.StringParameter(this, 'AlbArnParam', {
      parameterName: `${prefix}/alb-arn`,
      stringValue: this.alb.loadBalancerArn,
    });

    new ssm.StringParameter(this, 'EcsSecurityGroupIdParam', {
      parameterName: `${prefix}/ecs-security-group-id`,
      stringValue: this.ecsSecurityGroup.securityGroupId,
    });

    // Store private subnet IDs
    this.vpc.privateSubnets.forEach((subnet, index) => {
      new ssm.StringParameter(this, `PrivateSubnetParam${index}`, {
        parameterName: `${prefix}/private-subnet-${index}`,
        stringValue: subnet.subnetId,
      });
    });
  }
}

// bin/network.ts
#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { NetworkStack } from '../lib/network-stack';
import { getRequiredContext } from '../lib/constructs/validations';

const app = new cdk.App();

const environmentName = getRequiredContext(app, 'environmentName');
const accountId = getRequiredContext(app, 'accountId');
const region = getRequiredContext(app, 'region');

new NetworkStack(app, 'NetworkStack', {
  stackName: `${environmentName}-Network`,
  env: {
    account: accountId,
    region: region,
  },
  environmentName: environmentName,
});

app.synth();
```

---

#### Service Stack

**TypeScript:**
```typescript
// lib/service-stack.ts
import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecs_patterns from 'aws-cdk-lib/aws-ecs-patterns';
import * as ssm from 'aws-cdk-lib/aws-ssm';
import { Construct } from 'constructs';

export interface ServiceStackProps extends cdk.StackProps {
  environmentName: string;
  applicationName: string;
  dockerImageUrl: string;
  springProfile: string;
  healthCheckIntervalSeconds?: number;
}

export class ServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: ServiceStackProps) {
    super(scope, id, props);

    // Read network parameters from Parameter Store
    const prefix = `/${props.environmentName}/network`;

    const vpcId = ssm.StringParameter.valueFromLookup(
      this,
      `${prefix}/vpc-id`
    );

    const vpc = ec2.Vpc.fromLookup(this, 'Vpc', {
      vpcId: vpcId,
    });

    const ecsSecurityGroupId = ssm.StringParameter.valueFromLookup(
      this,
      `${prefix}/ecs-security-group-id`
    );

    const securityGroup = ec2.SecurityGroup.fromSecurityGroupId(
      this,
      'EcsSecurityGroup',
      ecsSecurityGroupId
    );

    // Create ECS Cluster
    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc: vpc,
      clusterName: `${props.applicationName}-${props.environmentName}`,
    });

    // Create Fargate Service with ALB
    const fargateService = new ecs_patterns.ApplicationLoadBalancedFargateService(
      this,
      'Service',
      {
        cluster: cluster,
        serviceName: props.applicationName,
        desiredCount: 1,
        taskImageOptions: {
          image: ecs.ContainerImage.fromRegistry(props.dockerImageUrl),
          containerPort: 8080,
          environment: {
            SPRING_PROFILES_ACTIVE: props.springProfile,
          },
        },
        publicLoadBalancer: true,
        securityGroups: [securityGroup],
      }
    );

    // Configure health check
    fargateService.targetGroup.configureHealthCheck({
      path: '/actuator/health',
      interval: cdk.Duration.seconds(props.healthCheckIntervalSeconds ?? 30),
      healthyThresholdCount: 2,
      unhealthyThresholdCount: 3,
    });

    // Output the service URL
    new cdk.CfnOutput(this, 'ServiceUrl', {
      value: `http://${fargateService.loadBalancer.loadBalancerDnsName}`,
      description: 'Service URL',
    });
  }
}

// bin/service.ts
#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { ServiceStack } from '../lib/service-stack';
import { getRequiredContext } from '../lib/constructs/validations';

const app = new cdk.App();

const environmentName = getRequiredContext(app, 'environmentName');
const applicationName = getRequiredContext(app, 'applicationName');
const accountId = getRequiredContext(app, 'accountId');
const springProfile = getRequiredContext(app, 'springProfile');
const dockerImageUrl = getRequiredContext(app, 'dockerImageUrl');
const region = getRequiredContext(app, 'region');

new ServiceStack(app, 'ServiceStack', {
  stackName: `${applicationName}-${environmentName}-Service`,
  env: {
    account: accountId,
    region: region,
  },
  environmentName: environmentName,
  applicationName: applicationName,
  dockerImageUrl: dockerImageUrl,
  springProfile: springProfile,
  healthCheckIntervalSeconds: 30,
});

app.synth();
```

### 2.4 Update package.json Scripts

```json
{
  "name": "meter-config-service-cdk",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "build": "tsc",
    "watch": "tsc -w",
    "test": "jest",
    "cdk": "cdk",
    "bootstrap": "cdk bootstrap --app \"npx ts-node bin/bootstrap.ts\"",
    "network:deploy": "cdk deploy --app \"npx ts-node bin/network.ts\" --require-approval never",
    "network:destroy": "cdk destroy --app \"npx ts-node bin/network.ts\" --force",
    "network:diff": "cdk diff --app \"npx ts-node bin/network.ts\"",
    "repository:deploy": "cdk deploy --app \"npx ts-node bin/docker-repository.ts\" --require-approval never",
    "repository:destroy": "cdk destroy --app \"npx ts-node bin/docker-repository.ts\" --force",
    "repository:diff": "cdk diff --app \"npx ts-node bin/docker-repository.ts\"",
    "service:deploy": "cdk deploy --app \"npx ts-node bin/service.ts\" --require-approval never",
    "service:destroy": "cdk destroy --app \"npx ts-node bin/service.ts\" --force",
    "service:diff": "cdk diff --app \"npx ts-node bin/service.ts\"",
    "synth:all": "npm run synth:bootstrap && npm run synth:repository && npm run synth:network && npm run synth:service",
    "synth:bootstrap": "cdk synth --app \"npx ts-node bin/bootstrap.ts\"",
    "synth:repository": "cdk synth --app \"npx ts-node bin/docker-repository.ts\"",
    "synth:network": "cdk synth --app \"npx ts-node bin/network.ts\"",
    "synth:service": "cdk synth --app \"npx ts-node bin/service.ts\""
  },
  "devDependencies": {
    "@types/jest": "^29.5.12",
    "@types/node": "^20.11.0",
    "aws-cdk": "^2.148.1",
    "jest": "^29.7.0",
    "ts-jest": "^29.1.2",
    "ts-node": "^10.9.2",
    "typescript": "^5.3.3"
  },
  "dependencies": {
    "aws-cdk-lib": "^2.148.1",
    "constructs": "^10.3.0"
  }
}
```

## Phase 3: Validation

### 3.1 Compare CloudFormation Templates

```bash
# Synthesize both Java and TypeScript templates
# Java (existing)
cdk synth --app "gradle -q run -DmainClass=com.lumina.cdk.NetworkApp" > java-network.yaml

# TypeScript (new)
cdk synth --app "npx ts-node bin/network.ts" > ts-network.yaml

# Compare
diff java-network.yaml ts-network.yaml
```

### 3.2 Run CDK Diff Against Deployed Stacks

```bash
# This shows what changes TypeScript version would make to existing stacks
npm run network:diff
npm run repository:diff
npm run service:diff
```

### 3.3 Test in Isolated Environment

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TESTING STRATEGY                                 │
│                                                                         │
│   1. Deploy TypeScript stacks to a separate test AWS account            │
│                                                                         │
│   2. Run integration tests against TypeScript deployment                │
│                                                                         │
│   3. Compare behavior with production Java deployment                   │
│                                                                         │
│   4. Validate:                                                          │
│      • VPC configuration (CIDR, subnets, routing)                       │
│      • ALB health checks and listeners                                  │
│      • ECS task definitions and service configuration                   │
│      • Security group rules                                             │
│      • Parameter Store values                                           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.4 Write Unit Tests

```typescript
// test/docker-repository.test.ts
import * as cdk from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import { DockerRepositoryStack } from '../lib/docker-repository-stack';

describe('DockerRepositoryStack', () => {
  test('creates ECR repository with correct configuration', () => {
    const app = new cdk.App();
    const stack = new DockerRepositoryStack(app, 'TestStack', {
      applicationName: 'test-app',
      maxImageCount: 10,
      imageTagMutability: false,
      env: {
        account: '123456789012',
        region: 'us-east-1',
      },
    });

    const template = Template.fromStack(stack);

    template.hasResourceProperties('AWS::ECR::Repository', {
      RepositoryName: 'test-app',
      ImageTagMutability: 'MUTABLE',
    });

    template.hasResource('AWS::ECR::Repository', {
      UpdateReplacePolicy: 'Retain',
      DeletionPolicy: 'Retain',
    });
  });
});
```

## Phase 4: Cutover

### 4.1 Migration Approach Options

**Option A: In-Place Update (Recommended)**
```
┌─────────────────────────────────────────────────────────────────────────┐
│                      IN-PLACE UPDATE                                    │
│                                                                         │
│   • Keep same stack names                                               │
│   • Keep same logical IDs where possible                                │
│   • CDK will perform update, not replacement                            │
│   • Minimal downtime                                                    │
│   • Rollback possible                                                   │
│                                                                         │
│   Steps:                                                                │
│   1. Ensure TypeScript generates identical CloudFormation               │
│   2. Deploy TypeScript version                                          │
│   3. CDK detects existing stack and updates                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Option B: Blue-Green Deployment**
```
┌─────────────────────────────────────────────────────────────────────────┐
│                      BLUE-GREEN DEPLOYMENT                              │
│                                                                         │
│   • Deploy new stacks alongside existing                                │
│   • Use different stack names (e.g., add "-v2" suffix)                  │
│   • Switch traffic at Route 53 or ALB level                             │
│   • Keep old stacks for rollback                                        │
│   • More resource usage during transition                               │
│                                                                         │
│   Steps:                                                                │
│   1. Deploy TypeScript stacks with new names                            │
│   2. Test new deployment thoroughly                                     │
│   3. Update DNS/traffic routing                                         │
│   4. Monitor for issues                                                 │
│   5. Destroy old Java stacks when confident                             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Cutover Checklist

```
PRE-CUTOVER
□ All TypeScript stacks synthesize without errors
□ CDK diff shows minimal/expected changes
□ Unit tests pass
□ Integration tests pass in test environment
□ Team reviewed TypeScript code
□ Rollback plan documented

DURING CUTOVER
□ Notify stakeholders of deployment window
□ Take snapshot/backup of current state
□ Deploy stacks in order: Repository → Network → Service
□ Verify each stack before proceeding
□ Monitor CloudWatch for errors
□ Test application functionality

POST-CUTOVER
□ Remove Java CDK code from repository
□ Update CI/CD pipelines
□ Update documentation
□ Archive Gradle build files
□ Remove Java dependencies from project
```

### 4.3 Update CI/CD Pipeline

**Before (Java):**
```yaml
- name: Deploy Infrastructure
  run: |
    cd infrastructure
    npm run network:deploy  # Uses Gradle internally
```

**After (TypeScript):**
```yaml
- name: Deploy Infrastructure
  run: |
    cd infrastructure
    npm ci
    npm run network:deploy  # Uses ts-node directly
```

## Key Differences Reference

### Syntax Comparison Table

| Concept | Java | TypeScript |
|---------|------|------------|
| App creation | `App app = new App();` | `const app = new cdk.App();` |
| Stack creation | `new Stack(app, "Name", StackProps.builder()...)` | `new cdk.Stack(app, 'Name', {...})` |
| Context access | `app.getNode().tryGetContext("key")` | `app.node.tryGetContext('key')` |
| Environment | `Environment.builder().account(...).region(...).build()` | `{ account: ..., region: ... }` |
| Builder pattern | `.builder().property(value).build()` | `{ property: value }` |
| Maps/Objects | `Map<String, String>` | `{ [key: string]: string }` |
| Null checks | `string == null \|\| string.isBlank()` | `!string \|\| string.trim() === ''` |
| Static imports | `import static ...` | `import { fn } from '...'` |

### Common Pitfalls

1. **Logical ID Changes**: If construct IDs change, resources get replaced. Keep IDs identical.

2. **Property Name Differences**: Some CDK properties have different names in TypeScript vs Java (e.g., camelCase vs snake_case).

3. **Type Casting**: Java requires explicit casting for context values; TypeScript infers types.

4. **Import Differences**: TypeScript uses named imports; Java uses class imports.

5. **Async Lookups**: `Vpc.fromLookup()` requires account/region at synth time in TypeScript.

## Timeline Estimate

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        MIGRATION TIMELINE                               │
│                                                                         │
│   Phase 1: Preparation                                                  │
│   ├── Project setup: 1-2 days                                           │
│   ├── Stratospheric analysis: 1 day                                     │
│   └── Documentation review: 1 day                                       │
│                                                                         │
│   Phase 2: Implementation                                               │
│   ├── Bootstrap stack: 0.5 days                                         │
│   ├── Docker Repository stack: 1 day                                    │
│   ├── Network stack: 2-3 days                                           │
│   └── Service stack: 2-3 days                                           │
│                                                                         │
│   Phase 3: Validation                                                   │
│   ├── Unit tests: 1-2 days                                              │
│   ├── Integration testing: 2-3 days                                     │
│   └── Template comparison: 1 day                                        │
│                                                                         │
│   Phase 4: Cutover                                                      │
│   ├── Production deployment: 1 day                                      │
│   └── Cleanup and documentation: 1 day                                  │
│                                                                         │
│   TOTAL: ~2-3 weeks                                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Resources

- [AWS CDK TypeScript Reference](https://docs.aws.amazon.com/cdk/api/v2/docs/aws-cdk-lib-readme.html)
- [CDK Patterns](https://cdkpatterns.com/)
- [AWS CDK Best Practices](https://docs.aws.amazon.com/cdk/v2/guide/best-practices.html)
- [CDK Workshop](https://cdkworkshop.com/)
- [Stratospheric Book](https://stratospheric.dev/)
