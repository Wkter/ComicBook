//#include "Random.h"

#ifndef UTILS_H_
#define UTILS_H_

#include <GLES/gl.h>

// Open the APK
void loadAPK(const char* apkPath);

// Load a file into a char array
int loadFile(const char* filename, char* &mem, bool nullTerminate = false);

//Filename will be looked up in the apk (should start with assets/ or res/
int loadTextureFromPNG(const char* filename, int &width, int &height, int &textureSize);

// Handy function from Windows!
unsigned long GetTickCount();

// Math functions
bool isPowerOfTwo(int x);
int  roundNearestPowerOfTwo(int x);

// Keep this public to ensure more random numbers are generated
//RANDOMGENERATOR Wkter;

#endif /* UTILS_H_ */
