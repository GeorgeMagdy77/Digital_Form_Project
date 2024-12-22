# Backstage

[Backstage](https://backstage.io/) is the Croatia implementation for a developer portal deployed to the shared services stack 

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/service-mesh)
- [vault](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault)

## Overlay Overrides

There are no expected overlay overrides for Backstage

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [certificate](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/certificate.yaml) | A kubernetes TLS secret for istio to run the application under for HTTPS | No | -1 |
| [deployment](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/deployment.yaml) | A kubernetes deployment for hosting the backstage application | No | 0 |
| [externalsecrets.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/externalsecrets.yaml) | An [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resource to load secrets from vault. Loads a full config file from vault, with secret keys for database, SCM, and cluster connectivity | No | -1 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/namespace.yaml) | Creation of the `backstage` namespace, and adds it to the service mesh | No | -2 |
| [role](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/role.yaml) | A kubernetes role resource to grant [Openshift SCC](https://docs.openshift.com/container-platform/4.10/authentication/managing-security-context-constraints.html) permissions | No | 0 |
| [rolebinding](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/rolebinding.yaml) | A binding for the role to allow it to use the `anyuid` SCC, as this container does not run as root | No | 0 |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/service-mesh.yaml) | Handles resources for the application to work within the service mesh. Creates an Istio [gateway](https://istio.io/latest/docs/reference/config/networking/gateway/) with tls support, and an associated [virtualservice](https://istio.io/latest/docs/reference/config/networking/virtual-service/) | No | 1 |
| [service](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/service.yaml) | A kubernetes service for exposing the interface running on the pod | No | 0 |
| [serviceaccount](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/backstage/serviceaccount.yaml) | A kubernetes service account that the pod will run under. Expected to use the role created by this configuration to meet Openshift SCC requirements | No | 0 |
