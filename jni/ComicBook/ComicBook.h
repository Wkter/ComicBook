#include <jni.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#ifndef _COMICBOOK_H_
#define _COMICBOOK_H_

#include "Logcat.h"
#include "utils.h"

// JNI function definition. Fuck me with a broomstick!
extern "C" {
	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_initializeNative(JNIEnv *, jclass, jint, jint, jstring);
	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_resizeNative(JNIEnv *, jclass, jint, jint);
	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_drawNative(JNIEnv *, jclass);
	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_drawSquareNative(JNIEnv *, jclass, jint);

	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_loadAPKTextureNative(JNIEnv *, jclass, jstring);
	JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_loadFileTextureNative(JNIEnv *, jclass, jstring);
}



#endif
