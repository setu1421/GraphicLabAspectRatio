/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.airhockey.android;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static android.opengl.Matrix.multiplyMM;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.airhockey.android.util.LoggerConfig;
import com.airhockey.android.util.MatrixHelper;
import com.airhockey.android.util.ShaderHelper;
import com.airhockey.android.util.TextResourceReader;

public class AirHockeyRenderer implements Renderer {    
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";

    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private int uMatrixLocation;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;    
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = 
        (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    
    private final FloatBuffer vertexData;
    private final Context context;

    private int program;
    private int aPositionLocation;
    private int aColorLocation;
    private float[] existingPoints = {
            // Order of coordinates: X, Y, R, G, B
            /*
            // Triangle Fan
             0.5f, 0.5f,   1f,   1f,   1f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
             0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
             0.5f,  0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f,  0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,

            // Line 1
            -0.5f, 0f, 1f, 0f, 0f,
             0.5f, 0f, 1f, 0f, 0f,

            // Mallets
            0f, -0.25f, 0f, 0f, 1f,
            0f,  0.25f, 1f, 0f, 0f*/

            /*-1.0f, 0f, 1f, 1f, 1f, //0
            -0.9f, 0.1f, 1f, 1f, 1f, //1
            -0.4f, 0.2f, 1f, 1f, 1f, //2
            0.9f, 0.2f, 1f, 1f, 1f,  //3
            0.8f, 0f, 1f, 1f, 1f,  //4
            0.65f, 0f, 1f, 1f, 1f,  //5

            -0.2f, 0.2f, 1f, 1f, 1f, //6
            0f, 0.4f, 1f, 1f, 1f,  //7
            0.3f, 0.4f, 1f, 1f, 1f,  //8
            0.9f, 0.2f, 1f, 1f, 1f,  //9

            0f, 0.2f, 1f, 1f, 1f, //10
            0f, 0.4f, 1f, 1f, 1f,  //11
            0.3f, 0.4f, 1f, 1f, 1f, //12
            0.3f, 0f, 1f, 1f, 1f,  //13

            -0.3f, 0.2f, 1f, 1f, 1f,  //14
            0f, 0.44f, 1f, 1f, 1f,  //15
            0.4f, 0.44f, 1f, 1f, 1f,  //16
            0.9f, 0.2f, 1f, 1f, 1f,  //17

            -0.2f, 0.2f, 1f, 1f, 1f,  //18
            -0.2f, -0.1f, 1f, 1f, 1f,  //19
            0.2f, -0.1f, 1f, 1f, 1f,  //20
            0.3f, 0f, 1f, 1f, 1f,  //21

            -1f, 0f, 1f, 1f, 1f,  //22
            -0.86f, -0.14f, 1f, 1f, 1f,  //23
            -0.72f, -0.18f, 1f, 1f, 1f,  //24
            -0.5f, -0.2f, 1f, 1f, 1f,  //25
            0.4f, -0.2f, 1f, 1f, 1f,  //26
            0.62f, -0.18f, 1f, 1f, 1f,  //27
            0.74f, -0.1f, 1f, 1f, 1f,  //28
            0.8f, 0f, 1f, 1f, 1f, //29

            0.34f, 0f, 1f, 1f, 1f, //30
            -0.44f, 0f, 1f, 1f, 1f, //31
            -0.76f, 0f, 1f, 1f, 1f, //32
            -1.0f, 0f, 1f, 1f, 1f //33*/

            //line1
            -.42f,0f,1f,0f,0f,
            .32f,0f,1f,0f,0f,
            //Triangle 1
            -.42f,0f,0f,0f,0f,
            -.3f,0f,0f,0f,0f,
            -.26f,.1f,1f,1f,1f,
            //Triangle 2
            0.0f,.15f,0f,0f,0f,
            //Trinagle 3
            -.3f,0f,1f,1f,1f,
            -0.02f,0f,0f,0f,0f,
            //Trinalge 4
            0.0f,.15f,1f,1f,1f,
            0f,0f,0f,0f,0f,
            //Triangle 5
            0.02f,0f,.5f,.5f,.5f,
            //Triangle 6
            0.0f,.15f,0f,0f,0f,
            0.02f,0f,1f,1f,1f,
            .26f,.1f,1f,1f,1f,
            //Triangle 7
            0.02f,0f,1f,1f,1f,
            .32f,0f,0f,0f,0f,
            .26f,.1f,1f,1f,1f,
            //Triangle 8
            -.27f,-.15f,.3f,.3f,.4f,
            0f,0f,.3f,.3f,.4f,
            0f,-.15f,.9f,.9f,.9f,
            //Triangle 9
            -.27f,0f,.3f,.3f,.4f,
            -.27f,-.15f,.3f,.3f,.4f,
            0f,0f,.3f,.3f,.4f,
            //Triangle 10
            0f,-.15f,1f,1f,1f,
            .32f,0f,.3f,.3f,.4f,
            0f,0f,.3f,.3f,.4f,
            //Triangle 11
            .32f,0f,.3f,.3f,.4f,
            0f,-.15f,.9f,.9f,.9f,
            .35f,-.15f,.3f,.3f,.4f,
            //Triangle 12
            -.42f,0f,0f,0f,0f,
            -.26f,.1f,0f,0f,0f,
            -.9f,0f,.6f,.6f,.6f,
            //Triangle 13
            -.42f,0f,0f,0f,0f,
            -.45f,-.08f,0f,0f,0f,
            -.9f,0f,.6f,.6f,.6f,
            //Triangle 14
            -.9f,0f,.6f,.6f,.6f,
            -.68f,-.15f,0f,0f,0f,
            -.45f,-0.08f,0f,0f,0f,
            //Triangle 15
            -.9f,0f,.6f,.6f,.6f,
            -.9f,-.15f,.6f,.6f,.6f,
            -.68f,-.15f,0f,0f,0f,
            //Triangle 16
            -.45f,-.08f,.3f,.3f,.4f,
            -.27f,-.15f,.3f,.3f,.4f,
            -.27f,0f,.3f,.3f,.4f,
            //Triangle 17
            -.42f,0f,.3f,.3f,.4f,
            -.45f,-.08f,.3f,.3f,.4f,
            -.27f,0f,.3f,.3f,.4f,
            //triangle 18
            .32f,0f,.7f,.7f,.7f,
            .5f,.08f,.8f,.87f,.8f,
            .26f,.1f,.9f,.9f,.9f,
            //tri 19
            .5f,.08f,0f,0f,0f,
            .68f,-0.08f,0f,0f,0f,
            .9f,-0.02f,.6f,.6f,.6f,
            //tri 20
            .5f,.08f,.3f,.3f,.4f,
            .32f,0f,.3f,.3f,.4f,
            .32f,-0.01f,.3f,.3f,.4f,
            //tri 21
            .35f,-.15f,.1f,.1f,.1f,
            .42f,-.15f,.1f,.1f,.1f,
            .68f,-0.08f,.1f,.1f,.1f,
            //tri 22
            .35f,-.15f,.1f,.1f,.1f,
            .32f,-0.01f,.1f,.1f,.1f,
            .68f,-.08f,.1f,.1f,.1f,
            //tri 23
            .68f,-.08f,.1f,.1f,.1f,
            .5f,.08f,.1f,.1f,.1f,
            .3f,-.02f,.1f,.1f,.1f,
            //tri 24
            .68f,-.08f,0f,0f,0f,
            .9f,-0.02f,.6f,.6f,.6f,
            .85f,-.15f,.6f,.6f,.6f,
            //tri 25
            .9f,-0.02f,.6f,.6f,.6f,
            .85f,-.15f,.6f,.6f,.6f,
            .98f,-.1f,.6f,.6f,.6f,
//            //tire line 1
//            -.45f,-.17f,1f,0f,0f,
//            -.35f,-.17f,.3f,.3f,.4f,
//            -.45f,-.11f,.3f,.3f,.4f,
//            //tire line 2
//            -.45f,-.17f,.3f,.3f,.4f,
//            -.45f,-.25f,.3f,.3f,.4f,
//            -.55f,-.17f,.3f,.3f,.4f,
//            //tire line 3
//            -.45f,-.17f,.3f,.3f,.4f,
//            -.45f,-.25f,.3f,.3f,.4f,
//            -.35f,-.17f,.3f,.3f,.4f,
//            //tire line 4
//            -.45f,-.17f,.3f,.3f,.4f,
//            -.45f,-.11f,.3f,.3f,.4f,
//            -.55f,-.17f,.3f,.3f,.4f,
//            //tire2 tri 1
//            .68f,-0.11f,0f,0f,0f,
//            .58f,-0.17f,0f,0f,1f,
//            .78f,-0.17f,.3f,.3f,.4f,
//            //tire tri 2
//            .58f,-0.17f,.3f,.3f,.4f,
//            .78f,-0.17f,.3f,.3f,.4f,
//            .68f,-0.25f,.3f,.3f,.4f,
            //line gate
            0f,-.15f,.3f,.3f,.4f,
            0f,0f,.3f,.3f,.4f,
    };

    public AirHockeyRenderer(Context context) {
        this.context = context;

        float[] tableVerticesWithTriangles = appendFans();

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }
    private float[] appendFans() {
        ArrayList<Float> points = new ArrayList<>();
        for(int i=0; i<existingPoints.length; i++) {
            points.add(existingPoints[i]);
        }

        points.addAll(addWheel(-.45f, -0.2f, 0.1f));
        points.addAll(addWheel(0.68f, -0.2f, 0.1f));

        float[] pointsArray = new float[points.size()];
        int i;
        for(i=0; i<points.size(); i++) {
            pointsArray[i] = points.get(i);
        }
        return pointsArray;
    }

    private ArrayList<Float> addWheel(float centerX, float centerY, float radius) {
        ArrayList<Float> wheelPoints = new ArrayList<>();

        wheelPoints.add(centerX);
        wheelPoints.add(centerY);
        wheelPoints.add(1.0f);
        wheelPoints.add(1.0f);
        wheelPoints.add(1.0f);

        double hdist, vdist;
        double secondX = 0, secondY=0;

        for(int theta=0; theta<=360; theta+=18) {
            hdist = radius * Math.cos((double)theta);
            vdist = radius * Math.sin((double)theta);

            if(theta==0) {
                secondX = hdist;
                secondY = vdist;
            }
            wheelPoints.add((float)(centerX + hdist));
            wheelPoints.add((float)(centerY + vdist));
            wheelPoints.add(0.0f);
            wheelPoints.add(0.0f);
            wheelPoints.add(0.0f);
        }

        wheelPoints.add((float)secondX);
        wheelPoints.add((float)secondY);
        wheelPoints.add(0.0f);
        wheelPoints.add(0.0f);
        wheelPoints.add(0.0f);

        return wheelPoints;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 0.84f, 0.0f);

        String vertexShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper
            .compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);   
        
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, 
            false, STRIDE, vertexData);

        glEnableVertexAttribArray(aPositionLocation);     
        
        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_COLOR_LOCATION.
        vertexData.position(POSITION_COMPONENT_COUNT);        
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, 
            false, STRIDE, vertexData);        

        glEnableVertexAttribArray(aColorLocation);
    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     * 
     * @param width
     *            The new width, in pixels.
     * @param height
     *            The new height, in pixels.
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        /*final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
            // Landscape
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }*/

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);

        translateM(modelMatrix, 0, 0f, 0f, -3.5f);
        rotateM(modelMatrix, 0, 60f, 0f, 0f, 1f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        /*// Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw the table.        
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        // Draw the center dividing line.        
        glDrawArrays(GL_LINES, 6, 2);

        // Draw the first mallet.        
        glDrawArrays(GL_POINTS, 8, 1);

        // Draw the second mallet.
        glDrawArrays(GL_POINTS, 9, 1);*/
        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        //line1

        glDrawArrays(GL_LINES, 0, 2);

        //triangle1

        glDrawArrays(GL_TRIANGLES, 2, 3);

        //triangle 2

        glDrawArrays(GL_TRIANGLES, 3, 3);
        //triangle 3

        glDrawArrays(GL_TRIANGLES, 5, 3);
        //triangle4

        glDrawArrays(GL_TRIANGLES, 7, 3);
        //triangle 5

        glDrawArrays(GL_TRIANGLES, 8, 3);
        //triangle 6

        glDrawArrays(GL_TRIANGLES, 11, 3);
        //triangle 7

        glDrawArrays(GL_TRIANGLES, 14, 3);
        //triangle 8

        glDrawArrays(GL_TRIANGLES, 17, 3);
        //triangle 9

        glDrawArrays(GL_TRIANGLES, 20, 3);
        //triangle 10

        glDrawArrays(GL_TRIANGLES, 23, 3);
        //triangle 11

        glDrawArrays(GL_TRIANGLES, 26, 3);
        //triangle 12

        glDrawArrays(GL_TRIANGLES, 29, 3);
        //triangle 13

        glDrawArrays(GL_TRIANGLES, 32, 3);
        //trinagle 14

        glDrawArrays(GL_TRIANGLES, 35, 3);
        //trinagle 15

        glDrawArrays(GL_TRIANGLES, 38, 3);
        //trianlge 16

        glDrawArrays(GL_TRIANGLES, 41, 3);
        //traingle 17

        glDrawArrays(GL_TRIANGLES, 44, 3);
        //tri 18

        glDrawArrays(GL_TRIANGLES, 47, 3);
        //tri 19

        glDrawArrays(GL_TRIANGLES, 50, 3);
        //tri 20

        glDrawArrays(GL_TRIANGLES, 53, 3);
        //tri 21

        glDrawArrays(GL_TRIANGLES, 56, 3);
        //tri 22

        glDrawArrays(GL_TRIANGLES, 59, 3);
        //tri 23

        glDrawArrays(GL_TRIANGLES, 62, 3);
        //tri 24

        glDrawArrays(GL_TRIANGLES, 65, 3);
        //tri 25

        glDrawArrays(GL_TRIANGLES, 68, 3);
        //line 26


//
//        glDrawArrays(GL_TRIANGLES, 71, 3);
//        //tire line 2
//
//        glDrawArrays(GL_TRIANGLES, 74, 3);
//        //tire line 3
//
//        glDrawArrays(GL_TRIANGLES, 77, 3);
//        //tire line 4
//
//        glDrawArrays(GL_TRIANGLES, 80, 3);
//        //tire 2 line 1
//
//        glDrawArrays(GL_TRIANGLES, 83, 3);
//        //tire 2 line 2

//        glDrawArrays(GL_TRIANGLES, 86, 3);
        //line gate

        glDrawArrays(GL_LINES, 71, 2);


        glDrawArrays(GL_TRIANGLE_FAN, 73, 22);

        glDrawArrays(GL_TRIANGLE_FAN, 96, 22);




    }
}