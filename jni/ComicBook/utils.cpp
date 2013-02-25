#include <GLES/gl.h>
extern "C" {
#include "includes/png.h"
}
#include <stdio.h>
#include <ctime>
#include <sys/time.h>
#include "includes/zip.h"
#include "Logcat.h"
#include "utils.h"


#define TEXTURE_LOAD_ERROR 0
#define GL_RED 0x1903

// As we're in an Linux environment, there is no GetTickCount,
// so lets make our own!
unsigned long GetTickCount(){
	struct timeval tv;
	if(gettimeofday(&tv, NULL) != 0)
		return 0;

	return (tv.tv_sec * 1000) + (tv.tv_usec / 1000);
}

bool isPowerOfTwo(int x){
	return ( (x > 0) && ((x & (x - 1)) == 0) );
}

int roundNearestPowerOfTwo(int x){
    if (x < 0)
        return 0;
    --x;
    x |= x >> 1;
    x |= x >> 2;
    x |= x >> 4;
    x |= x >> 8;
    x |= x >> 16;
    return x+1;
}


//Taken from http://en.wikibooks.org/wiki/OpenGL_Programming/Intermediate/Textures
/** loadTexture
 *   loads a png file into an opengl texture object, using cstdio , libpng, and opengl.
 *
 *   \param filename : the png file to be loaded
 *   \param width : width of png, to be updated as a side effect of this function
 *   \param height : height of png, to be updated as a side effect of this function
 *
 *   \return GLuint : an opengl texture id.  Will be 0 if there is a major error,
 *           should be validated by the client of this function.
 *
 */
zip_file* file;
zip* APKArchive;

/*! /brief Load the content of an APK file.
 *
 * This static function will load a specified APK file
 * as a ZIP file, ready for use with libzip.
 */
void loadAPK(const char* apkPath){
	LOGI("Utils", "Loading APK %s", apkPath);
	APKArchive = zip_open(apkPath, 0, NULL);
	if (APKArchive == NULL) {
		LOGE("Utils", "WTF: Error loading APK");		// Send an What a Terrible Faliure error (Or What The Fuck error,
		return;								// depending on how keen you are at profanity)
	}

	//Just for debug, print APK contents
	int numFiles = zip_get_num_files(APKArchive);
	for (int i=0; i<numFiles; i++) {
		const char* name = zip_get_name(APKArchive, i, 0);
		if (name == NULL) {
			LOGE("Utils", "WTF: Error reading zip file name at index %i : %s", zip_strerror(APKArchive));
			return;
		}
		LOGI("Utils", "File %i : %s\n", i, name);
 	}
}

int loadFile(const char* filename, char* &mem, bool nullTerminate){
	file = zip_fopen(APKArchive, filename, 0);
	if (!file) {
		zip_fclose(file);
		//LOGE("Error opening %s from APK", filename);
		return 0;
	}
	struct zip_stat fileStat;
	zip_stat(APKArchive, filename, 0, &fileStat);

	int size = fileStat.size;

	if(nullTerminate)
		size++;

	mem = new char[size];

	zip_fread(file, mem, fileStat.size);

	mem[size-1] = 0;

	zip_fclose(file);
	return size;
};

void png_zip_read(png_structp png_ptr, png_bytep data, png_size_t length) {
	zip_fread(file, data, length);
}

