# Artifactory

[Artifactory](https://jfrog.com/artifactory/) is the strategic artifact storage solution for Croatia, and is to be setup as a single instance within the shared sevices stack

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)

## Overlay Overrides

There are no expected overlay overrides for Artifactory

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/artifactory/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/artifactory/namespace.yaml) | Creation of the `artfactory` namespace, and adds it to the service mesh | Yes - Sync | -2 |
| [secrets.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/artifactory/secrets.yaml) | An [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resource to load secrets from vault. Loads the initial keys for Artifactory from vault | No | 0 |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/artifactory/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [Artifactory chart](https://github.com/jfrog/charts/tree/master/stable/artifactory) | N/A | N/A |
