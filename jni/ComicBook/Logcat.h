#include <android/log.h>

#ifndef _LOGCAT_H_
#define _LOGCAT_H_

// Just some shart-hand versions for logging
#define LOGF(tag, fmt, ...) __android_log_print(ANDROID_LOG_FATAL, tag, fmt, ##__VA_ARGS__);
#define LOGE(tag, fmt, ...) __android_log_print(ANDROID_LOG_ERROR, tag, fmt, ##__VA_ARGS__);
#define LOGW(tag, fmt, ...) __android_log_print(ANDROID_LOG_WARN, tag, fmt, ##__VA_ARGS__);
#define LOGI(tag, fmt, ...) __android_log_print(ANDROID_LOG_INFO, tag, fmt, ##__VA_ARGS__);
#define LOGD(tag, fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, tag, fmt, ##__VA_ARGS__);
#define LOGV(tag, fmt, ...) __android_log_print(ANDROID_LOG_VERBOSE, tag, fmt, ##__VA_ARGS__);

#endif