import java.net.*;

class UDPReceiver {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(4321);
            byte[] buf = new byte[256];
            for(int i=0; i<10; i++) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println("receive DatagramPacket "
                        + (new String(packet.getData())).trim() + " "
                        + packet.getAddress() + ":"
                        + packet.getPort());
            }
        } catch(Exception e){System.out.println("error "+e);}
    }
}