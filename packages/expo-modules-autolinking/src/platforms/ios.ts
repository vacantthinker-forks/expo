import glob from 'fast-glob';
import fs from 'fs-extra';
import path from 'path';

import { ModuleDescriptor, PackageRevision, SearchOptions } from '../types';

/**
 * Resolves module search result with additional details required for iOS platform.
 */
export async function resolveModuleAsync(
  packageName: string,
  revision: PackageRevision,
  options: SearchOptions
): Promise<ModuleDescriptor | null> {
  const [podspecFile] = await glob('*/*.podspec', {
    cwd: revision.path,
    ignore: ['**/node_modules/**'],
  });

  if (!podspecFile) {
    return null;
  }

  const podName = path.basename(podspecFile, path.extname(podspecFile));
  const podspecDir = path.dirname(path.join(revision.path, podspecFile));

  return {
    podName,
    podspecDir,
    flags: options.flags,
    modulesClassNames: revision.config?.iosModulesClassNames(),
  };
}

/**
 * Generates Swift file that contains all autolinked Swift packages.
 */
export async function generatePackageListAsync(
  modules: ModuleDescriptor[],
  targetPath: string
): Promise<void> {
  const className = path.basename(targetPath, path.extname(targetPath));
  const generatedFileContent = await generatePackageListFileContentAsync(modules, className);

  await fs.outputFile(targetPath, generatedFileContent);
}

/**
 * Generates the string to put into the generated package list.
 */
async function generatePackageListFileContentAsync(
  modules: ModuleDescriptor[],
  className: string
): Promise<string> {
  const modulesToProvide = modules.filter(module => module.modulesClassNames.length > 0);
  const pods = modulesToProvide.map(module => module.podName);
  const classNames = [].concat(...modulesToProvide.map(module => module.modulesClassNames));

  return `// Automatically generated by expo-modules-autolinking.
import ExpoModulesCore

${pods.map(podName => `import ${podName}`).join('\n')}

@objc(${className})
public class ${className}: ModulesProvider {
  public override func exportedModules() -> [AnyModule.Type] {
    return [
      ${classNames.map(className => `${className}.self`).join(',\n      ')}
    ]
  }
}
`;
}
