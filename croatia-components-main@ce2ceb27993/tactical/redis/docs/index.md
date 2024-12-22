# Redis

[Redis](https://redis.io/) is a powerful in memory data caching store in use by Croatia microservices

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)

## Overlay Overrides

There are no expected overlay overrides for Redis

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/redis/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/redis/namespace.yaml) | Creation of the `redis` namespace, and adds it to the service mesh | No | -2 |
| [scc](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/redis/scc.yaml) | Creation of the `redis` scc to control permissions for pods | No | No |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/redis/service-mesh.yaml) | Handles resources for the application to work within the service mesh. Creates an Istio [gateway](https://istio.io/latest/docs/reference/config/networking/gateway/) with tls support, and an associated [virtualservice](https://istio.io/latest/docs/reference/config/networking/virtual-service/) | No | 1 |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/redis/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [Redis chart](https://charts.bitnami.com/bitnami) | N/A | N/A |