# Openshift Operators

[Openshift Operators](https://www.redhat.com/en/technologies/cloud-computing/openshift/what-are-openshift-operators) are the preferred method of subscribing to component upstreams, and their [Custom Resource Definitions(CRDs)](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/)

Operator subscriptions are added as a separate application to our clusters, in order to make sure the required CRDs are present before running deployments/reconcilliations against the target component deployments

## Depends On

There are no dependencies for Openshift Operators

## Overlay Overrides

There are no expected overlay overrides for Openshift Operators

## Per cluster type Operators

- [core](./core.md)
- [sharedservices](./sharedservices.md)
