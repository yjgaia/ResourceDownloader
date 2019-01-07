#import "ResourceDownloader.h"

@implementation ResourceDownloader

const int EVENT_OTHER_SOCIAL = 70;
extern int CreateDsMap( int _num, ... );
extern void CreateAsynEventWithDSMap(int dsmapindex, int event_index);

- (id) init
{
    tag = @"";
    url = @"";

    return self;
}

- (double) rd_init:(char *)tag Arg2:(char *)url
{
    self->tag = [[NSString stringWithUTF8String:tag] copy];
    self->url = [[NSString stringWithUTF8String:url] copy];
    
    return (double)1;
}

- (double) rd_download:(char *)filename
{
    NSString * folderPath = [NSString stringWithFormat:@"%@/%@", [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0], tag];
    NSString * filePath = [NSString stringWithFormat:@"%@/%@", folderPath, [NSString stringWithUTF8String:filename]];

    NSFileManager * fileManager = [NSFileManager defaultManager];
    
    // 폴더가 없으면 생성
    if ([fileManager fileExistsAtPath:folderPath] != YES) {
        [fileManager createDirectoryAtPath:folderPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    // 파일이 존재하지 않으면 다운로드
    if ([fileManager fileExistsAtPath:filePath] != YES) {
        NSData * data = [NSData dataWithContentsOfURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@resource/%@", url, [NSString stringWithUTF8String:filename]]]];
        [data writeToFile:filePath atomically:YES];
    }
    
    int dsMapIndex = CreateDsMap(2,
                                 "type", 0.0, "__RESOURCE_READY",
                                 "filename", 0.0, filename
                                 );
    
    CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
    
    return (double)-1;
}

@end

