# Openshift Shared Services Operators

[Openshift Shared Services Operators](https://www.redhat.com/en/technologies/cloud-computing/openshift/what-are-openshift-operators) are the Operators meant to be run on the Shared Services cluster on Croatia

## Depends On

There are no dependencies for Openshift Shared Services Operators

## Overlay Overrides

There are no expected overlay overrides for Openshift Shared Services Operators

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [keycloak](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices/keycloak.yaml) | Subscription for the Keycloak Operator | No | 0 |
| [postgresql](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices/postgresql.yaml) | Subscription for the Percona Managed Postgres Operator | No | 0 |
