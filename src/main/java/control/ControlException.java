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

/**
 *
 * @author stuart
 */
public class ControlException extends RuntimeException {

    private byte[] bytes;

    public ControlException(String message, byte[] bytes) {
        super(message);

    }

    public byte[] getBytes() {
        if (bytes == null) {
            return new byte[8];
        }
        return bytes;
    }

}
