# Jenkins

[Jenkins](https://www.jenkins.io/) is the primary Continuous Integration(CI) tool in use by Croatia, and is designed to handle CI workloads across all applications

This component also contains the Dockerfiles for building the [Jenkins Agent](https://www.jenkins.io/doc/book/using/using-agents/) images in use within Jenkins. These can be locate service by service at: [jenkins/containers](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/containers), as well as their `Jenkinsfile` orchestrating builds

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [operators/sharedservices](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)
- [vault](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault)

## Overlay Overrides

There are no expected overlay overrides for Jenkins

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [agent-serviceaccount](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/agent-serviceaccount.yaml) | A Kubernetes service account all agentes are expected to run under. Used for meeting Openshift SCC requirements | No | -3 - -1 |
| [certificate](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/certificate.yaml) | A kubernetes TLS secret for istio to run the application under for HTTPS | No | -1 |
| [externalsecrets](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/externalsecrets.yaml) | [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resources to load secrets from vault. Loads the Jenkins administration configuration, and credentials which will be available within every CI pipeline | No | -1 |
| [machineset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/machineset.yaml) | An Openshift MachineSet resource for creating worker nodes for Jenkins Agents to run within | No | -1 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/namespace.yaml) | Creation of the `jenkins` namespace, and adds it to the service mesh | No | -4 |
| [role](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/role.yaml) | A kubernetes role resource to grant [Openshift SCC](https://docs.openshift.com/container-platform/4.10/authentication/managing-security-context-constraints.html) permissions to the Jenkins master service account | No | -2 - -1 |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/service-mesh.yaml) | Handles resources for the application to work within the service mesh. Creates an Istio [gateway](https://istio.io/latest/docs/reference/config/networking/gateway/) with tls support, and an associated [virtualservice](https://istio.io/latest/docs/reference/config/networking/virtual-service/) | No | 1 |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [Jenkins chart](https://github.com/jenkinsci/helm-charts/tree/main/charts/jenkins) | N/A | N/A |

## User Guide

The Jenkins setup contains a number of sensitive elements, and as such, this section will go over common scenarios with respect to updating the Jenkins instance

### Updating Agents

Agent images are located in: [jenkins/containers](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/containers). These images are defined as standard `Dockerfiles`. There is an inheritance model in place, where all other images build off of the image as defined in `base`. As such, any changes to base, means that all other images must then be re-built to apply the changes.

The builds for each image are orchestrated by Jenkins at: https://jenkins.projectcroatia.cloud/job/agents/, each type of image having their own build process.

For updating images, update the `Dockerfile` with the appropriate changes, and then run the build from Jenkins. If the build completes successfully, the latest version of that image will be used in all subsequent builds, without any further changes needed. The exception to this is the `base` image, in which all other image types will be need to be rebuilt individually after that build has completed, in order for the changes to be reflected

### Jenkins CasC

The Jenkins instance here uses the [Jenkins Configuration as Code plugin (CasC)](https://plugins.jenkins.io/configuration-as-code/), meaning all configuration elements are handled via code.

The location of these configurations are located in [jenkins/values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/values.yaml), within a Helm values file. The appropriate yaml path to get to the CasC elements, is at `controller.JCasC.configScripts`.

Documentation regarding how to interact with the CasC file(s) can be found at: https://jenkins.projectcroatia.cloud/manage/configuration-as-code/reference, providing a full API reference for how to configure Jenkins

### User Administration

Jenkins users are managed by Keycloak only. Access is handled by a Keycloak role called `jenkins-users`, and is attached to all newly created users by default.

Jenkins administrators are managed by the Keycloak role `jenkins-administrators`. Adding this role to a user in Keycloak, will grant full administrative priviledges to that user. Please speak with the Platform Team leads if you believe this is appropriate for you.

### Changing secrets/credentials

All secrets/credentials in Jenkins are held within the [Hashicorp vault instance](https://vault.ingress.san-sandbox.projectcroatia.cloud/).

Secrets for Jenkins are only accessible to the Platform Team, if you need access, and do not have the appropriate permissions for Jenkins credentials, please reach out to the Platform Team.

To add a secret/credential to Jenkins, or change an existing one, follow these steps:
1. Log into to [Vault](https://vault.ingress.san-sandbox.projectcroatia.cloud/) via the UI
2. Navigate to the secret [kv/jenkins-credentials](https://vault.ingress.san-sandbox.projectcroatia.cloud/ui/vault/secrets/kv/show/jenkins-credentials)
3. Click the `Create new version` button, this will open up the fields for editing
4. Add/Update the secrets
    - If changing an existing secret, enter in the new value in the field, and click the `Save` button, and finish the steps here
    - If adding a new secret, enter in the new key on the left, and the new value on the fight, and click the `Save` button
5. In the [jenkins/externalsecrets.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/externalsecrets.yaml) file, add in a new entry after the last line, replacing `{KEY}` with the key entered in step 4, eg:
```
- secretKey: {KEY}
  remoteRef:
    key: kv/jenkins-credentials
    property: {KEY}
```
6. In the [jenkins/values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/jenkins/values.yaml) file, do the following
    - Replacing `{KEY}` with the key entered in step 4, add the following element to the property `controller.additionalExistingSecrets`
    ```
    - name: jenkins-credentials
        keyName: {KEY}
    ```
    - Replacing `{KEY}` with the key entered in step 4, add the following element to the property `controller.JCasC.configScripts.main.credentials.system.domainCredentials[0].credentals`. The example given is for a `string` type secret. Different Jenkins credential types will have different requirements. Refer to the [CasC documentation](https://jenkins.projectcroatia.cloud/manage/configuration-as-code/reference#domainCredentials) for implementation specific details for each credential type
    ```
    - string:
      id: "{KEY}"
      scope: GLOBAL
      secret: "${jenkins-credentials-{KEY}}"
    ``` 
