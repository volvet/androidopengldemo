package com.example.opengldemo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public class ColorCube implements SurfaceTexture.OnFrameAvailableListener {

	private FloatBuffer mVertexBuffer;
	private FloatBuffer mColorBuffer;
	private ByteBuffer  mIndexBuffer;
    private FloatBuffer texBuffer;    // Buffer for texture-coords-array (NEW)
    private FloatBuffer texBufferLR;
    private FloatBuffer texBufferTB;
	private Camera      camera;
	private GLSurfaceView view;
    private int[] textureIDs = new int[1];
    private SurfaceTexture  surfaceTexture;
    private final static int previewWidth = 1280;
    private final static int previewHeight = 720;

	/*private float vertices[] = {
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,
			1.0f,  1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,
			-1.0f, -1.0f,  1.0f,
			1.0f, -1.0f,  1.0f,
			1.0f,  1.0f,  1.0f,
			-1.0f,  1.0f,  1.0f
	};*/
    private float vertices[] = {
            -1.0f, -1.0f, 1.0f,  // 0. left-bottom-front
            1.0f, -1.0f, 1.0f,  // 1. right-bottom-front
            -1.0f,  1.0f, 1.0f,  // 2. left-top-front
            1.0f,  1.0f, 1.0f,   // 3. right-top-front

            -1.0f, -1.0f,  -1.0f,
            1.0f, -1.0f,   -1.0f,
            -1.0f,  1.0f,  -1.0f,
            1.0f,  1.0f,  -1.0f,
    };
	private float colors[] = {
			0.0f,  1.0f,  0.0f,  1.0f,
			0.0f,  1.0f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.5f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			1.0f,  0.0f,  0.0f,  1.0f,
			0.0f,  0.0f,  1.0f,  1.0f,
			1.0f,  0.0f,  1.0f,  1.0f
	};

	/*private byte indices[] = {
			0, 4, 5, 0, 5, 1,
			1, 5, 6, 1, 6, 2,
			2, 6, 7, 2, 7, 3,
			3, 7, 4, 3, 4, 0,
			4, 7, 6, 4, 6, 5,
			3, 0, 1, 3, 1, 2
	};*/
    private byte indices[] = {
            0, 1, 2, 2, 1, 3,
            4, 5, 6, 6, 5, 7,

            0, 4, 2, 2, 4, 6,
            1, 5, 3, 3, 5, 7,

            2, 3, 6, 6, 3, 7,
            0, 1, 4, 4, 1, 5
    };

    float[] texCoords = { // Texture coords for the above face (NEW)
            0.0f, 1.0f,  // A. left-bottom (NEW)
            1.0f, 1.0f,  // B. right-bottom (NEW)
            0.0f, 0.0f,  // C. left-top (NEW)
            1.0f, 0.0f,   // D. right-top (NEW)

            0.0f, 1.0f,  // A. left-bottom (NEW)
            1.0f, 1.0f,  // B. right-bottom (NEW)
            0.0f, 0.0f,  // C. left-top (NEW)
            1.0f, 0.0f,   // D. right-top (NEW)
    };

    float[] texCoords_lr = {
            0.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 0.0f,

            1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 0.0f,
    };

    float[] texCoords_tb = {
            0.0f,   1.0f,
            1.0f,   1.0f,
            0.0f,   1.0f,
            1.0f,   1.0f,

            0.0f,   0.0f,
            1.0f,   0.0f,
            0.0f,   0.0f,
            1.0f,   0.0f,
    };

	public ColorCube() {
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mColorBuffer = byteBuf.asFloatBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asFloatBuffer();
        texBuffer.put(texCoords);
        texBuffer.position(0);

        tbb = ByteBuffer.allocateDirect(texCoords_lr.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texBufferLR = tbb.asFloatBuffer();
        texBufferLR.put(texCoords_lr);
        texBufferLR.position(0);

        tbb = ByteBuffer.allocateDirect(texCoords_tb.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texBufferTB = tbb.asFloatBuffer();
        texBufferTB.put(texCoords_tb);
        texBufferTB.position(0);
	}

    public void loadTexture(GL10 gl, GLSurfaceView view) {
        gl.glGenTextures(1, textureIDs, 0); // Generate texture-ID array
        OpenGLRenderer.checkGLError(gl, "glGenTextures");

        gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIDs[0]);   // Bind to texture ID
        OpenGLRenderer.checkGLError(gl, "glBindTexture");
        // Set up texture filters
        gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        OpenGLRenderer.checkGLError(gl, "glTexParameterf");

        //now, it is in thread of GLSurfaceView
        surfaceTexture = new SurfaceTexture(textureIDs[0]);
        surfaceTexture.setOnFrameAvailableListener(this);

        this.view = view;
        camera = Camera.open(1);
        Log.d("CameraCube", "open camera OK");
        Camera.Parameters param = camera.getParameters();
        param.setPreviewSize(previewWidth, previewHeight);
        camera.setParameters(param);

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();
        Log.d("CameraCube", "start preview OK");

    }

	public void draw(GL10 gl) {
        surfaceTexture.updateTexImage();

        gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);  // Enable texture (NEW), specific for OpenGL1.x
        gl.glActiveTexture(gl.GL_TEXTURE0);
        gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIDs[0]);   // Bind to texture ID
        OpenGLRenderer.checkGLError(gl, "glBindTexture");

		//gl.glFrontFace(GL10.GL_CW);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        //gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // Enable texture-coords-array (NEW)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer); // Define texture-coords buffer (NEW)

        //gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);

        int i = 2;
        for( i=0;i<2;i++ ){
            mIndexBuffer.position(i*6);
		    gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        }

        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBufferLR);
        for( i=3;i<4;i++ ){
            mIndexBuffer.position(i*6);
            gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        }

        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBufferTB);
        for( i=4;i<6;i++ ){
            mIndexBuffer.position(i*6);
            gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        }

        //gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		//gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // Disable texture-coords-array (NEW)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}

    public void release()
    {
        camera.stopPreview();
        surfaceTexture.setOnFrameAvailableListener(null);
        camera.release();
        Log.d("CameraCube", "camera release");
    }

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		//Log.d("CameraCube", "onFrameAvailable called");
		view.requestRender();
	}
}
