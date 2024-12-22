# croatia-components

Croatia components is a [Kustomize](https://kustomize.io/) focused repository, designed to handle stateful deployments of the Croatia platform stack onto Openshift clusters

The expected deployment mechanism for any components within this repository, is [ArgoCD](https://argo-cd.readthedocs.io/en/stable/). As such, ArgoCD metadata annotations related to the lifecycle of the manifest are included. The primary interaction with these annotations will be `sync-waves` and `hooks`, documentation regarding the specifics of these can be found at: https://argo-cd.readthedocs.io/en/stable/user-guide/sync-waves/

## Contribution

All contributions to this repository are expected to follow a standard flow of making changes on a feature branch, and then raiding a PR for merge to master, as per [trunk based development](https://trunkbaseddevelopment.com/) conventions.

In addition, this repository is expected to be controlled by [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/), and commit messages MUST adhere to these conventions. As this repository holds multiple components, using the scoped messages is expected, eg. `feat(jenkins): updated core jenkins version`. As such, for a PR changing multiple components, separate commits MUST be used to separate the changes between them

### Updating Components

When updating existing components, please refer to the documentation for that specific component to validate what is required. The documentation can always be found at `{component}/docs/index.md`

This documentation will specify any dependencies, and if any environment specific considerations must be factored in. Please adhere to the guidance in there when updating components

Beyond this, all files should be valid Kubernetes manifest files, and when adding a file, make sure it is reflected in the component kustomization.yaml file

The documentation at `{component}/docs/index.md` must be updated to reflect any changes where appropriate, especially if new manifest files have been added

### Adding Components

When adding a component, please copy the folder `template`, and re-name it to the desired target component. Once done, follow the following steps:
* In the `mkdocs.yml` file, replace `{{name}}` with the name of the component
* Add in the required manifest files for the component
* Reference the manifest files and/or helm chart in the `kustomization.yaml` file
* Update the `./docs/index.md` file, replace all instance of `{{name}}` with the name of the component, and follow the instruction in the template version to fill in the required fields
* Add a line in `./catalog-info.yaml` to add in the new component so it's documentation is picked up, the following template can be used to add in a new line item: 

```apiVersion: backstage.io/v1alpha1
kind: Component
metadata:
  annotations:
    backstage.io/techdocs-ref: dir:./{{name}}
  title: {{Title/DisplayName}}
  name: {{name}}
spec:
  lifecycle: experimental
  owner: platform
  type: service
  system: croatia-components
```

* Raise a PR, seek reviews, and merge the change
* If required, follow the instruction in the [ArgoCD repository](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-argocd/browse) to add the component as an application for deployment via ArgoCD
