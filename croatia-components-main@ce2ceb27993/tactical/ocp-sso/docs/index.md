# Openshift SSO

The Openshift SSO configuration is for setting up the Openshift OAuth configuration to allow Keycloak as a valid target for SSO logins

## Depends On

There are no dependencies for Openshift SSO

## Overlay Overrides

### oauth.yaml

| Field | Expected Value |
| ----- | ----------- |
| spec.identityProviders | A full implementation of an [Openshift OpenID identity provider](https://docs.openshift.com/container-platform/4.8/rest_api/oauth_apis/oauth-apis-index.html. The primary field needed for override as environment specific is `spec.identityProviders[0].openID.clientID`, all other fields are common |

#### Example implementation
[overlays/development/ocp-sso/oauth.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/overlays/development/ocp-sso/oauth.yaml)
```
apiVersion: config.openshift.io/v1
kind: OAuth
metadata:
  name: cluster
spec:
  identityProviders:
    - mappingMethod: add
      name: keycloak
      openID:
        ca:
          name: croatia-keycloak-ca
        claims:
          email:
            - email
          name:
            - name
          preferredUsername:
            - preferred_username
        clientID: cluster-development
        clientSecret:
          name: croatia-keycloak-client-secret
        extraScopes: []
        issuer: https://keycloak.apps.san-sandbox.projectcroatia.cloud/realms/master
      type: OpenID
```

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/ocp-sso/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [groups](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/ocp-sso/groups.yaml) | Kubernetes groups & role bindings for Keycloak users to be granted access to the cluster | No | 0 |
| [oauth](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/ocp-sso/groups.yaml) | An example template implementation of the Openshift OAuth resource. Expected to be called/overridden by an overlay | No | -1 |
