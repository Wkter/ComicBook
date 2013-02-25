precision mediump float;

varying vec2 uv;

uniform sampler2D texture;
uniform float blurSize;

void main(void){
   vec4 sum = vec4(0.0);
 
   // blur in y (vertical)
   // take nine samples, with the distance blurSize between them
   sum += texture2D(texture, vec2(uv.x, uv.y - 4.0*blurSize)) * 0.05;
   sum += texture2D(texture, vec2(uv.x, uv.y - 3.0*blurSize)) * 0.09;
   sum += texture2D(texture, vec2(uv.x, uv.y - 2.0*blurSize)) * 0.12;
   sum += texture2D(texture, vec2(uv.x, uv.y - blurSize)) * 0.15;
   sum += texture2D(texture, vec2(uv.x, uv.y)) * 0.16;
   sum += texture2D(texture, vec2(uv.x, uv.y + blurSize)) * 0.15;
   sum += texture2D(texture, vec2(uv.x, uv.y + 2.0*blurSize)) * 0.12;
   sum += texture2D(texture, vec2(uv.x, uv.y + 3.0*blurSize)) * 0.09;
   sum += texture2D(texture, vec2(uv.x, uv.y + 4.0*blurSize)) * 0.05;
 
   gl_FragColor = sum;
}