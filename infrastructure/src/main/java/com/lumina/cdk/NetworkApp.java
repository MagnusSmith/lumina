package com.lumina.cdk;

import dev.stratospheric.cdk.Network;
import dev.stratospheric.cdk.Network.NetworkInputParameters;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import static com.lumina.cdk.Validations.requireNonEmpty;

public class NetworkApp {
  public static void main(final String[] args) {
    App app = new App();

    String environmentName = (String) app.getNode().tryGetContext("environmentName");
    requireNonEmpty(environmentName, "context variable 'environmentName' must not be null");

    String accountId = (String) app.getNode().tryGetContext("accountId");
    requireNonEmpty(accountId, "context variable 'accountId' must not be null");

    String region = (String) app.getNode().tryGetContext("region");
    requireNonEmpty(region, "context variable 'region' must not be null");
 //   String sslCertificateArn = (String) app.getNode().tryGetContext("sslCertificateArn");
 //   requireNonEmpty(sslCertificateArn, "context variable 'sslCertificateArn' must not be null");

    Environment awsEnvironment = makeEnv(accountId, region);

    Stack networkStack = new Stack(app, "NetworkStack", StackProps.builder()
        .stackName(environmentName + "-Network")
        .env(awsEnvironment)
        .build());

    NetworkInputParameters inputParameters = new NetworkInputParameters();

//    if(!sslCertificateArn.isEmpty()){
//      inputParameters.withSslCertificateArn(sslCertificateArn);
//    }

    new Network(
        networkStack,
        "Network",
        awsEnvironment,
        environmentName,
        inputParameters);

    app.synth();
  }

  static Environment makeEnv(String account, String region) {
    return Environment.builder()
        .account(account)
        .region(region)
        .build();
  }
}
