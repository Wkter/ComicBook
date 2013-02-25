/* HBlurVertexShader.glsl */

uniform mat4 modelMatrix;
uniform float blurSize;
attribute vec4 vertexPosition;
attribute vec2 textureCoordinates;
 
varying vec2 uv;
varying vec2 blurTexCoords[8];
 
void main(){
    gl_Position = modelMatrix * vertexPosition;
    uv = textureCoordinates;
    blurTexCoords[0] = uv + vec2(0.0, -5.0*blurSize);
    blurTexCoords[1] = uv + vec2(0.0, -4.0*blurSize);
    blurTexCoords[2] = uv + vec2(0.0, -3.0*blurSize);
    blurTexCoords[3] = uv + vec2(0.0, -2.0*blurSize);
    blurTexCoords[4] = uv + vec2(0.0,  2.0*blurSize);
    blurTexCoords[5] = uv + vec2(0.0,  3.0*blurSize);
    blurTexCoords[6] = uv + vec2(0.0,  4.0*blurSize);
    blurTexCoords[7] = uv + vec2(0.0,  5.0*blurSize);
}