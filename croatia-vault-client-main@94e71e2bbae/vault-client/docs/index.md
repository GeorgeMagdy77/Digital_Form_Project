# Vault Client

Vault Client is an implementation that enables a cluster to use [External Secrets](https://external-secrets.io/v0.7.2/) to load secrets from Vault, using only Kubernetes based authentication mechanisms.

This setup allows other applications to request [ExternalSecret](https://external-secrets.io/v0.7.2/api/externalsecret/) resources from Vault, without needing integration information, only path and resource references

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

## Overlay Overrides

### clustersecretstore.yaml

| Field | Expected Value |
| ----- | ----------- |
| spec.provider.vault.auth.kubernetes.mountPath | Name of the cluster, required so Vault can delegate authentication to the correct Openshift cluster |

#### Example implementation
[overlays/development/vault-client/clustersecretstore.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/overlays/development/vault-client/clustersecretstore.yaml)
```
apiVersion: external-secrets.io/v1beta1
kind: ClusterSecretStore
metadata:
  name: vault
spec:
  provider:
    vault:
      auth:
        kubernetes:
          mountPath: development

```

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [clusterrolebinding](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/clusterrolebinding.yaml) | A binding for the Kubernetes Service Account associated to allow the role to act as an authentication delegator | No | 0 |
| [clustersecretstore](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/clustersecretstore.yaml) | A [ClusterSecretStore](https://external-secrets.io/v0.7.2/api/clustersecretstore/) that allows the External Secrets controller to reach the configured Vault instance | No | 4 |
| [namespace](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/namespace.yaml) | Creation of the `vault` namespace, and adds it to the service mesh | No | -2 |
| [operatorconfig](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/operatorconfig.yaml) | A OperatorConfig resource to provision the External Secrets controller to listen to Kubernetes API events | No | -1 |
| [serviceaccount](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/serviceaccount.yaml) | A kubernetes service account Vault can call to authenticate against the target cluster authentication layer | No | -1 |
| [vault-ca](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/vault-client/vault-ca.yaml) | A Kubernetes TLS secret holding the CA certificate (without private key) for the Vault instance. Required qhilst Vault is running under a self signed certificate | No | -1 |
