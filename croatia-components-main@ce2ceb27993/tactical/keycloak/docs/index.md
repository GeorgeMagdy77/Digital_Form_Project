# Keycloak

[Keycloak](https://www.keycloak.org/) is the tactical setup for SSO and user management within Croatia. Expected to be replaced by IBM ISVA for strategic

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)
- [operators/sharedservices](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/sharedservices)

## Overlay Overrides

There are no expected overlay overrides for Keycloak

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/keycloak/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [certificates](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/keycloak/certificates.yaml) | cert-manager [Certificate](https://cert-manager.io/docs/concepts/certificate/) resources to create a self signed TLS certificate for Keycloak to use to host as HTTPS | No | -4 - -1 |
| [externalsecrets](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/keycloak/externalsecrets.yaml) | [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resources to load secrets from vault. Loads connection information for Postgres, to enable data persistence | No | -1 |
| [keycloak](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/keycloak/keycloak.yaml) | A [Keycloak](https://www.keycloak.org/operator/advanced-configuration) resource to create the Keycloak server instance | No | 0 |
