precision mediump float;
 
uniform sampler2D texture;
 
varying vec2 uv;
varying vec2 blurTexCoords[8];
 
void main(){
    vec4 color = vec4(0.0);                       // 012345678
    //color += texture2D(texture, blurTexCoords[0])*0.004429912;
    //color += texture2D(texture, blurTexCoords[1])*0.008957812;
    //color += texture2D(texture, blurTexCoords[2])*0.021596386;
    color += texture2D(texture, blurTexCoords[0])*0.044368333;
    color += texture2D(texture, blurTexCoords[1])*0.077674421;
    color += texture2D(texture, blurTexCoords[2])*0.115876621;
    color += texture2D(texture, blurTexCoords[3])*0.147308056;
    color += texture2D(texture, uv              )*0.159576912;
    color += texture2D(texture, blurTexCoords[4])*0.147308056;
    color += texture2D(texture, blurTexCoords[5])*0.115876621;
    color += texture2D(texture, blurTexCoords[6])*0.077674421;
    color += texture2D(texture, blurTexCoords[7])*0.044368333;
    //color += texture2D(texture, blurTexCoords[11])*0.021596386;
    //color += texture2D(texture, blurTexCoords[12])*0.008957812;
    //color += texture2D(texture, blurTexCoords[13])*0.004429912;
    gl_FragColor = color;
}