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

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author stuart
 */
public class Control {

    private static enum ID {
        STATS("00"),
        ON__1("11{tim}"), OFF_1("21{tim}"),
        ON__2("12{tim}"), OFF_2("22{tim}"),
        ON__A("11{tim}|12{tim}"), OFF_A("21{tim}|22{tim}");
        private final String cmd;
        ID(String cmd) {
            this.cmd = cmd;
        }
        public String getCmd() {
            return cmd;
        }
    };

    private static final String IPv4_REGEX
            = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
            + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

    private static String ipAddress = "192.168.1.100";
    private static int port = 6722;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            if (args.length == 0) {
                System.out.println(command(ID.STATS, ""));
            }
            String state = "";
            String ch = "";
            String tim = "";
            for (String arg : args) {
                String cmd = arg.toUpperCase();
                if (cmd.equals("OFF")) {
                    state = "OFF_";
                }
                if (cmd.equals("ON")) {
                    state = "ON__";
                }
                if (cmd.startsWith("T")) {
                    tim = cmd.substring(1);
                }
                if (cmd.equals("ALL")) {
                    ch = "A";
                }
                if (cmd.equals("1")) {
                    ch = "1";
                }
                if (cmd.equals("2")) {
                    ch = "2";
                }
                if (cmd.startsWith("-I")) {
                    ipAddress = cmd.substring(2);
                    Matcher matcher = IPv4_PATTERN.matcher(ipAddress);
                    if (!matcher.matches()) {
                        throw new ControlException("Invalid:ip:" + ipAddress, null);
                    }
                }
                if (cmd.startsWith("-P")) {
                    try {
                        port = Integer.parseInt(cmd.substring(2));
                    } catch (NumberFormatException ex) {
                        throw new ControlException("Invalid:port:" + cmd.substring(2), null);
                    }
                }
            }
            String result = tryAction(state, ch, tim);
            if (result != null) {
                System.out.println(result);
            } else {
                System.out.println(response("No-Command-Run", new byte[8]));
            }
        } catch (ControlException ex) {
            System.out.println(response(ex.getMessage(), ex.getBytes()));
        } catch (Exception ex) {
            System.out.println(response(ex.getMessage(), new byte[8]));
        }
    }

    private static String tryAction(String action, String ch, String tim) {
        if (action.length() == 0) {
            action = "STATS";
            ch = "";
            tim = "";
        }
        ID id = null;
        try {
            id = ID.valueOf((action + ch).substring(0, 5));
        } catch (Exception ex) {
            throw new ControlException("Undefined:Action:" + action + ch, null);
        }
        if ((action + ch).length() != 5) {
            throw new ControlException("Invalid:Action:" + action + ch, null);
        }
        if (tim.length() > 0) {
            try {
                Long.parseLong(tim);
            } catch (NumberFormatException ex) {
                throw new ControlException("Invalid:Time:" + tim, null);
            }
        }
        switch (id) {
            case STATS:
                return command(id, "");
            case OFF_A:
                command(ID.OFF_1, tim);
                id = ID.OFF_2;
                break;
            case ON__A:
                command(ID.ON__1, tim);
                id = ID.ON__2;
                break;
        }
        if (ch.length() == 0) {
            throw new ControlException("Undefined:Switch:" + action + "?", null);
        }
        return command(id, tim);
    }

    private static String command(ID action, String tim) {
        byte[] buffer = new byte[1000];
        String resp = "OK";
        SocketIO socketIO = new SocketIO();
        String cmd = action.getCmd();
        if (tim.length() > 0) {
            cmd = cmd.replaceAll("\\{tim\\}", ":"+tim);
        } else {
            cmd = cmd.replaceAll("\\{tim\\}", "");
        }
        System.err.println(">>> "+cmd);
        try {
            socketIO.connect(ipAddress, port);
            if (socketIO.isConnected()) {
                socketIO.write((action.getCmd() + tim).getBytes(Charset.forName("UTF-8")));
                socketIO.flush();
                int count = socketIO.read(buffer);
                if (count < 2) {
                    throw new ControlException("Read-Length:" + count, buffer);
                }
            } else {
                return response("Not-Connected", buffer);
            }
            return response(resp, buffer);
        } finally {
            socketIO.close();
        }
    }

    private static String response(String err, byte[] buffer) {
        return String.format("{\"status\":\"%s\", \"ch1\":\"%s\", \"ch2\":\"%s\", \"raw\":\"%s\"}", err, buffer[0] == 49 ? "ON" : "OFF", buffer[1] == 49 ? "ON" : "OFF", buffer[0] + "," + buffer[1] + "," + buffer[2] );
    }

}
