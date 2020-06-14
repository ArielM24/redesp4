import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class Cliente extends Thread{
 	public static final String MCAST_ADDR  = "230.1.1.1"; //dir clase D valida, grupo al que nos vamos a unir
 	public static final int MCAST_PORT = 4000;//puerto multicast
 	public static final int DGRAM_BUF_LEN= 1024; //tamaño del buffer
 	public static String name, msg;
 	public boolean init = true;
 	Scanner s = new Scanner(System.in);

	public void run(){
   		Thread tleer = new Thread(){
   			public void run(){
   				leer();
   			}
   		};
   		tleer.start();

   		Thread tescribir = new Thread(){
   			public void run(){
   				escribir();
   			}
   		};
   		tescribir.start();
	}

	public void leer(){
        InetAddress group = null;
        try{
            group = InetAddress.getByName(MCAST_ADDR); //se trata de resolver dir multicast     
        }catch(UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.println("Servidor iniciado");
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
            if(mt.group(1).equals("<privado>")){
                //System.out.println("privado de "+mt.group(2)+" para "+mt.group(3)+": "+mt.group(4));
                String auxname = mt.group(3).substring(1,mt.group(3).length()-1);
                //System.out.println(auxname);
                if(auxname.equals(name)){
                	System.out.println(mt.group(2)+": "+converEmoticon(mt.group(4)));
                }
            }
        }catch(Exception ex){
            //System.out.println("Mensaje invalido");
        }
    }

    public void escribir(){
    	InetAddress group = null;
        try{
            group = InetAddress.getByName(MCAST_ADDR); //se trata de resolver dir multicast     
        }catch(UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.println("Servidor iniciado");
        for(;;){
            try{
                MulticastSocket socket = new MulticastSocket(MCAST_PORT);
                socket.joinGroup(group); 

                byte[] buf = new byte[DGRAM_BUF_LEN];//crea arreglo de bytes 
    			if(init){
                    msg = "<inicio>"+name;
                    System.out.println(msg);
    				init = false;
    			}else{
    				msg = s.nextLine();
    			}
    			//System.out.println(msg);
    			DatagramPacket packet = new DatagramPacket(msg.getBytes(),msg.length(),group,MCAST_PORT);
    			//System.out.println("Enviando: " + msg+"  con un TTL= "+socket.getTimeToLive());
    		    socket.send(packet);
            }catch(IOException e){
                e.printStackTrace();
                System.exit(2);
            }
        }
    }

    public static String converEmoticon(String msg){
    	return msg.replace(" :) "," \u263A ").replace(" :( ", " \u2639 ").replace(" <3 "," \u2764 ");
    }
 	    
    public static void main(String[] args) throws Exception{
		try{
			name = args[0];
		    Cliente mc2 = new Cliente();
		    mc2.start();
		  
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}