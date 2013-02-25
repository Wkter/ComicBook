#ifndef _CENGINE_H_
#define _CENGINE_H_

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "Matrix.h"
#include "CPageElement.h"
#include <vector>

class CEngine{
public:
	
	bool initialize(int width, int height);
	bool draw();

	Matrix4 projectionMatrix;
	
	//std::vector<CPageElement*> elements;
};


#endif