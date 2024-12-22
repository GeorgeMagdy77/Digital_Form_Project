# Openshift Core Operators

[Openshift Core Operators](https://www.redhat.com/en/technologies/cloud-computing/openshift/what-are-openshift-operators) are the core Operators meant to be applied/subscribed to in every Croatia Openshift cluster

## Depends On

There are no dependencies for Openshift Core Operators

## Overlay Overrides

There are no expected overlay overrides for Openshift Core Operators

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [cert-manager](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/cert-manager.yaml) | Subscription for the cert-manager Operator | No | 0 |
| [external-secrets](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/external-secrets.yaml) | Subscription for the External Secrets Operator | No | 0 |
| [jaeger](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/jaeger.yaml) | Subscription for the Jaeger Operator | No | 0 |
| [kiali](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/kiali.yaml) | Subscription for the Kiali Operator | No | 0 |
| [logging](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/logging.yaml) | Subscription for the Openshift Logging Operator | No | 0 |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core/service-mesh.yaml) | Subscription for the Openshift Service Mesh Operator | No | 0 |
