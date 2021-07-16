// Copyright 2016-present 650 Industries. All rights reserved.

#if __has_include(<ABI40_0_0EXPermissions/ABI40_0_0EXPermissions.h>)
#import "ABI40_0_0EXScopedPermissions.h"
#import <ABI40_0_0UMCore/ABI40_0_0UMUtilities.h>
#import <ABI40_0_0UMCore/ABI40_0_0UMDefines.h>

@interface ABI40_0_0EXScopedPermissions ()

@property (nonatomic, strong) NSString *scopeKey;
@property (nonatomic, weak) id<ABI40_0_0EXPermissionsScopedModuleDelegate> permissionsService;
@property (nonatomic, weak) id<ABI40_0_0UMUtilitiesInterface> utils;
@property (nonatomic, weak) ABI40_0_0EXConstantsBinding *constantsBinding;

@end

@implementation ABI40_0_0EXScopedPermissions

- (instancetype)initWithScopeKey:(NSString *)scopeKey andConstantsBinding:(ABI40_0_0EXConstantsBinding *)constantsBinding
{
  if (self = [super init]) {
    _scopeKey = scopeKey;
    _constantsBinding = constantsBinding;
  }
  return self;
}

- (void)setModuleRegistry:(ABI40_0_0UMModuleRegistry *)moduleRegistry
{
  [super setModuleRegistry:moduleRegistry];
  _utils = [moduleRegistry getModuleImplementingProtocol:@protocol(ABI40_0_0UMUtilitiesInterface)];
  _permissionsService = [moduleRegistry getSingletonModuleForName:@"Permissions"];
}

# pragma mark - permission requesters / getters

// overriding ABI40_0_0EXPermission to inject scoped permission logic
- (NSDictionary *)getPermissionUsingRequesterClass:(Class)requesterClass
{
  NSDictionary *globalPermission = [super getPermissionUsingRequesterClass:requesterClass];
  return [self getScopedPermissionForType:[requesterClass permissionType] withGlobalPermission:globalPermission];
}

- (NSString *)getScopedPermissionStatus:(NSString *)permissionType {
  if (!_permissionsService) {
    return [[self class] permissionStringForStatus:ABI40_0_0UMPermissionStatusGranted];
  }

  return [[self class] permissionStringForStatus:[_permissionsService getPermission:permissionType forExperience:_scopeKey]];
}

- (BOOL)hasGrantedScopedPermission:(NSString *)permissionType
{
  if (!_permissionsService || ![self shouldVerifyScopedPermission:permissionType]) {
    return YES;
  }

  return [_permissionsService getPermission:permissionType forExperience:_scopeKey] == ABI40_0_0UMPermissionStatusGranted;
}

