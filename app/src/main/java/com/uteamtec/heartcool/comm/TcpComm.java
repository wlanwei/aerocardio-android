package com.uteamtec.heartcool.comm;

import com.uteamtec.heartcool.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by liulingfeng on 2015/10/24.
 */
public class TcpComm {
    public final static int STATE_DISCONNECTED = 1;
    public final static int STATE_DISCONNECTING = 2;
    public final static int STATE_CONNECTING = 3;
    public final static int STATE_CONNECTED = 4; //连接状态

    private String addr;
    private int port;

    private Socket skt = null;
    private InputStream input;
    private OutputStream output;
    private int state;

    private ReadThread readThread;

    private ConnectionChangedListener connListener = null;
    private DataListener dataListener = null;

    public TcpComm(String addr, int port) {
        this.addr = addr;
        this.port = port;
        this.state = STATE_DISCONNECTED;
    }

    public void connect() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (addr == null || port < 1024) {
                    //无效地址，反馈至connListener
                    state = STATE_DISCONNECTED;
                    if (connListener != null) {
                        connListener.onOnConnectionChanged(state);
                    }
                } else {
                    /*
                     * removing the state check, re-connect anyway
                     */
//                    if (state == STATE_CONNECTING || state == STATE_CONNECTED) {
//                        L.i("<APP> connecting or connected, skip connect()");
//                        return;
//                    }

                    if (skt != null) {
                        try {
                            skt.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    skt = new Socket();
                    try {
                        skt.setKeepAlive(true);
                    } catch (SocketException e) {
                        L.i("<APP> socket initialization failed");
                        onDisconnected();
                    }

                    try {
                        skt.connect(new InetSocketAddress(addr, port), 2000); //set timeout of 1 s

                        state = STATE_CONNECTING;
                        if (connListener != null) {
                            connListener.onOnConnectionChanged(state);
                        }
                    } catch (IOException e) {
                        L.i("<APP> connect timout");
                        onDisconnected();
                        return;
                    }

                    //if connected
                    try {
                        input = skt.getInputStream();
                        output = skt.getOutputStream();
                    } catch (IOException e) {
                        onDisconnected();
                        return;
                    }

                    L.i("<APP> server connected");
                    state = STATE_CONNECTED;
                    if (connListener != null) {
                        connListener.onOnConnectionChanged(state);
                    }

                    startRead();
                }
            }
        }).start();
    }


    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopRead();
                state = STATE_DISCONNECTED;

                if (connListener != null) {
                    connListener.onOnConnectionChanged(STATE_DISCONNECTED);
                }

                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                    if (skt != null) {
                        skt.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean send(byte data[]) {
        if (state == STATE_CONNECTED && output != null) {
            try {
                output.write(data);
                output.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                onDisconnected();
            }
        }
        return false;
    }

    public void onDisconnected() {
        state = STATE_DISCONNECTED;
        if (connListener != null) {
            connListener.onOnConnectionChanged(state);
        }

        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (skt != null) {
            try {
                skt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startRead() {
        if (readThread != null) {
            readThread.quit();
        }
        readThread = new ReadThread();
        readThread.start();
    }

    public void stopRead() {
        if (readThread != null) {
            readThread.quit();
        }
    }

    public void setOnConnectionChangedListener(ConnectionChangedListener listener) {
        this.connListener = listener;
    }

    public void setOnDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    public interface ConnectionChangedListener {
        void onOnConnectionChanged(int state);
    }

    public interface DataListener {
        void onReceivedData(byte data[], int len);
    }

    /*读写线程，在连接之后自动启动*/
    private class ReadThread extends Thread {
        byte buff[];
        int len;

        public ReadThread() {
            enabled = true;
        }

        private boolean enabled;

        synchronized public void quit() {
            enabled = false;
            this.interrupt();
        }

        @Override
        public void run() {
            while (true) {
                if (!enabled) {
                    break;
                }

                if (state == STATE_CONNECTED) {
                    try {
                        buff = new byte[1024];
                        len = input.read(buff);

                        if (len > 0 && dataListener != null) {
                            byte outBuff[] = new byte[len];
                            System.arraycopy(buff, 0, outBuff, 0, len);
                            dataListener.onReceivedData(buff, len);
                        }
                    } catch (IOException e) {
                        onDisconnected();
                        break;
                    }
                }
            }
        }
    }
}
