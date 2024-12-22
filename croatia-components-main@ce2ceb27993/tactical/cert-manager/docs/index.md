# cert-manager

[cert-manager](https://cert-manager.io/docs/) is a client implementation to allow clusters to generate and request signing of certificates against a remote private certificate authority

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

## Overlay Overrides

There are no expected overlay overrides for cert-manager

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [clusterissuer](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/clusterissuer.yaml) | A kubernetes deployment for hosting Bitbucket server | No | 0 |
