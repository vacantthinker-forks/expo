/**
 * Copyright (c) 2015-present, Horcrux.
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import <ABI40_0_0React/ABI40_0_0RCTBridge.h>
#import <ABI40_0_0React/ABI40_0_0RCTUIManager.h>
#import <ABI40_0_0React/ABI40_0_0RCTUIManagerUtils.h>
#import "ABI40_0_0RNSVGSvgViewManager.h"
#import "ABI40_0_0RNSVGSvgView.h"

@implementation ABI40_0_0RNSVGSvgViewManager

ABI40_0_0RCT_EXPORT_MODULE()

- (UIView *)view
{
    return [ABI40_0_0RNSVGSvgView new];
}

ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(bbWidth, ABI40_0_0RNSVGLength*)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(bbHeight, ABI40_0_0RNSVGLength*)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(minX, CGFloat)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(minY, CGFloat)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(vbWidth, CGFloat)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(vbHeight, CGFloat)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(align, NSString)
ABI40_0_0RCT_EXPORT_VIEW_PROPERTY(meetOrSlice, ABI40_0_0RNSVGVBMOS)
ABI40_0_0RCT_CUSTOM_VIEW_PROPERTY(tintColor, id, ABI40_0_0RNSVGSvgView)
{
    view.tintColor = [ABI40_0_0RCTConvert UIColor:json];
}
ABI40_0_0RCT_CUSTOM_VIEW_PROPERTY(color, id, ABI40_0_0RNSVGSvgView)
{
    view.tintColor = [ABI40_0_0RCTConvert UIColor:json];
}


- (void)toDataURL:(nonnull NSNumber *)ABI40_0_0ReactTag options:(NSDictionary *)options callback:(ABI40_0_0RCTResponseSenderBlock)callback attempt:(int)attempt {
    [self.bridge.uiManager addUIBlock:^(ABI40_0_0RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        __kindof UIView *view = viewRegistry[ABI40_0_0ReactTag];
        NSString * b64;
        if ([view isKindOfClass:[ABI40_0_0RNSVGSvgView class]]) {
            ABI40_0_0RNSVGSvgView *svg = view;
            if (options == nil) {
                b64 = [svg getDataURL];
            } else {
                id width = [options objectForKey:@"width"];
                id height = [options objectForKey:@"height"];
                if (![width isKindOfClass:NSNumber.class] ||
                    ![height isKindOfClass:NSNumber.class]) {
                    ABI40_0_0RCTLogError(@"Invalid width or height given to toDataURL");
                    return;
                }
                NSNumber* w = width;
                NSInteger wi = (NSInteger)[w intValue];
                NSNumber* h = height;
                NSInteger hi = (NSInteger)[h intValue];

                CGRect bounds = CGRectMake(0, 0, wi, hi);
                b64 = [svg getDataURLwithBounds:bounds];
            }
        } else {
            ABI40_0_0RCTLogError(@"Invalid svg returned frin registry, expecting ABI40_0_0RNSVGSvgView, got: %@", view);
            return;
        }
        if (b64) {
            callback(@[b64]);
        } else if (attempt < 1) {
            void (^retryBlock)(void) = ^{
                [self toDataURL:ABI40_0_0ReactTag options:options callback:callback attempt:(attempt + 1)];
            };

            ABI40_0_0RCTExecuteOnUIManagerQueue(retryBlock);
        } else {
            callback(@[]);
        }
    }];
}

ABI40_0_0RCT_EXPORT_METHOD(toDataURL:(nonnull NSNumber *)ABI40_0_0ReactTag options:(NSDictionary *)options callback:(ABI40_0_0RCTResponseSenderBlock)callback)
{
    [self toDataURL:ABI40_0_0ReactTag options:options callback:callback attempt:0];
}

@end
