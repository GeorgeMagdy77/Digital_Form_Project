# Camunda Platform

[Camunda Platform](https://camunda.com/platform/) is the strategic Business Process Management(BPM) platform to be used by Croatia. The expected major version of the platform, is Camunda Platform 8.

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)

## Overlay Overrides

### machineset.yaml

| Field | Expected Value |
| ----- | ----------- |
| metadata.labels.machine.openshift.io/cluster-api-cluster | Name of the cluster |
| spec.selector.matchLabels.machine.openshift.io/cluster-api-cluster | Name of the cluster |
| spec.template.metadata.labels.machine.openshift.io/cluster-api-cluster | Name of the cluster |
| spec.template.spec.providerSpec.value.network.devices[0].networkName | Name of the NSX-T network in vmware |
| spec.template.spec.providerSpec.value.workspace.folder | Folder path to the resources within VSphere |
| spec.template.spec.providerSpec.value.workspace.server | FQDN/IP to the VSphere host reachable from the cluster |
| spec.template.spec.providerSpec.value.template | The template VM image in VSphere for cluster worker images |

#### Example implementation
[overlays/development/camunda-platform/machineset.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/overlays/development/camunda-platform/machineset.yaml)
```
apiVersion: machine.openshift.io/v1beta1
kind: MachineSet
metadata:
  name: camunda-wokers
  namespace: openshift-machine-api
  labels:
    machine.openshift.io/cluster-api-cluster: dev-nonpci-shared-5f9mm
spec:
  selector:
    matchLabels:
      machine.openshift.io/cluster-api-cluster: dev-nonpci-shared-5f9mm
  template:
    metadata:
      labels:
        machine.openshift.io/cluster-api-cluster: dev-nonpci-shared-5f9mm
    spec:
      providerSpec:
        value:
          network:
            devices:
              - networkName: NSX-SEG-NonProd-NonPCI-Shared
          workspace:
            folder: OCI-DigitalPlatform/vm/dev-nonpci-shared-5f9mm
            server: vcenter-nonproddpsddc.sddc.jed.oci.oraclecloud.com
          template: dev-nonpci-shared-5f9mm-rhcos
```

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [connectors-cm](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/connectors-cm.yaml) | A kubernetes configmap for configuring [Camunda connectors](https://camunda.com/platform/modeler/connectors/) | No | 0 |
| [connectors-deployment](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/connectors-deployment.yaml) | A kubernetes deployment for the application connectors pod required by the platform | No | 0 |
| [connectors-service](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/connectors-service.yaml) | A kubernetes service for exposing the interface running on the pod for the connectors | No | 0 |
| [es-statefulset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/es-statefulset.yaml) | A kubernetes statefulset for deploying the required Elastic Stack component used by the platform | No | 0 |
| [key-statefulset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/key-statefulset.yaml) | A kubernetes statefulset for deploying the required Keycloak component used by the platform | No | 0 |
| [machineset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/machineset.yaml) | An Openshift MachineSet resource for creating worker nodes specific for the platform | No | -1 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/namespace.yaml) | Creation of the `camunda` namespace, and adds it to the service mesh | No | -1 |
| [pg-statefulset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/pg-statefulset.yaml) | A kubernetes statefulset for deploying the required Postgres component used by the platform | No | 0 |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/camunda-platform/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [Camunda Platform chart](https://github.com/camunda/camunda-platform-helm/tree/main/charts/camunda-platform) | N/A | N/A |
