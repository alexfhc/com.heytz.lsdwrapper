#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "HiJoine.h"
#import "GCDAsyncUdpSocket.h"

@interface lsdwrapper : CDVPlugin <HiJoineDelegate> {
    // Member variables go here.
    HiJoine * joine;
    GCDAsyncUdpSocket *gcdUdpSocket;
    GCDAsyncUdpSocket *gcdUdpSocketsender;
    //EASYLINK *easylink_config;
    NSMutableDictionary *deviceIPConfig;
    NSString *loginID;
    CDVInvokedUrlCommand * commandHolder;
    NSString *deviceIp ;
    NSString *para ;
    NSString *userToken ;
    NSString *mac ;
    NSString *device_id ;
    int acitvateTimeout;
    NSString* activatePort;
    NSString* bssid;
    BOOL isSent;
    BOOL isRecieved;
    long tag;
    //
    NSString* deviceLoginId;
    NSString* devicePass;
//    NSTimer * _timer;
}
- (void)setDeviceWifi:(CDVInvokedUrlCommand*)command;
- (void)dealloc:(CDVInvokedUrlCommand*)command;
- (void)startUDPServer:(CDVInvokedUrlCommand*)command;
- (void)sendUDPData:(CDVInvokedUrlCommand*)command;
@end

//static long  _times = 0;

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
    commandHolder = command;
    isSent=false;
    isRecieved=false;
    if (joine !=nil) {
        [joine cancelBoardData];
    }
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
//    _times = 0;
//    _timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(counteTime) userInfo:nil repeats:YES];
//    [_timer fire];
    joine = [[HiJoine alloc] init];
    joine.delegate = self;
        [joine setBoardDataWithPassword:wifiKey withBackBlock:^(NSInteger result, NSString *message) {
            if (result == 1) {
                @try{
                    NSDictionary *ret = [NSDictionary dictionaryWithObjectsAndKeys:
                                         message, @"mac",
                                         nil];
                    if(!isSent)
                    {
                        [self startUDPServer];
                       isSent=true;
                        
                        mac=[ret objectForKey:@"mac"];
                    }
                    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[ret objectForKey:@"mac"]];
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

- (void)startUDPServer{
    gcdUdpSocket=[[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
    NSError *error;
    
    [gcdUdpSocket bindToPort:19530 error:&error];
    
    if (nil != error) {
        NSLog(@"failed.:%@",[error description]);
    }
 
    [gcdUdpSocket enableBroadcast:YES error:&error];
    
    if (nil != error) {
        NSLog(@"failed.:%@",[error description]);
    }
    [gcdUdpSocket beginReceiving:&error];
    
    if (nil != error) {
        NSLog(@"failed.:%@",[error description]);
    }
    para=[[[[[[[[@"{\"app_id\":\"" stringByAppendingString:@"123123c45213cg2454354hg"] stringByAppendingString:@"\",\"product_key\":\""]stringByAppendingString:@"0665bc7a5a62cc538070373cc507446b"]stringByAppendingString:@"\",\"user_token\":\""]stringByAppendingString:@"69c09f9d-3920-4d4a-ab7e-1f4e2827c2f2"] stringByAppendingString:@"\",\"uid\":\""]stringByAppendingString:@"69c09f9d-3920-4d4a-ab7e-1f4e2827c2f2"]stringByAppendingString:@"\"}"];
    NSData *data = [para dataUsingEncoding:NSUTF8StringEncoding];
    [self broadcastData:data];
}

- (void)broadcastData:(NSData*)data{
    if(gcdUdpSocketsender==nil){
    gcdUdpSocketsender=[[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
    }
    NSError *error;
    [gcdUdpSocketsender bindToPort:19531 error:&error];
    
    if (nil != error) {
        NSLog(@"failed.:%@",[error description]);
    }
    
    [gcdUdpSocketsender enableBroadcast:YES error:&error];
    
    if (nil != error) {
        NSLog(@"failed.:%@",[error description]);
    }
//    [gcdUdpSocketsender beginReceiving:&error];
//    
//    if (nil != error) {
//        NSLog(@"failed.:%@",[error description]);
//    }
    
//    NSData *data = [para dataUsingEncoding:NSUTF8StringEncoding];
    [self send:(NSData *)data];
}

-(void)send:(NSData*)data{
    [gcdUdpSocketsender sendData:data toHost:@"192.168.1.255" port:19531 withTimeout:-1 tag:tag];
    tag++;
}

- (void)udpSocket:(GCDAsyncUdpSocket *)sock didReceiveData:(NSData *)data fromAddress:(NSData *)address withFilterContext:(id)filterContext
{
    NSLog(@"Reciv Data len:%lu",(unsigned long)[data length]);
    if(!isRecieved)
    {
        NSError *err;
        NSDictionary *ret = [NSJSONSerialization JSONObjectWithData:data
                                              options:NSJSONReadingMutableContainers
                                                error:&err];
        device_id=[[@"{\"did\":\"" stringByAppendingString:[ret objectForKey:@"device_id"]] stringByAppendingString:@"\"}"] ;
        NSData *did= [device_id dataUsingEncoding:NSUTF8StringEncoding];
        [self broadcastData:did];
        isRecieved=true;
    }
}


- (void)udpSocketDidClose:(GCDAsyncUdpSocket *)sock withError:(NSError *)error
{
    NSLog(@"udpSocketDidClose Error:%@",[error description]);
}

- (void)dealloc:(CDVInvokedUrlCommand*)command
{
    NSLog(@"//====dealloc...====");
    if (joine !=nil) {
        [joine cancelBoardData];
    }
//    if (gcdUdpSocket) {
//        [gcdUdpSocket close];
//    }
//    if (gcdUdpSocketsender) {
//        [gcdUdpSocketsender close];
//    }

}
- (void)startUDPServer:(CDVInvokedUrlCommand*)command
{
    [self startUDPServer];
}
- (void)sendUDPData:(CDVInvokedUrlCommand*)command
{
//    [self broadcastData];
}
//- (void)endSending
//{
//    [_timer invalidate];
//    _timer = nil;
//}
//
//- (void)counteTime
//{
//    _times ++;
//}

@end