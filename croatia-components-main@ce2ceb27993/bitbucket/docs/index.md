# Bitbucket

[Bitbucket Server](https://www.atlassian.com/software/bitbucket/enterprise) is the strategic SCM solution in place for Croatia

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)
- [vault](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault)

## Overlay Overrides

There are no expected overlay overrides for Bitbucket

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [deployment](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/deployment.yaml) | A kubernetes deployment for hosting Bitbucket server | No | 0 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/namespace.yaml) | Creation of the `bitbucket` namespace, and adds it to the service mesh | No | -2 |
| [postgres-configmap](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/postgres-configmap.yaml) | A kubernetes configmap for the Postgres database, to set the name and user for the Bitbucket database | No | 0 |
| [postgres-deployment](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/postgres-deployment.yaml) | A kubernetes deployment for the Postgres database to be used by the Bitbucket instance in the same namespace | No | 0 |
| [postgres-pvc](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/postgres-pvc.yaml) | A kubernetes persistent volume claim attached to the Postgres database pod to maintain data persistence of Bitbucket data stored in the database | No | 0 |
| [postgres-service](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/postgres-service.yaml) | A kubernetes service for exposing the interface running on the pod for the databse |
| [pvc](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/ppvc.yaml) | A kubernetes persistent volume claim attached to the Bitbucket pod to maintain data persistence of Bitbucket data stored locally | No | 0 |
| [secrets.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/secrets.yaml) | An [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resource to load secrets from vault. Loads the initial Postgres user password | No | 0 |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/bitbucket/service-mesh.yaml) | Handles resources for the application to work within the service mesh. Creates an Istio [gateway](https://istio.io/latest/docs/reference/config/networking/gateway/) with tls support, and an associated [virtualservice](https://istio.io/latest/docs/reference/config/networking/virtual-service/) | No | 1 |
