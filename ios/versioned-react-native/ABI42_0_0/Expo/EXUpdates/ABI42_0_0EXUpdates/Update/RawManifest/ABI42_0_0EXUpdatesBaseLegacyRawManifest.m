//  Copyright © 2021 650 Industries. All rights reserved.

#import <ABI42_0_0EXUpdates/ABI42_0_0EXUpdatesBaseLegacyRawManifest.h>
#import <ABI42_0_0EXUpdates/ABI42_0_0NSDictionary+EXUpdatesRawManifest.h>

@implementation ABI42_0_0EXUpdatesBaseLegacyRawManifest

- (nullable NSDictionary *)expoClientConfigRootObject {
  return self.rawManifestJSON;
}

- (nullable NSDictionary *)expoGoConfigRootObject {
  return self.rawManifestJSON;
}

- (NSString *)stableLegacyId {
  NSString *originalFullName = [self.rawManifestJSON nullableStringForKey:@"originalFullName"];
  if (originalFullName) {
    return originalFullName;
  } else {
    return [self legacyId];
  }
}

- (NSString *)scopeKey {
  NSString *scopeKey = [self.rawManifestJSON nullableStringForKey:@"scopeKey"];
  if (scopeKey) {
    return scopeKey;
  } else {
    return [self stableLegacyId];
  }
}

- (nullable NSString *)projectId {
  return [self.rawManifestJSON nullableStringForKey:@"projectId"];
}

- (NSString *)bundleUrl {
  return [self.rawManifestJSON stringForKey:@"bundleUrl"];
}

- (nullable NSString *)sdkVersion {
  return [self.rawManifestJSON nullableStringForKey:@"sdkVersion"];
}

- (nullable NSArray *)assets {
  return [self.rawManifestJSON nullableArrayForKey:@"assets"];
}

@end
