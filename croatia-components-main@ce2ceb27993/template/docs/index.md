# {{name}}

<!---
Add a description of the component here, with details of it's intended purpose in the project
--->

## Depends On

<!---
State here if any other components are a pre-requisite to deploying this one, provide it in a format of a link to that component source code, eg:
- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

Otherwise, simply state there are no dependencies, eg: There are no dependencies for {{name}}
--->

## Overlay Overrides

<!---
State here if there are environment specific overrides that are needed using Kustomize overlays.
An example format of this is the following:

### example.yaml

| Field | Expected Value |
| ----- | ----------- |
| metadata.labels.example | And example field |

#### Example implementation
[overlays/example/{{name}}/example.yaml](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/overlays/example/{{name}}/example.yaml)
```
apiVersion: v1
kind: Example
metadata:
  name: example
  namespace: example
  labels:
    example: some-value
```

If there are no overrides needed, simply state it, eg: There are no expected overlay overrides for {{name}}
--->

## Configuration Files

<!---
In the table here, fill in the fields for every manifest file used in the component. Kustomization should always be included as a reference
--->

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/{{name}}/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
