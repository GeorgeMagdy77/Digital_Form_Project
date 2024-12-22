# ArgoCD

[ArgoCD](https://argo-cd.readthedocs.io/en/stable/) is the strategic Continuous Delivery(CD) tool used to deploy applications and components to Croatia.

The live instance for croatia, can be located at: https://argocd.projectcroatia.cloud/

This repository holds the [Kustomize](https://kustomize.io/) configuration for deploying the main deployment of the application.

Documentation regarding the key pre-requsite operator subscription is at: [subscription](./subscription.md)

## Configuration Files

| File | Description |
| ---- | ----------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply |
| [certificate](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/certificate.yaml) | A kubernetes TLS secret for istio to run the application under for HTTPS |
| [externalsecret.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/externalsecret.yaml) | An [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resource to load secrets from vault. Meant to load credentials to integrate with BitBucket |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/namespace.yaml) | Create the namespace, and add it to the service mesh |
| [server](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/server.yaml) | An [ArgoCD](https://argocd-operator.readthedocs.io/en/latest/usage/basics/) custom resource to create the instance |
| [service-mesh](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/service-mesh.yaml) | Create a gateway & virtual service with the custom certificate for the application |

## ArgoCD Applications

Documentation regarding the definitions for the ArgoCD Applications and ApplicationSets can be found at:
- [applications](./applications.md)
- [sharedservices](./sharedservices.md)
