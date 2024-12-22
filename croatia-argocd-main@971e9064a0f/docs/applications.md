# argocd-applications

argocd-applications are either [ArgoCD](https://argo-cd.readthedocs.io/en/stable/) [Applications](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/#applications) or [ApplicationSets](https://argo-cd.readthedocs.io/en/stable/user-guide/application-set/), and are targetted to run on Croatia Alpha environments, as pre-requisites to Alpha microservices.

## Configuration Files

| File | Description |
| ---- | ----------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply |
| [alpha-microservices](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/argocd-applications/alpha-microservices.yaml) | An ApplicationSet to create deployments for every Alpha microservice, on every cluster registered within ArgoCD. This will be covered in it's [own section](#alpha-microservices) to explain the environment specific configuration |
| [cert-manager](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/alpha-microservices.yaml) | Deploys and configures an instance of [cert-manager](https://cert-manager.io/) to every Alpha cluster |
| [croatia-configuration](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/croatia-configuration.yaml) | Deploys the Kustomize repository [croatia-configuration](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse) to every Alpha environment |
| [croatia-sso](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/croatia-sso.yaml) | Configures each Alpha cluster to integrate with the central SSO, currently Keycloak |
| [kong](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/kong.yaml) | Deploys the [Kong]() proof of concept configuration to the sandbox |
| [operators](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/operators.yaml) | Deploy the core operators to every Alpha environment |
| [vault-client](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/cert-manager/vault-client.yaml) | Deploys the client of [External Secrets](https://external-secrets.io) into Alpha environments, allowing secrets to be synced against Hasicorp Vault |

## alpha-microservices

The ApplicationSet for microservices generates applications based on a set of rules in the section `spec.generators`.
For these applications, we generate using the [matrix generator](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-Matrix/) to combine a series of apllication & environment specific values.
There are then two sub generators to build the matrix
* [SCM provider](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-SCM-Provider/) - Generate a list of applications from Bitbucket
* [Merge](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-Merge/) - Merge two sub-generators get a list of clusters to use
    * [Cluster](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-Cluster/) - Create a list of all registered clusters
    * [List](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-List/) - A list of environment specific overrides, allowing us to change the version of the application to deploy with the entries in the list

The list generator then, is used to specifically set the version constraints on the application created by the set. The intention being more restrictive conventions based on the environment in use.
