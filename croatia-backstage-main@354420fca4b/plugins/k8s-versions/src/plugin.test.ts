import { k8SVersionsPlugin } from './plugin';

describe('k8s-versions', () => {
  it('should export plugin', () => {
    expect(k8SVersionsPlugin).toBeDefined();
  });
});
