import { createPlugin, createRoutableExtension } from '@backstage/core-plugin-api';

import { rootRouteRef } from './routes';

export const k8SVersionsPlugin = createPlugin({
  id: 'k8s-versions',
  routes: {
    root: rootRouteRef,
  },
});

export const K8SVersionsPage = k8SVersionsPlugin.provide(
  createRoutableExtension({
    name: 'K8SVersionsPage',
    component: () =>
      import('./components/EnvironmentVersionComponent').then(m => m.EnvironmentVersionComponent),
    mountPoint: rootRouteRef,
  }),
);
