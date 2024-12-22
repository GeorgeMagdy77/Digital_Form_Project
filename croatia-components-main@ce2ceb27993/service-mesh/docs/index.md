# Openshift Service Mesh

[Openshift Service Mesh](https://www.redhat.com/en/technologies/cloud-computing/openshift/what-is-openshift-service-mesh) is the Openshift implementation of [Istio](https://istio.io/) in use as the service mesh on Croatia

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

## Overlay Overrides

There are no expected overlay overrides for Openshift Service Mesh

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [control-pane](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh/control-pane.yaml) | A [ServiceMeshControlPlane](https://docs.openshift.com/container-platform/4.10/service_mesh/v2x/ossm-reference-smcp.html) resource for provisioning the sevice mesh and associated resources on the cluster | No | 0 |
