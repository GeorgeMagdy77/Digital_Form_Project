# argocd-subscription

argocd-subscription is the core Kustomize setup for subscribing to the operator for ArgoCD. As ArgoCD deploys the rest of the components, this is kept separate so it may be used independently as a whole of platform bootstrap.

## Configuration Files

| File | Description |
| ---- | ----------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/namespace.yaml) | The namespace to hold the subscription |
| [subscription](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-sharedservices/subscription.yaml) | An operator subscription resource to deploy |
