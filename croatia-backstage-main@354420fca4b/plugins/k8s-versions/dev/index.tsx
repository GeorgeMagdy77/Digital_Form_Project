import React from 'react';
import { createDevApp } from '@backstage/dev-utils';
import { k8SVersionsPlugin, K8SVersionsPage } from '../src/plugin';

createDevApp()
  .registerPlugin(k8SVersionsPlugin)
  .addPage({
    element: <K8SVersionsPage />,
    title: 'Root Page',
    path: '/k8s-versions'
  })
  .render();
