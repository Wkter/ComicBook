precision mediump float;

varying vec2 uv;

uniform sampler2D texture;
uniform float sigma;    // Should be used to replace sigma_
uniform float blurSize; // Should be used to replace blurSize_

void main(void){
   vec4 sum = vec4(0.0);
 
   // blur in y (vertical)
   // take nine samples, with the distance blurSize between them
   sum += texture2D(texture, vec2(uv.x - 4.0*blurSize, uv.y)) * 0.05;
   sum += texture2D(texture, vec2(uv.x - 3.0*blurSize, uv.y)) * 0.09;
   sum += texture2D(texture, vec2(uv.x - 2.0*blurSize, uv.y)) * 0.12;
   sum += texture2D(texture, vec2(uv.x - blurSize, uv.y)) * 0.15;
   sum += texture2D(texture, vec2(uv.x, uv.y)) * 0.16;
   sum += texture2D(texture, vec2(uv.x + blurSize, uv.y)) * 0.15;
   sum += texture2D(texture, vec2(uv.x + 2.0*blurSize, uv.y)) * 0.12;
   sum += texture2D(texture, vec2(uv.x + 3.0*blurSize, uv.y)) * 0.09;
   sum += texture2D(texture, vec2(uv.x + 4.0*blurSize, uv.y)) * 0.05;
 
   gl_FragColor = sum;
}