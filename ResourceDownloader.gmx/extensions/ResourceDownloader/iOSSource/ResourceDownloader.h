#import <AVFoundation/AVFoundation.h>

@interface ResourceDownloader : NSObject
{
    NSString * tag;
    NSString * url;
}

- (double) rd_init:(char *)tag Arg2:(char *)url;
- (double) rd_download:(char *)filename;

@end
