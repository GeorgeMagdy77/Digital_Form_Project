# SonarQube

[SonarQube](https://www.sonarsource.com/products/sonarqube/) is a static analysis tool hosted on Croatia to analyse the source code generated as part of the project

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)

## Overlay Overrides

There are no expected overlay overrides for SonarQube

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/namespace.yaml) | Creation of the `sonarqube` namespace, and adds it to the service mesh | No | -2 |
| [service headless](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/sonarqube-svc-headless.yaml) | A Kubernetes Headless service resource responsible for the network identity of the Pods | No | 1 |
| [sonarqube statefulset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/sonarqube-statefulset.yaml) | A Kubernetes StatefulSet resource for provisioning the SonarQube running Pod | No | 1 |
| [postgresql statefulset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/postgresql-statefulset.yaml) | A Kubernetes StatefulSet resource for provisioning the Postgresql running Pod | No | 1 |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/sonarqube/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [SonarQube chart](https://github.com/SonarSource/helm-chart-sonarqube) | N/A | N/A |
