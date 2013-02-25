/* -------------------------------*\
*  Matrix.cpp
*
*  Implementations
*
*  
*  Copyright (C) Wkter, 2012
*  All rights reserved.
*
\* -------------------------------*/

#include "Matrix.h"
#include <math.h>
#include "Logcat.h"

float degreesToRadians(float degree){
        float radian = 0;
        radian = degree * (pi/180);
        return radian;
}

void matrixIdentity(Matrix4 m){
    m[0] = m[5] = m[10] = m[15] = 1.0;
    m[1] = m[2] = m[3] = m[4] = 0.0;
    m[6] = m[7] = m[8] = m[9] = 0.0;
    m[11] = m[12] = m[13] = m[14] = 0.0;
}
 
void matrixTranslate(float x, float y, float z, Matrix4 matrix){
    matrixIdentity(matrix);
 
    // Translate slots.
    matrix[12] = x;
    matrix[13] = y;
    matrix[14] = z;
}
 
void matrixScale(float sx, float sy, float sz, Matrix4 matrix){
    matrixIdentity(matrix);
 
    // Scale slots.
    matrix[0] = sx;
    matrix[5] = sy;
    matrix[10] = sz;
}
 
void matrixRotateX(float degrees, Matrix4 matrix){
    float radians = degreesToRadians(degrees);
 
    matrixIdentity(matrix);
 
    // Rotate X formula.
    matrix[5] = cosf(radians);
    matrix[6] = -sinf(radians);
    matrix[9] = -matrix[6];
    matrix[10] = matrix[5];
}
 
void matrixRotateY(float degrees, Matrix4 matrix){
    float radians = degreesToRadians(degrees);
 
    matrixIdentity(matrix);
 
    // Rotate Y formula.
    matrix[0] = cosf(radians);
    matrix[2] = sinf(radians);
    matrix[8] = -matrix[2];
    matrix[10] = matrix[0];
}
 
void matrixRotateZ(float degrees, Matrix4 matrix){
    float radians = degreesToRadians(degrees);
 
    matrixIdentity(matrix);
 
    // Rotate Z formula.
    matrix[0] = cosf(radians);
    matrix[1] = sinf(radians);
    matrix[4] = -matrix[1];
    matrix[5] = matrix[0];
}

void matrixMultiply(Matrix4 m1, Matrix4 m2, Matrix4 result){
    // Fisrt Column
    result[0] = m1[0]*m2[0] + m1[4]*m2[1] + m1[8]*m2[2] + m1[12]*m2[3];
    result[1] = m1[1]*m2[0] + m1[5]*m2[1] + m1[9]*m2[2] + m1[13]*m2[3];
    result[2] = m1[2]*m2[0] + m1[6]*m2[1] + m1[10]*m2[2] + m1[14]*m2[3];
    result[3] = m1[3]*m2[0] + m1[7]*m2[1] + m1[11]*m2[2] + m1[15]*m2[3];
 
    // Second Column
    result[4] = m1[0]*m2[4] + m1[4]*m2[5] + m1[8]*m2[6] + m1[12]*m2[7];
    result[5] = m1[1]*m2[4] + m1[5]*m2[5] + m1[9]*m2[6] + m1[13]*m2[7];
    result[6] = m1[2]*m2[4] + m1[6]*m2[5] + m1[10]*m2[6] + m1[14]*m2[7];
    result[7] = m1[3]*m2[4] + m1[7]*m2[5] + m1[11]*m2[6] + m1[15]*m2[7];
 
    // Third Column
    result[8] = m1[0]*m2[8] + m1[4]*m2[9] + m1[8]*m2[10] + m1[12]*m2[11];
    result[9] = m1[1]*m2[8] + m1[5]*m2[9] + m1[9]*m2[10] + m1[13]*m2[11];
    result[10] = m1[2]*m2[8] + m1[6]*m2[9] + m1[10]*m2[10] + m1[14]*m2[11];
    result[11] = m1[3]*m2[8] + m1[7]*m2[9] + m1[11]*m2[10] + m1[15]*m2[11];
 
    // Fourth Column
    result[12] = m1[0]*m2[12] + m1[4]*m2[13] + m1[8]*m2[14] + m1[12]*m2[15];
    result[13] = m1[1]*m2[12] + m1[5]*m2[13] + m1[9]*m2[14] + m1[13]*m2[15];
    result[14] = m1[2]*m2[12] + m1[6]*m2[13] + m1[10]*m2[14] + m1[14]*m2[15];
    result[15] = m1[3]*m2[12] + m1[7]*m2[13] + m1[11]*m2[14] + m1[15]*m2[15];
}

void matrixOrtho(Matrix4 m, float left, float right, float bottom, float top, float near, float far){
	m[0] = 2.0f/(right-left);
	m[1] = 0.0f;
	m[2] = 0.0f;
	m[3] = 0.0f;//(right+left)/(right-left);

	m[4] = 0.0f;
	m[5] = 2.0f/(top-bottom);
	m[6] = 0.0f;
	m[7] = 0.0f;//(top+bottom)/(top-bottom);

	m[8] = 0.0f;
	m[9] = 0.0f;
	m[10] = -2.0f/(far-near);
	m[11] = 0.0f;//(far+near)/(far-near);

	m[12] = -(right+left)/(right-left);
	m[13] = -(top+bottom)/(top-bottom);
	m[14] = -(far+near)/(far-near);
	m[15] = 1.0f;
}

void matrixPerspective(Matrix4 m, float angle, float aspect, float near, float far){

	// Some calculus before the formula.
    float size = near * tanf(degreesToRadians(angle) / 2.0);
    float left = -size;
	float right = size;
	float bottom = -size / aspect;
	float top = size / aspect;
 
	// First Column
    m[0] = 2 * near / (right - left);
    m[1] = 0.0;
    m[2] = 0.0;
    m[3] = 0.0;
 
    // Second Column
    m[4] = 0.0;
    m[5] = 2 * near / (top - bottom);
    m[6] = 0.0;
    m[7] = 0.0;
 
    // Third Column
    m[8] = (right + left) / (right - left);
    m[9] = (top + bottom) / (top - bottom);
    m[10] = -(far + near) / (far - near);
    m[11] = -1;
 
    // Fourth Column
    m[12] = 0.0;
    m[13] = 0.0;
    m[14] = -(2 * far * near) / (far - near);
    m[15] = 0.0;
};