int loadTextureFromPNG(const char* filename, int &width, int &height, int &textureSize) {
	file = zip_fopen(APKArchive, filename, 0);
	if (!file) {
		LOGE("Utils", "Error opening %s from APK", filename);
		return -1;
	}

	struct zip_stat fileStat;
	zip_stat(APKArchive, filename, 0, &fileStat);

	// Read the file header and check if this is a PNG file
	png_byte header[8];
	zip_fread(file, header, 8);
	if (png_sig_cmp(header, 0, 8)) {
		zip_fclose(file);
		LOGE("Utils", "Not a png file : %s", filename);
		return -1;
	}

	// Create the PNG struct
	png_structp png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
	if (!png_ptr){
		zip_fclose(file);
		LOGE("Utils", "Unable to create png struct : %s", filename);
		return (-1);
	}

	// Create a PNG info struct
	png_infop info_ptr = png_create_info_struct(png_ptr);
	if (!info_ptr){
		png_destroy_read_struct(&png_ptr, (png_infopp) NULL, (png_infopp) NULL);
		LOGE("Utils", "Unable to create png info : %s", filename);
		zip_fclose(file);
		return (-1);
	}

	// Create another PNG info struct
	png_infop end_info = png_create_info_struct(png_ptr);
	if (!end_info){
		png_destroy_read_struct(&png_ptr, &info_ptr, (png_infopp) NULL);
		LOGE("Utils", "Unable to create png end info : %s", filename);
		zip_fclose(file);
		return (-1);
	}

	//png error stuff, not sure libpng man suggests this.
	if (setjmp(png_jmpbuf(png_ptr))) {
		zip_fclose(file);
		LOGE("Utils", "Error during setjmp : %s", filename);
		png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		return (-1);
	}

	png_set_read_fn(png_ptr, NULL, png_zip_read); //init png reading
	png_set_sig_bytes(png_ptr, 8);                //let libpng know you already read the first 8 bytes
	png_read_info(png_ptr, info_ptr);             // read all the info up to the image data

	// IHDR info
	int bit_depth, color_type;
	png_uint_32 twidth, theight;
	png_get_IHDR(png_ptr, info_ptr, &twidth, &theight, &bit_depth, &color_type, NULL, NULL, NULL);
	width = twidth;
	height = theight;

	LOGW("Utils", "%s: %ix%ipx", filename, twidth, theight);

	// Anything to RGBA convert
	if(bit_depth == 16)                                    png_set_strip_16(png_ptr); // Tell libpng to strip 16 bit/color files down to 8 bits/color
	if(bit_depth < 8)                                      png_set_packing(png_ptr);
	if(color_type == PNG_COLOR_TYPE_PALETTE)               png_set_expand(png_ptr); // Expand paletted colors into true RGB triplets
	if(color_type == PNG_COLOR_TYPE_GRAY && bit_depth < 8) png_set_expand(png_ptr); // Expand grayscale images to the full 8 bits from 1, 2, or 4 bits/pixel
	if(color_type == PNG_COLOR_TYPE_GRAY)                  png_set_gray_to_rgb(png_ptr); // Convert graysclae to RGB
	if(color_type == PNG_COLOR_TYPE_GRAY_ALPHA)            png_set_gray_to_rgb(png_ptr); // Convert graysclae to RGB
	if(png_get_valid(png_ptr, info_ptr, PNG_INFO_tRNS))    png_set_expand(png_ptr); // Expand paletted or RGB images with transparency to full alpha channels so the data will be available as RGBA quartets

	png_read_update_info(png_ptr, info_ptr); // Update the png info struct.

	// Allocate a memory block to hold the decompressed image
	int rowbytes = png_get_rowbytes(png_ptr, info_ptr);
	png_byte *image_data = new png_byte[rowbytes * height];
	if (!image_data) {
		png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		LOGE("Utils", "Unable to allocate image_data while loading %s ", filename);
		zip_fclose(file);
		return -1;
	}

	if(rowbytes == 0 || rowbytes/4 != twidth)
		LOGE("Utils", "Unmatching rowbytes (%i) to texture width (%i)", rowbytes, twidth);

	// row_pointers is for pointing to image_data for reading the png with libpng
	png_bytep *row_pointers = new png_bytep[height];
	if (!row_pointers) {
		png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		delete[] image_data;
		LOGE("Utils", "Unable to allocate row_pointer while loading %s ", filename);
		zip_fclose(file);
		return -1;
	}

	// Set the individual row_pointers to point at the correct offsets of image_data
	for (int i = 0; i < height; ++i)
		row_pointers[height - 1 - i] = image_data + i * rowbytes;

	png_read_image(png_ptr, row_pointers); // Read the png into image_data through row_pointers

	// Convert to pow2
	unsigned int newSize = roundNearestPowerOfTwo(width>height?width:height);
	unsigned char *pow2Image = new unsigned char[newSize * newSize * 4];
	for(int i = 0; i < (newSize * newSize * 4); i++)
		pow2Image[i] = 0;
	unsigned int newRowsize = width*4;
	for(unsigned int i = 0; i < height; i++){
		for(unsigned int j = 0; j < newRowsize; j++){
			pow2Image[(i*newRowsize)+j] = image_data[(i*newRowsize)+j];
		}
	}

	LOGW("Utils", "%s: %ix%ipx", filename, newSize, newSize);

	// Generate a GL texture and load the PNG image into it
	GLuint texture;
	glGenTextures(1, &texture);
	glBindTexture(GL_TEXTURE_2D, texture);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, newSize, newSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, (GLvoid*)pow2Image);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); //GL_NEAREST);//

	if(texture == 0){
		LOGE("Utils", "A major error has occured while loading %s ", filename);
	}else{
		LOGI("Utils", "Loaded texture ID: %i - %s ", texture, filename);
	}
	
	//clean up memory and close stuff
	png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
	delete [] image_data;
	delete [] pow2Image;
	delete [] row_pointers;
	zip_fclose(file);

	textureSize = newSize;
	
	return texture;
}