- (void)askForPermissionUsingRequesterClass:(Class)requesterClass
                                    resolve:(ABI40_0_0UMPromiseResolveBlock)resolve
                                     reject:(ABI40_0_0UMPromiseRejectBlock)reject
{
  NSDictionary *globalPermissions = [super getPermissionUsingRequesterClass:requesterClass];
  NSString *permissionType = [requesterClass permissionType];
  ABI40_0_0UM_WEAKIFY(self)
  if (![globalPermissions[@"status"] isEqualToString:@"granted"]) {
    // first group
    // ask for permission. If granted then save it as scope permission
    void (^customOnResults)(NSDictionary *) = ^(NSDictionary *permission){
      ABI40_0_0UM_ENSURE_STRONGIFY(self)
      // if permission should be scoped save it
      if ([self shouldVerifyScopedPermission:permissionType]) {
        [self.permissionsService savePermission:permission ofType:permissionType forExperience:self.scopeKey];
      }
      resolve(permission);
    };

    return [self askForGlobalPermissionUsingRequesterClass:requesterClass withResolver:customOnResults withRejecter:reject];
  } else if ([_constantsBinding.appOwnership isEqualToString:@"expo"] &&
             ![self hasGrantedScopedPermission:permissionType]) {
    // second group
    // had to reinitilize UIAlertActions between alertShow invocations
    UIAlertAction *allowAction = [UIAlertAction actionWithTitle:@"Allow" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
      ABI40_0_0UM_ENSURE_STRONGIFY(self);
      NSMutableDictionary *permission = [globalPermissions mutableCopy];
      // try to save scoped permissions - if fails than permission is denied
      if (![self.permissionsService savePermission:permission ofType:permissionType forExperience:self.scopeKey]) {
        permission[@"status"] = [[self class] permissionStringForStatus:ABI40_0_0UMPermissionStatusDenied];
        permission[@"granted"] = @(NO);
      }
      resolve(permission);
    }];

    UIAlertAction *denyAction = [UIAlertAction actionWithTitle:@"Deny" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
      ABI40_0_0UM_ENSURE_STRONGIFY(self);
      NSMutableDictionary *permission = [globalPermissions mutableCopy];
      permission[@"status"] = [[self class] permissionStringForStatus:ABI40_0_0UMPermissionStatusDenied];
      permission[@"granted"] = @(NO);
      resolve([NSDictionary dictionaryWithDictionary:permission]);
    }];

    return [self showPermissionRequestAlert:permissionType withAllowAction:allowAction withDenyAction:denyAction];
  }

  resolve(globalPermissions); // third group
}

# pragma mark - helpers

- (NSDictionary *)getScopedPermissionForType:(NSString *)permissionType withGlobalPermission:(NSDictionary *)globalPermission
{
  if (!globalPermission) {
    return nil;
  }
  NSMutableDictionary *permission = [NSMutableDictionary dictionaryWithDictionary:globalPermission];

  if ([_constantsBinding.appOwnership isEqualToString:@"expo"]
      && [self shouldVerifyScopedPermission:permissionType]
      && [ABI40_0_0EXPermissions statusForPermission:permission] == ABI40_0_0UMPermissionStatusGranted) {
    permission[@"status"] = [self getScopedPermissionStatus:permissionType];
    permission[@"granted"] = [permission[@"status"] isEqual:@"granted"] ? @YES : @NO;
  }
  return permission;
}

- (void)showPermissionRequestAlert:(NSString *)permissionType
                   withAllowAction:(UIAlertAction *)allow
                    withDenyAction:(UIAlertAction *)deny
{
  NSString *experienceName = self.scopeKey; // TODO: we might want to use name from the manifest?
  NSString *messageTemplate = @"%1$@ needs permissions for %2$@. You\'ve already granted permission to another Expo experience. Allow %1$@ to also use it?";
  NSString *permissionString = [[self class] textForPermissionType:permissionType];

  NSString *message = [NSString stringWithFormat:messageTemplate, experienceName, permissionString];
  UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Experience needs permissions"
                                                                 message:message
                                                          preferredStyle:UIAlertControllerStyleAlert];
  [alert addAction:deny];
  [alert addAction:allow];

  ABI40_0_0UM_WEAKIFY(self);
  [ABI40_0_0UMUtilities performSynchronouslyOnMainThread:^{
    ABI40_0_0UM_ENSURE_STRONGIFY(self);
    // TODO: below line is sometimes failing with: "Presenting view controllers on detached view controllers is discourage"
    [self->_utils.currentViewController presentViewController:alert animated:YES completion:nil];
  }];
}

+ (NSString *)textForPermissionType:(NSString *)type
{
  if ([type isEqualToString:@"audioRecording"]) {
    return @"recording audio";
  } else if ([type isEqualToString:@"cameraRoll"]) {
    return @"photos";
  }

  return type;
}

- (BOOL)shouldVerifyScopedPermission:(NSString *)permissionType
{
  // temporarily exclude notifactions from permissions per experience; system brightness is always granted
  return ![@[@"notifications", @"userFacingNotifications", @"systemBrightness"] containsObject:permissionType];
}

@end
#endif
