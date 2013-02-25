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
    blurTexCoords[0] = uv + vec2(-5.0*blurSize, 0.0  );
    blurTexCoords[1] = uv + vec2(-4.0*blurSize, 0.0  );
    blurTexCoords[2] = uv + vec2(-3.0*blurSize, 0.0  );
    blurTexCoords[3] = uv + vec2(-2.0*blurSize, 0.0  );
    blurTexCoords[4] = uv + vec2( 2.0*blurSize, 0.0  );
    blurTexCoords[5] = uv + vec2( 3.0*blurSize, 0.0  );
    blurTexCoords[6] = uv + vec2( 4.0*blurSize, 0.0  );
    blurTexCoords[7] = uv + vec2( 5.0*blurSize, 0.0  );
}