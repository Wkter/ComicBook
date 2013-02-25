/* -------------------------------*\
*  Matrix.h
*
*  Definition Header 
*
*  Matrix functions have been found here:
*  http://db-in.com/blog/2011/04/cameras-on-opengl-es-2-x/
*  
*  Copyright (C) Wkter, 2012
*  All rights reserved.
*
\* -------------------------------*/

#ifndef _MATRIX_H_
#define _MATRIX_H_

#define pi 3.14159265

typedef float Matrix4[16];
 
// General purpose math functions
float degreesToRadians(float degree);

// Matrix functions
void matrixIdentity    (Matrix4 m);
void matrixTranslate   (float x,       float y,    float z,  Matrix4 matrix);
void matrixScale       (float sx,      float sy,   float sz, Matrix4 matrix);
void matrixRotateX     (float degrees, Matrix4 matrix);
void matrixRotateY     (float degrees, Matrix4 matrix);
void matrixRotateZ     (float degrees, Matrix4 matrix);
void matrixMultiply    (Matrix4 m1,    Matrix4 m2, Matrix4 result);
void matrixOrtho       (Matrix4 m, float left, float right, float bottom, float top, float near, float far);
void matrixPerspective (Matrix4 m, float angle, float aspect, float near, float far);

#endif