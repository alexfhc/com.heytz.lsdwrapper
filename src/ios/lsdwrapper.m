#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "HiJoine.h"

@interface lsdwrapper : CDVPlugin <HiJoineDelegate> {
    // Member variables go here.
    HiJoine * joine;
    //EASYLINK *easylink_config;
    NSMutableDictionary *deviceIPConfig;
    NSString *loginID;
    CDVInvokedUrlCommand * commandHolder;
    NSString *deviceIp ;
    NSString *userToken ;
    int acitvateTimeout;
    NSString* activatePort;
    NSString* bssid;
    //
    NSString* deviceLoginId;
    NSString* devicePass;
}
- (void)setDeviceWifi:(CDVInvokedUrlCommand*)command;
@end

@implementation lsdwrapper

-(void)pluginInitialize{

}

- (void)setDeviceWifi:(CDVInvokedUrlCommand*)command
{

    NSString* wifiSSID = [command.arguments objectAtIndex:0];
    NSString* wifiKey = [command.arguments objectAtIndex:1];
    loginID = [command.arguments objectAtIndex:2];
    deviceLoginId = [command.arguments objectAtIndex:6];
    devicePass = [command.arguments objectAtIndex:7];
    int easylinkVersion;
    activatePort = [command.arguments objectAtIndex:5];

    if ([command.arguments objectAtIndex:3] == nil || [command.arguments objectAtIndex:4] == nil) {
        NSLog(@"Error: arguments easylink_version & timeout");
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }else {
        easylinkVersion = [[command.arguments objectAtIndex:3] intValue];
        acitvateTimeout = [[command.arguments objectAtIndex:4] intValue];
    }

    if (wifiSSID == nil || wifiSSID.length == 0 || wifiKey == nil || wifiKey.length == 0 || loginID == nil || loginID.length == 0 || activatePort==nil || activatePort.length == 0 || deviceLoginId == nil || deviceLoginId.length == 0
        || devicePass == nil || devicePass.length==0) {
        NSLog(@"Error: arguments");
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    HiJoine *joine = [[HiJoine alloc] init];
        [joine setBoardDataWithPassword:wifiKey withBackBlock:^(NSInteger result, NSString *message) {
            if (result == 1) {
                @try{
                    NSDictionary *ret = [NSDictionary dictionaryWithObjectsAndKeys:
                                         message, @"mac",
                                         nil];
                    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:ret];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
                }
                @catch (NSException *e){
                    NSLog(@"error - save configuration..." );
                    CDVPluginResult *pluginResult = nil;
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
                }
                // NSString *successStr = [NSString stringWithFormat:@"MAC地址 %@ 连接成功，耗时 %ld 秒", message];
            }else{
                CDVPluginResult *pluginResult = nil;
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
            }
        }];

}

@end