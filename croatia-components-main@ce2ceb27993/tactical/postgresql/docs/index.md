# Percona Managed Postgresql

[Percona Managed Postgresql](https://www.percona.com/software/postgresql-distribution) is a managed operator instance of Postgresql used in the Shared Services cluster stack

## Depends On

- [operators/sharedservices](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices)

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
[overlays/san-sandbox/postgresql/machineset.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/overlays/san-sandbox/postgresql/machineset.yaml)
```
apiVersion: machine.openshift.io/v1beta1
kind: MachineSet
metadata:
  name: postgresql-wokers
  namespace: openshift-machine-api
  labels:
    machine.openshift.io/cluster-api-cluster: san-sandbox
spec:
  selector:
    matchLabels:
      machine.openshift.io/cluster-api-cluster: san-sandbox
  template:
    metadata:
      labels:
        machine.openshift.io/cluster-api-cluster: san-sandbox
    spec:
      providerSpec:
        value:
          network:
            devices:
              - networkName: san-sandbox
          workspace:
            folder: /oci-w01dc/vm/san-sandbox-gbp2k
            server: vcenter-sandbox.sddc.jed.oci.oraclecloud.com
          template: san-sandbox-gbp2k-rhcos
```

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/postgresql/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [machineset](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/postgresql/machineset.yaml) | An Openshift MachineSet resource for creating worker nodes specific for the database platform | No | -1 |
| [postgresql](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/postgresql/postgresql.yaml) | A [PerconaPGCluster](https://docs.percona.com/percona-operator-for-postgresql/operator.html) resource for creating the Percona Postgresql database cluster stack | No | 0 |

