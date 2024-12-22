# Openshift Logging

[Openshift Logging](https://docs.openshift.com/container-platform/4.10/logging/cluster-logging.html) is the tactical logging solution for log collection and aggregation on Croatia

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

## Overlay Overrides

There are no expected overlay overrides for Openshift Logging

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/logging/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [cluster-logging](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/logging/cluster-logging.yaml) | A `ClusterLogging` resource to setup the default logging setup for the cluster | No | 0 |
