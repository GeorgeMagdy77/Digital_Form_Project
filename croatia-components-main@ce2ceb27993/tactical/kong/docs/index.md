# Kong

[Kong](https://docs.konghq.com/) is the primary API Gateway in use by Croatia. This configuration is designed to handle a ['dbless'](https://docs.konghq.com/gateway/latest/production/deployment-topologies/db-less-and-declarative-config/) deployment of Kong, and therefore expects the configuration to be deployed independently of this application setup

## Depends On

There are no dependencies for Kong

## Overlay Overrides

### certificate.yaml

Not provided in the base configuration, is is expected that this is run via an overlay, with a Kubernetes TLS secret resource with the name `kong-proxy-tls` will be created. This is essential for Kong to run as a TLS application, and should be signed with a valid CA certificate

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/kong/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [values.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/kong/values.yaml) | A Helm [values file](https://helm.sh/docs/chart_template_guide/values_files/) for the [kong chart](https://github.com/Kong/charts/tree/main/charts/kong) | N/A | N/A |
