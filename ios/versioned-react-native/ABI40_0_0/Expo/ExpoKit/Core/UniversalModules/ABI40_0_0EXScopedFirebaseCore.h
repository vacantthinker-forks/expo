// Copyright 2020-present 650 Industries. All rights reserved.

#if __has_include(<ABI40_0_0EXFirebaseCore/ABI40_0_0EXFirebaseCore.h>)
#import <UIKit/UIKit.h>
#import <ABI40_0_0EXFirebaseCore/ABI40_0_0EXFirebaseCore.h>
#import <ABI40_0_0EXUpdates/ABI40_0_0EXUpdatesRawManifest.h>
#import "ABI40_0_0EXConstantsBinding.h"

NS_ASSUME_NONNULL_BEGIN

@interface ABI40_0_0EXScopedFirebaseCore : ABI40_0_0EXFirebaseCore

- (instancetype)initWithScopeKey:(NSString *)scopeKey manifest:(ABI40_0_0EXUpdatesRawManifest *)manifest constantsBinding:(ABI40_0_0EXConstantsBinding *)constantsBinding;

@end

NS_ASSUME_NONNULL_END
#endif
