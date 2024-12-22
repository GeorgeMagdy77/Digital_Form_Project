# croatia-argocd

croatia-argocd repository is setup to manage the instance of [ArgoCD](https://argo-cd.readthedocs.io/en/stable/), as well as it's respective [Application & ApplicationSet](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/) resources

It's expected that this repository references the [croatia-components](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse) repository with specific versions for environments, to manage the respective deployments on the appropriate versions

## Contribution

All contributions to this repository are expected to follow a standard flow of making changes on a feature branch, and then raiding a PR for merge to master, as per [trunk based development](https://trunkbaseddevelopment.com/) conventions.

In addition, this repository is expected to be controlled by [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/), and commit messages MUST adhere to these conventions

### Updating Components

When updating components, the expected mechanism to use an new branch/tag/version of the repspective associated component only
