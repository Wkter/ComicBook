#ifndef _CPAGEELEMENT_H_
#define _CPAGEELEMENT_H_

#include "CEngine.h"
#include <string>

class CPageElement{
public:
	CPageElement(){};
	~CPageElement(){};

	bool load(std::string filename);
	bool draw(int x, int y);

private:
	int texture;
	int width;
	int height;

	float vertices[4*3];
	float uv[2*4];
	int indices[6];
};

#endif 