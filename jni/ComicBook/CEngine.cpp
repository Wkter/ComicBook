#include "CEngine.h"
#include "Logcat.h"

bool CEngine::initialize(int width, int height){

	LOGW("CEngine", "Initializing.");

	// Setup the projection matrix
	matrixOrtho(projectionMatrix, 0, width, height, 0, -1.0f, 1.0f);

	CPageElement element;
	element.load("assets/twokinds_1_1_1.png");

	//elements.push_back(element);

	return true;
}

bool CEngine::draw(){
	glClear(GL_COLOR_BUFFER_BIT);
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	return true;
}
