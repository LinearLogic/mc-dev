package net.minecraft.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.SecretKey;

public class NetworkManager implements INetworkManager {

    public static AtomicInteger a = new AtomicInteger();
    public static AtomicInteger b = new AtomicInteger();
    private final Object h = new Object();
    private final IConsoleLogManager i;
    private Socket socket;
    private final SocketAddress k;
    private volatile DataInputStream input;
    private volatile DataOutputStream output;
    private volatile boolean n = true;
    private volatile boolean o = false;
    private List inboundQueue = Collections.synchronizedList(new ArrayList());
    private List highPriorityQueue = Collections.synchronizedList(new ArrayList());
    private List lowPriorityQueue = Collections.synchronizedList(new ArrayList());
    private Connection connection;
    private boolean t = false;
    private Thread u;
    private Thread v;
    private String w = "";
    private Object[] x;
    private int y = 0;
    private int z = 0;
    public static int[] c = new int[256];
    public static int[] d = new int[256];
    public int e = 0;
    boolean f = false;
    boolean g = false;
    private SecretKey A = null;
    private PrivateKey B = null;
    private int lowPriorityQueueDelay = 50;

    public NetworkManager(IConsoleLogManager iconsolelogmanager, Socket socket, String s, Connection connection, PrivateKey privatekey) {
        this.B = privatekey;
        this.socket = socket;
        this.i = iconsolelogmanager;
        this.k = socket.getRemoteSocketAddress();
        this.connection = connection;

        try {
            socket.setSoTimeout(30000);
            socket.setTrafficClass(24);
        } catch (SocketException socketexception) {
            System.err.println(socketexception.getMessage());
        }

        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5120));
        this.v = new NetworkReaderThread(this, s + " read thread");
        this.u = new NetworkWriterThread(this, s + " write thread");
        this.v.start();
        this.u.start();
    }

    public void a(Connection connection) {
        this.connection = connection;
    }

    public void queue(Packet packet) {
        if (!this.t) {
            Object object = this.h;

            synchronized (this.h) {
                this.z += packet.a() + 1;
                this.highPriorityQueue.add(packet);
            }
        }
    }

    private boolean h() {
        boolean flag = false;

        try {
            Packet packet;
            int i;
            int[] aint;

            if (this.e == 0 || !this.highPriorityQueue.isEmpty() && System.currentTimeMillis() - ((Packet) this.highPriorityQueue.get(0)).timestamp >= (long) this.e) {
                packet = this.a(false);
                if (packet != null) {
                    Packet.a(packet, this.output);
                    if (packet instanceof Packet252KeyResponse && !this.g) {
                        if (!this.connection.a()) {
                            this.A = ((Packet252KeyResponse) packet).d();
                        }

                        this.k();
                    }

                    aint = d;
                    i = packet.n();
                    aint[i] += packet.a() + 1;
                    flag = true;
                }
            }

            if (this.lowPriorityQueueDelay-- <= 0 && (this.e == 0 || !this.lowPriorityQueue.isEmpty() && System.currentTimeMillis() - ((Packet) this.lowPriorityQueue.get(0)).timestamp >= (long) this.e)) {
                packet = this.a(true);
                if (packet != null) {
                    Packet.a(packet, this.output);
                    aint = d;
                    i = packet.n();
                    aint[i] += packet.a() + 1;
                    this.lowPriorityQueueDelay = 0;
                    flag = true;
                }
            }

            return flag;
        } catch (Exception exception) {
            if (!this.o) {
                this.a(exception);
            }

            return false;
        }
    }

    private Packet a(boolean flag) {
        Packet packet = null;
        List list = flag ? this.lowPriorityQueue : this.highPriorityQueue;
        Object object = this.h;

        synchronized (this.h) {
            while (!list.isEmpty() && packet == null) {
                packet = (Packet) list.remove(0);
                this.z -= packet.a() + 1;
                if (this.a(packet, flag)) {
                    packet = null;
                }
            }

            return packet;
        }
    }

    private boolean a(Packet packet, boolean flag) {
        if (!packet.e()) {
            return false;
        } else {
            List list = flag ? this.lowPriorityQueue : this.highPriorityQueue;
            Iterator iterator = list.iterator();

            Packet packet1;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                packet1 = (Packet) iterator.next();
            } while (packet1.n() != packet.n());

            return packet.a(packet1);
        }
    }

    public void a() {
        if (this.v != null) {
            this.v.interrupt();
        }

        if (this.u != null) {
            this.u.interrupt();
        }
    }

    private boolean i() {
        boolean flag = false;

        try {
            Packet packet = Packet.a(this.i, this.input, this.connection.a(), this.socket);

            if (packet != null) {
                if (packet instanceof Packet252KeyResponse && !this.f) {
                    if (this.connection.a()) {
                        this.A = ((Packet252KeyResponse) packet).a(this.B);
                    }

                    this.j();
                }

                int[] aint = c;
                int i = packet.n();

                aint[i] += packet.a() + 1;
                if (!this.t) {
                    if (packet.a_() && this.connection.b()) {
                        this.y = 0;
                        packet.handle(this.connection);
                    } else {
                        this.inboundQueue.add(packet);
                    }
                }

                flag = true;
            } else {
                this.a("disconnect.endOfStream", new Object[0]);
            }

            return flag;
        } catch (Exception exception) {
            if (!this.o) {
                this.a(exception);
            }

            return false;
        }
    }

    private void a(Exception exception) {
        exception.printStackTrace();
        this.a("disconnect.genericReason", new Object[] { "Internal exception: " + exception.toString()});
    }

    public void a(String s, Object... aobject) {
        if (this.n) {
            this.o = true;
            this.w = s;
            this.x = aobject;
            this.n = false;
            (new NetworkMasterThread(this)).start();

            try {
                this.input.close();
            } catch (Throwable throwable) {
                ;
            }

            try {
                this.output.close();
            } catch (Throwable throwable1) {
                ;
            }

            try {
                this.socket.close();
            } catch (Throwable throwable2) {
                ;
            }

            this.input = null;
            this.output = null;
            this.socket = null;
        }
    }

    public void b() {
        if (this.z > 2097152) {
            this.a("disconnect.overflow", new Object[0]);
        }

        if (this.inboundQueue.isEmpty()) {
            if (this.y++ == 1200) {
                this.a("disconnect.timeout", new Object[0]);
            }
        } else {
            this.y = 0;
        }

        int i = 1000;

        while (!this.inboundQueue.isEmpty() && i-- >= 0) {
            Packet packet = (Packet) this.inboundQueue.remove(0);

            packet.handle(this.connection);
        }

        this.a();
        if (this.o && this.inboundQueue.isEmpty()) {
            this.connection.a(this.w, this.x);
        }
    }

    public SocketAddress getSocketAddress() {
        return this.k;
    }

    public void d() {
        if (!this.t) {
            this.a();
            this.t = true;
            this.v.interrupt();
            (new NetworkMonitorThread(this)).start();
        }
    }

    private void j() {
        this.f = true;
        InputStream inputstream = this.socket.getInputStream();

        this.input = new DataInputStream(MinecraftEncryption.a(this.A, inputstream));
    }

    private void k() {
        this.output.flush();
        this.g = true;
        BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(MinecraftEncryption.a(this.A, this.socket.getOutputStream()), 5120);

        this.output = new DataOutputStream(bufferedoutputstream);
    }

    public int e() {
        return this.lowPriorityQueue.size();
    }

    public Socket getSocket() {
        return this.socket;
    }

    static boolean a(NetworkManager networkmanager) {
        return networkmanager.n;
    }

    static boolean b(NetworkManager networkmanager) {
        return networkmanager.t;
    }

    static boolean c(NetworkManager networkmanager) {
        return networkmanager.i();
    }

    static boolean d(NetworkManager networkmanager) {
        return networkmanager.h();
    }

    static DataOutputStream e(NetworkManager networkmanager) {
        return networkmanager.output;
    }

    static boolean f(NetworkManager networkmanager) {
        return networkmanager.o;
    }

    static void a(NetworkManager networkmanager, Exception exception) {
        networkmanager.a(exception);
    }

    static Thread g(NetworkManager networkmanager) {
        return networkmanager.v;
    }

    static Thread h(NetworkManager networkmanager) {
        return networkmanager.u;
    }
}
