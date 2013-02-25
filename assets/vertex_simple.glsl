

uniform mat4 modelMatrix;
attribute vec4 vertexPosition;
attribute vec2 textureCoordinates;
varying vec2 uv;

void main() {
	gl_Position = modelMatrix * vertexPosition;
	uv = textureCoordinates;
}