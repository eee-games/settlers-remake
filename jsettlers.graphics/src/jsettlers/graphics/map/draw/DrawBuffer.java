/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.graphics.map.draw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jsettlers.graphics.map.IGLProvider;

public class DrawBuffer {

	private static final int BUFFERS = 5;
	private final IGLProvider context;
	private float z;

	public class Buffer {
		/**
		 * Bytes we need for one vertex
		 */
		private static final int VERTEX_LENGTH = 5 * 4 + 4;
		private static final int TRIAMGLE_LENGTH = 3 * VERTEX_LENGTH;
		private static final int BUFFER_TRIANGLES = 1000;

		/**
		 * The last texture we set.
		 */
		private int currentTexture = 0;

		private int currentTriangles = 0;

		protected final ByteBuffer byteBuffer;

		protected Buffer() {
			byteBuffer =
					ByteBuffer.allocateDirect(BUFFER_TRIANGLES * TRIAMGLE_LENGTH);
			byteBuffer.order(ByteOrder.nativeOrder());
		}

		protected void setForTexture(int texture) {
			if (currentTexture != texture) {
				if (currentTriangles != 0) {
					draw();
				}
				currentTexture = texture;
			}
		}

		protected void draw() {
			// System.out.println("draw " + currentTriangles + " tris of " + currentTexture);
			byteBuffer.rewind();
			context.getGl().drawTrianglesWithTextureColored(currentTexture, byteBuffer, currentTriangles);
			byteBuffer.rewind();
			currentTriangles = 0;
		}

		protected void addImage(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, int activeColor) {
			if (currentTriangles >= BUFFER_TRIANGLES - 2) {
				draw();
			}
			addPointPrimitive(x1, y1, u1, v1, activeColor);
			addPointPrimitive(x1, y2, u1, v2, activeColor);
			addPointPrimitive(x2, y1, u2, v1, activeColor);
			addPointPrimitive(x2, y1, u2, v1, activeColor);
			addPointPrimitive(x1, y2, u1, v2, activeColor);
			addPointPrimitive(x2, y2, u2, v2, activeColor);
			currentTriangles += 2;
		}

		public void addTriangle(float x1, float y1, float x2, float y2, float x3, float y3,
									float u1, float v1, float u2, float v2, float u3, float v3, int activeColor) {
			if (currentTriangles >= BUFFER_TRIANGLES - 1) {
				draw();
			}
			addPointPrimitive(x1, y1, u1, v1, activeColor);
			addPointPrimitive(x2, y2, u2, v2, activeColor);
			addPointPrimitive(x3, y3, u3, v3, activeColor);
			currentTriangles += 1;
		}

		private void addPointPrimitive(float x1, float y1, float u, float v, int activeColor) {
			byteBuffer.putFloat(x1);
			byteBuffer.putFloat(y1);
			byteBuffer.putFloat(getZ());
			byteBuffer.putFloat(u);
			byteBuffer.putFloat(v);
			byteBuffer.putInt(activeColor);
		}
	}

	private int lastFreedBuffer = 0;
	private final Buffer[] drawBuffers;

	public DrawBuffer(IGLProvider context) {
		this.context = context;
		drawBuffers = new Buffer[BUFFERS];
		for (int i = 0; i < BUFFERS; i++) {
			drawBuffers[i] = new Buffer();
		}
	}

	public void addImage(int texture, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, int activeColor) {
		setZ(getZ() + .00001f);
		getBuffer(texture).addImage(x1, y1, x2, y2, u1, v1, u2, v2, activeColor);
	}

	public Buffer getBuffer(int texture) {
		for (int i = 0; i < BUFFERS; i++) {
			if (drawBuffers[i].currentTexture == texture) {
				return drawBuffers[i];
			}
		}

		lastFreedBuffer++;
		if (lastFreedBuffer >= BUFFERS) {
			lastFreedBuffer = 0;
		}

		Buffer buffer = drawBuffers[lastFreedBuffer];
		buffer.setForTexture(texture);
		return buffer;
	}

	public void flush() {
		for (int i = 0; i < BUFFERS; i++) {
			drawBuffers[i].draw();
		}
		setZ(0);
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}
}
