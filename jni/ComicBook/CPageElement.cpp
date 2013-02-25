#include "CPageElement.h"
#include "utils.h"

bool CPageElement::load(std::string filename){

	// Load the texture
	int width, height, textureSize;
	texture = loadTextureFromPNG(filename.c_str(), width, height, textureSize);

	// Set the vertices
	float vertices_[4*3] = {
		 0.0f,   0.0f, 0.0f,
		 0.0f, height, 0.0f,
		width, height, 0.0f,
		width,   0.0f, 0.0f
	};
	for(int i = 0; i < 4*3; i++)
		vertices[i] = vertices_[i];

	// Calculate texture coordinates
	float _x1 = (1.0f/(float)textureSize)*(float)0;
	float _x2 = (1.0f/(float)textureSize)*(float)width;
	float _y1 = (1.0f/(float)textureSize)*(float)0;
	float _y2 = (1.0f/(float)textureSize)*(float)height;

	// Copy the calculated coordinates
	float uv_[2*4] = {
		_x1    , _y1,
		_x1    , _y1+_y2,
		_x1+_x2, _y1+_y2,
		_x1+_x2, _y1
	};
	for(int i = 0; i < 2*4; i++)
		uv[i] = uv_[i];

	// Copy default indices for a 2D plane
	int indices_[6] = {
		0, 1, 2, 0, 2, 3
	};
	for(int i = 0; i < 6; i++)
		indices_[i] = indices_[i];

	// Everything went smooth! :3
	return true;
};

bool CPageElement::draw(int x, int y){
	// Add program to OpenGL ES environment
	glUseProgram(simpleProgram);
	
	// Use the vertex buffer
	glEnableVertexAttribArray(simplePositionHandle);
	glVertexAttribPointer(simplePositionHandle, 3, GL_FLOAT, false, 0, vertexBuffer);
	
	// Set uniforms
	glUniformMatrix4fv(simpleModelMatrixHandle, 1, false, scaleMatrix, 0); // Projection matrix uniform
	
	// Bind texture to the fragment shader
	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, simpleTextureHandle);
	glUniform1i(simpleTextureHandle, 0);
	
	// Use the UV coordinate buffer
	glEnableVertexAttribArray(simpleTextureCoordinates);
	glVertexAttribPointer(simpleTextureCoordinates, 2, GL_FLOAT, false, 0, textureBuffer);
	
	// Draw the object
	glDrawElements(GL_TRIANGLES, drawOrder.length, GL_UNSIGNED_SHORT, drawListBuffer);
	
	// Clean up
	glDisableVertexAttribArray(simpleTextureCoordinates);
	glDisableVertexAttribArray(simplePositionHandle);
};