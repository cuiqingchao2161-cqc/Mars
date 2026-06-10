#include <jni.h>

#include <string>

#include <android/log.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_qx_qylogsample_MainActivity_nativeSmokeTest(JNIEnv* env, jobject /* thiz */) {
    __android_log_print(ANDROID_LOG_INFO, "QylogSample", "native smoke test");
    std::string message = "native module loaded";
    return env->NewStringUTF(message.c_str());
}
