# argocd-sharedservices

argocd-sharedservices are [ArgoCD](https://argo-cd.readthedocs.io/en/stable/) [Applications](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/#applications) meant to be deployed into the Croatia shared services application stack. As such, they are only expected to exist once across all of Croatia.

## Configuration Files

| File | Description |
| ---- | ----------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply |
| [applications](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/applications.yaml) | This is a meta-application, designed to setup an Application for deploying the `argocd-application` Kustomize setup |
| [artifactory](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/artifactory.yaml) | Application for deploying the Croatia instance of [Artifactory](https://jfrog.com/artifact-management/) |
| [backstage](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/backstage.yaml) | Application for deploying the Croatia instance of [Backstage](https://backstage.io/) |
| [camunda](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/camunda.yaml) | Application for deploying the Croatia instance of [Camunda](https://camunda.com/platform/) |
| [jenkins-namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/jenkins-namespace.yaml) | Deploys pre-requisite components for [Jenkins](https://www.jenkins.io/), namely the namespace, networking, and secrets |
| [jenkins](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/jenkins.yaml) | Application for deploying the Croatia instance of [Jenkins](https://www.jenkins.io/) |
| [keycloak](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/keycloak.yaml) | Application for deploying the Croatia instance of [keycloak](https://www.keycloak.org/) |
| [operators](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/operators.yaml) | Application for deploying all the operators used in the Croatia shared services stack |
| [postgresql](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/postgresql.yaml) | Application for deploying the Croatia instance of [perona postgres](https://www.percona.com/software/postgresql-distribution) |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/service-mesh.yaml) | Application for deploying the Croatia instance of [openshift service mesh](https://www.redhat.com/en/technologies/cloud-computing/openshift/what-is-openshift-service-mesh) |
| [sonarqube](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/sonarqube.yaml) | Application for deploying the Croatia instance of [sonarqube](https://www.sonarsource.com/products/sonarqube/) |
| [vault-network](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/vault-network.yaml) | Application for deploying pre-requisites for Hashicorp Vault |
| [vault](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/service-mesh.yaml) | Application for deploying the Croatia instance of [vault](https://www.vaultproject.io/) |
