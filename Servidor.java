import java.net.*;
import java.io.*; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 
public class Servidor extends Thread{
    public static final String MCAST_ADDR = "230.1.1.1";//dir clase D valida, grupo al que nos vamos a unir
    public static final int MCAST_PORT = 4000;
    public static final int DGRAM_BUF_LEN = 1024;
    private String msg;

	public void run(){
    	leer();
	}

    public void leer(){
        InetAddress group = null;
        try{
            group = InetAddress.getByName(MCAST_ADDR); //se trata de resolver dir multicast     
        }catch(UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Servidor iniciado");
        for(;;){
            try{
                MulticastSocket socket = new MulticastSocket(MCAST_PORT);
                socket.joinGroup(group); 

                byte[] buf = new byte[DGRAM_BUF_LEN];
                DatagramPacket recv = new DatagramPacket(buf,buf.length);
                socket.receive(recv);

                byte [] data = recv.getData();
                msg = new String(data);
                //System.out.println("Datos recibidos: " + msg); 
                handleMsg();
                socket.close();         
            }catch(IOException e){
                e.printStackTrace();
                System.exit(2);
            }
        }
    }

    public void handleMsg(){
        Pattern pt = Pattern.compile("(<[^>]+>)(<[^>]+>)?(<[^>]+>)?(.+)"); 
        Matcher mt = pt.matcher(msg);
        mt.find();
        try{
            if(mt.group(1).equals("<inicio>")){
                System.out.println("Usuario conectado: "+mt.group(4));
            }else if(mt.group(1).equals("<msj>")){
                System.out.println(mt.group(2)+": "+converEmoticon(mt.group(4)));
            }else if(mt.group(1).equals("<privado>")){
                //System.out.println("privado de "+mt.group(2)+" para "+mt.group(3)+": "+mt.group(4));
            }else{
                System.out.println("Mensaje invalido");
            }
        }catch(Exception ex){
            System.out.println("Mensaje invalido");
        }
    }

    public static String converEmoticon(String msg){
        return msg.replace(" :) "," \u263A ").replace(" :( ", " \u2639 ").replace(" <3 "," \u2764 ");
    }
    	
    public static void main(String[] args) {
    	try{
    	    Servidor mc2 = new Servidor();
    	    mc2.start();
    	}catch(Exception e){
            e.printStackTrace();
        }
    }
}