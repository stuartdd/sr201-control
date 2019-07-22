/*
 * Copyright (C) 2018 Stuiart Davies (stuartdd)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class SocketIO {

    private Socket socket;

    private InputStream input;

    private OutputStream output;

    public SocketIO() {
        socket = null;
        input = null;
        output = null;
    }

    public void connect(final String ip, final int port) {
        close();
        try {
            socket = new Socket(ip, port);
        } catch (IOException io) {
            throw new ControlException("Socket-Open:Failed:" + ip + ":" + port, null);
        }

        try {
            input = socket.getInputStream();
        } catch (IOException io) {
            throw new ControlException("Socket-getInputStream:Failed:" + ip + ":" + port, null);
        }
        try {
            output = socket.getOutputStream();
        } catch (IOException io) {
            throw new ControlException("Socket-getOutputStream:Failed:" + ip + ":" + port, null);
        }

    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        } else if (socket.isConnected()) {
            return true;
        } else {
            socket = null;
            return false;
        }
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        } catch (IOException io) {
            throw new ControlException("Close failed", null);
        }
    }

    public int read(byte[] bytes) {
        if (!isConnected()) {
            throw new ControlException("Read:Socket-not-connected", null);
        }
        try {
            return input.read(bytes);
        } catch (IOException io) {
            throw new ControlException("Read failed", null);
        }
    }

    public int read(byte[] bytes, int i, int remaining) {
        if (!isConnected()) {
            throw new ControlException("Read:Socket-not-connected", null);
        }
        try {
            return input.read(bytes, i, remaining);
        } catch (IOException io) {
            throw new ControlException("Read:failed", null);
        }
    }

    public void write(byte[] bytes) {
        if (!isConnected()) {
            throw new ControlException("Write:Socket-not-connected", null);
        }
        try {
            output.write(bytes);
        } catch (IOException io) {
            throw new ControlException("Write:failed", null);
        }
    }

    public void flush() {
        if (!isConnected()) {
            throw new ControlException("Flush:Socket-not-connected", null);
        }
        try {
            output.flush();
        } catch (IOException io) {
            throw new ControlException("Flush:failed", null);
        }
    }
}
