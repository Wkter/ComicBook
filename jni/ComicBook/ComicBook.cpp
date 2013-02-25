#include "ComicBook.h"
#include "CEngine.h"
#include <string>

JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_initializeNative(JNIEnv *env, jclass c, jint width, jint height, jstring apk_path){
	LOGE("Native", "Finally free, from the chains of %s!", "Java");
	glViewport(0, 0, width, height);

	jboolean isCopy;
	std::string apk_path_(env->GetStringUTFChars(apk_path, &isCopy));
	loadAPK(apk_path_.c_str());
	
	return 0;
}

JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_resizeNative(JNIEnv *end, jclass c, jint width, jint height){
	glViewport(0, 0, width, height);
};

JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_drawNative(JNIEnv *, jclass){
}


JNIEXPORT jint JNICALL Java_li_alo_comicbook_CBEngine_drawSquareNative(JNIEnv *, jclass, jint program){
	int sigma = glGetUniformLocation(program, "sigma_");
	int blurSize = glGetUniformLocation(program, "blurSize_");
	glUniform1f(sigma, 3.0f); // Blur amount
	glUniform1f(blurSize, 0.0016835016f);
	LOGE("Native", "program = %i, sigma = %i, blurSize = %i", program, sigma, blurSize);
}
	
// EOF