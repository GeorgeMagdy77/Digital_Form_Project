# Vault

[Hashicorp Vault](https://www.vaultproject.io/) is a secure vault, designed to hold secrets and other confidential information encrypted, but retrievable by applications

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)

## Overlay Overrides

There are no expected overlay overrides for SonarQube

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [certificate](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/certificate.yaml) | cert-manager [Certificate](https://cert-manager.io/docs/concepts/certificate/) resources to create a self signed TLS certificate for Vault to use to host as HTTPS | No | -4 - -1 |
| [clusterrolebinding](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/clusterrolebinding.yaml) | A binding for the Kubernetes Service Account associated to allow the role to act as an authentication delegator | No | -1 |
| [clustersecretstore](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/clustersecretstore.yaml) | A [ClusterSecretStore](https://external-secrets.io/v0.7.2/api/clustersecretstore/) that allows the External Secrets controller to reach the configured Vault instance | No | 0 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/namespace.yaml) | Creation of the `sonarqube` namespace, and adds it to the service mesh | No | -5 |
| [operatorconfig](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/operatorconfig.yaml) | A OperatorConfig resource to provision the External Secrets controller to listen to Kubernetes API events | No | -1 |
| [serviceaccount](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/serviceaccount.yaml) | A kubernetes service account Vault can call to authenticate against the target cluster authentication layer | No | -1 |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/service-mesh.yaml) | Handles resources for the application to work within the service mesh. Creates an Istio [gateway](https://istio.io/latest/docs/reference/config/networking/gateway/) with tls support, and an associated [virtualservice](https://istio.io/latest/docs/reference/config/networking/virtual-service/) | No | 1 - 2 |



| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [SonarQube chart](https://github.com/SonarSource/helm-chart-sonarqube) | N/A | N/A |
