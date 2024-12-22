import React from 'react';
import {
  EmptyState,
  Page,
  Content,
  Table,
  TableColumn,
  Progress,
} from '@backstage/core-components';

import Alert from '@material-ui/lab/Alert';

import { useKubernetesObjects, isKubernetesAvailable } from '@backstage/plugin-kubernetes';
import { useEntity } from '@backstage/plugin-catalog-react';

const k8sVersions = () => {
  const { entity } = useEntity();
  const { kubernetesObjects, error } = useKubernetesObjects(
    entity,
    300000,
  );

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!isKubernetesAvailable) {
    return <EmptyState
      title="No kubernetes cluster deployments found for the component"
      missing="info"
      description="Deploy the component to an environment to get a view of versions."
    />
  }

  if (!kubernetesObjects) {
    return <Progress/>;
  }

  let environmentMap = new Map<string, string>();

  let labelLookup: string;
  if (entity.metadata && entity.metadata.annotations && entity.metadata.annotations['k8s-versions/version-label']) {
    labelLookup = entity.metadata.annotations['k8s-versions/version-label'];
  } else {
    labelLookup = 'app.kubernetes.io/version';
  }

  kubernetesObjects.items.forEach(item => {
    item.resources.forEach(resource => {
      if (resource.type === 'replicasets' || resource.type === 'deployments' || resource.type === 'statefulsets' || resource.type === 'daemonsets' ) {
        resource.resources.forEach(computeResource => {
          if (computeResource.metadata && computeResource.metadata.labels && computeResource.metadata.labels[labelLookup]) {
            environmentMap.set(item.cluster.name, computeResource.metadata.labels[labelLookup]);
          }
        })
      }
    })
  })

  let versionData: any[] = [];

  environmentMap.forEach((value: string, key: string) => {
    versionData.push({environment: key, version: value})
  })

  const columns: TableColumn[] = [
    { title: 'Environment', field: 'environment' },
    { title: 'Version', field: 'version' },
  ];

  return <Table
    title="Environment Versions"
    options={{ search: false, paging: false }}
    columns={columns}
    data={versionData}
  />;
}

export const EnvironmentVersionComponent = () => (
  <Page themeId="tool">
    <Content>
      {k8sVersions()}
    </Content>
  </Page>
);
