#include <cstring>
#include <string>

#include "gtest/gtest.h"
#include "mars/xlog/appender.h"
#include "mars/xlog/xlogger_interface.h"
#include "mars/comm/xlogger/xloggerbase.h"

using namespace testing;

static int calc_dump_required_length(int srcbytes) {
    // MUST CHANGE THIS IF YOU CHANGE `to_string` function.
    return srcbytes * 6 + 1;
}

TEST(appender, memorydump) {
    char srcbuffer[4096];
    char dummybuf[64];

    const char* dump1 = xlogger_memory_dump(srcbuffer, 1);
    int head_bytes = snprintf(dummybuf, sizeof(dummybuf), "\n%zu bytes:\n", 1);
    EXPECT_EQ(strlen(dump1), calc_dump_required_length(1) + head_bytes + 1);  // 1 for '\n'

    dump1 = xlogger_memory_dump(srcbuffer, 121);
    head_bytes = snprintf(dummybuf, sizeof(dummybuf), "\n%zu bytes:\n", 121);
    int round = 121 / 32 + 1;  // +1 because not aligned to 32
    EXPECT_EQ(strlen(dump1), calc_dump_required_length(121) + head_bytes + round * 2 - 1);

    dump1 = xlogger_memory_dump(srcbuffer, 128);
    head_bytes = snprintf(dummybuf, sizeof(dummybuf), "\n%zu bytes:\n", 128);
    round = 128 / 32;
    EXPECT_EQ(strlen(dump1), calc_dump_required_length(128) + head_bytes + round * 2 - 1);

    EXPECT_GT(calc_dump_required_length(4096), 4096);

    dump1 = xlogger_memory_dump(srcbuffer, 4096);
    EXPECT_LT(strlen(dump1), 4096);

    std::string sdump1(dump1);
    const char* dump2 = xlogger_memory_dump(srcbuffer, 673);
    EXPECT_EQ(sdump1.length(), strlen(dump2) + 1);
}

TEST(xlogger_interface, instance_key_includes_dirs_and_prefix) {
    mars::xlog::XLogConfig first;
    first.cachedir_ = "/tmp/qylog-cache-a";
    first.logdir_ = "/tmp/qylog-log-a";
    first.nameprefix_ = "CAN_BUS";

    mars::xlog::XLogConfig second;
    second.cachedir_ = "/tmp/qylog-cache-b";
    second.logdir_ = "/tmp/qylog-log-b";
    second.nameprefix_ = "CAN_BUS";

    EXPECT_NE(mars::xlog::MakeXloggerInstanceKey(first), mars::xlog::MakeXloggerInstanceKey(second));
}

TEST(appender, binary_record_preserves_zero_bytes) {
    const unsigned char payload[] = {0x10, 0x00, 0x11, 0x12};
    std::string record = mars::xlog::FormatBinaryLogRecord("can_frame", payload, sizeof(payload));

    EXPECT_NE(std::string::npos, record.find("type=binary"));
    EXPECT_NE(std::string::npos, record.find("tag=can_frame"));
    EXPECT_NE(std::string::npos, record.find("length=4"));
    EXPECT_NE(std::string::npos, record.find(std::string(reinterpret_cast<const char*>(payload), sizeof(payload))));
}

EXPORT_GTEST_SYMBOLS(log_export_appender_unittest)
