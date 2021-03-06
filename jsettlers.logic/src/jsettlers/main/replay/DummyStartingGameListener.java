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
package jsettlers.main.replay;

import jsettlers.graphics.map.IMapInterfaceConnector;
import jsettlers.graphics.progress.EProgressState;
import jsettlers.graphics.startscreen.interfaces.EGameError;
import jsettlers.graphics.startscreen.interfaces.IStartedGame;
import jsettlers.graphics.startscreen.interfaces.IStartingGameListener;

class DummyStartingGameListener implements IStartingGameListener {
	private final Object waitMutex = new Object();
	private IStartedGame startedGame = null;

	@Override
	public void startProgressChanged(EProgressState state, float progress) {
	}

	@Override
	public IMapInterfaceConnector startFinished(IStartedGame game) {
		startedGame = game;
		synchronized (waitMutex) {
			waitMutex.notifyAll();
		}
		return new DummyMapInterfaceConnector();
	}

	@Override
	public void startFailed(EGameError errorType, Exception exception) {
		System.err.println("start failed due to: " + errorType);
		exception.printStackTrace();
		System.exit(1);
	}

	public IStartedGame waitForGameStartup() {
		while (startedGame == null) {
			synchronized (waitMutex) {
				try {
					waitMutex.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return startedGame;
	}
}
