package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.net.SocketTimeoutException;
import static java.lang.String.valueOf;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    String[] REMOTE_PORT = {"11108", "11112", "11116", "11120","11124"};
    static final int SERVER_PORT = 10000;
    int count=0;
    String myPort1;
    private int Failed_Status =-1;
    private int Failed_node =-1;
    int Maximum_Priority=0;
    int initial_Flag=1;
    private double Maximum=0;
    public ArrayList<Kinga> messages = new ArrayList<Kinga>();

    boolean notified = false;
    //Reference: OnPTestClickListener
    //lines: 39-45
    private final Uri providerUri=buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        //Reference PA1
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myPort1=myPort;
        Log.v(TAG, myPort);
        System.out.println("Message myPort  " + myPort1);

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

            new FailTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        //Reference: PA1
        final EditText editText = (EditText) findViewById(R.id.editText1);
        //Reference: https://developer.android.com/reference/android/widget/Button
        //Lines:113-115
        final Button button=(Button)findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg); // This is one way to display a string.
                Log.v("local", String.valueOf(localTextView));
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append("\n");
                Log.v("remote", String.valueOf(remoteTextView));

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);


            }
        });
    }

    private class FailTask extends AsyncTask<ServerSocket, Void, Void> {

        protected Void doInBackground(ServerSocket... sockets) {

            try{
                int Know_FailedNode = 0;
                Thread.sleep(20000);
                while (true){

                    if(Failed_Status == -1 && !notified) {

                        for (int i = 0; i < REMOTE_PORT.length; i++) {
                            try{

                                if(Failed_Status!=-1){

                                    if(Know_FailedNode==Failed_Status)
                                    {
                                        System.out.println("Message from fail  " + Failed_Status);
                                        Know_FailedNode++;
                                        System.out.println("Message from Know_FailedNode  " + Know_FailedNode);
                                        continue;
                                    }

                                }

                                String remotePort = REMOTE_PORT[i];
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                                StringBuilder sb4 = new StringBuilder();
                                sb4.append("Ping");

                                String ss1=sb4.toString();
                                if (!ss1.equals(null))
                                {
                                    writer.println(ss1);
                                    writer.flush();


                                }
                                socket.close();

                            }
                            catch (UnknownHostException e) {
                                Log.e(TAG, "ClientTask UnknownHostException");
                            }

                            catch(IOException e){
                                Log.e(TAG, "ClientTask socket IOException: client : " + Failed_Status);
                                if(Failed_Status==-1){
                                    Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                                    Failed_Status = Know_FailedNode-initial_Flag;

                                }
                            }
                            catch (Exception  e) {
                                if(Failed_Status==-1){
                                    Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                                    Failed_Status = Know_FailedNode-initial_Flag;
                                }
                                System.out.println("failed node"+REMOTE_PORT[i]);

                                Log.e(TAG, "ClientTask socket Exception: client2: " + Failed_Status);

                            }



                        }
                    }

                    if(Failed_Status!=-1 && !notified) {

                        for (int i = 0; i < REMOTE_PORT.length; i++) {
                            try{

                                if(Failed_Status!=-1){

                                    if(Know_FailedNode==Failed_Status)
                                    {
                                        System.out.println("Message from faile here  " + Failed_Status);
                                        Know_FailedNode++;
                                        System.out.println("Message from Know_FailedNode here  " + Know_FailedNode);
                                        continue;
                                    }

                                }

                                String remotePort = REMOTE_PORT[i];
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                                String divider=":";
                                StringBuilder sb4 = new StringBuilder();
                                sb4.append("Failed");
                                sb4.append(divider);
                                sb4.append(Failed_Status);
                                String ss1=sb4.toString();
                                if (!ss1.equals(null))
                                {
                                    writer.println(ss1);
                                    writer.flush();


                                }
                                socket.close();

                            }catch (UnknownHostException e) {
                                Log.e(TAG, "ClientTask UnknownHostException");
                            }

                            catch(IOException e){
                                Log.e(TAG, "ClientTask socket IOException: : " + Failed_Status);
                                if(Failed_Status==-1){
                                    Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                                    Failed_Status = Know_FailedNode-initial_Flag;

                                }

                            }
                            catch (Exception  e) {
                                if(Failed_Status==-1){
                                    Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                                    Failed_Status = Know_FailedNode-initial_Flag;
                                }
                                System.out.println("failed node"+REMOTE_PORT[i]);

                                Log.e(TAG, "ClientTask socket Exception: : " + Failed_Status);

                            }


                        }
                    }

                    Thread.sleep(5000);
                    if(notified){
                        break;
                    }

                }

            }catch (Exception ex){
                Log.e(TAG, "Exception socket IOException");

            }



            return  null;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }




    //Reference:
// 1. https://stackoverflow.com/questions/683041/how-do-i-use-a-priorityqueue
// 2. https://stackoverflow.com/questions/18355932/java-priority-queues-and-comparable-interface

    public class Kinga implements Comparable<Kinga> {
        public String Message_msg;
        public boolean Delivery_status;
        public double Max_priorty;
        public int RemotePort_num;

        public String getMessage_msg(){
            return this.Message_msg;
        }
        public void setDelivery_status(boolean status){
            this.Delivery_status=status;
        }
        public void setMax_priorty(double Max_priorty){
            this.Max_priorty = Max_priorty;
        }
        public boolean getDelivery_status(){
            return Delivery_status;
        }
        public int getRemotePort_num(){
            return this.RemotePort_num;
        }
        public Kinga(String Message_msg,double Max_priorty, boolean Delivery_status,  int RemotePort_num){
            this.Message_msg = Message_msg;
            this.Delivery_status = Delivery_status;
            this.Max_priorty = Max_priorty;
            this.RemotePort_num = RemotePort_num;

        }
        @Override
        public int compareTo(Kinga object2) {
            if(this.Max_priorty < object2.Max_priorty){
                return -1;
            }
            else if(this.Max_priorty > object2.Max_priorty){
                return 1;
            }
            else{
                return 0;
            }

        }



    }
    private static PriorityQueue<Kinga> priorityQueue = new PriorityQueue();
    private static HashMap<String, Kinga> hashMap= new HashMap<String, Kinga>();
//Reference:
// 1. https://stackoverflow.com/questions/32091931/how-can-i-store-stringbuilder-value-in-a-string

    //Reference: PA1
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {


        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];


            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            try {
                while(true){

                    //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                      /* Note: 1. Invoking serverSocket and establishing the connection- accepting connection from client
                              2. PrintWriter class prints formatted representations of objects to a text-output stream.
                              3. BufferedReader class reads text from a character-input stream
                       */
                    Socket socket = serverSocket.accept();
                    PrintWriter writer= new PrintWriter(socket.getOutputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String MessageInput = reader.readLine();
                    System.out.println("Message from client  " + MessageInput);
                    String[] m = MessageInput.split(":");
                    String Input = String.valueOf(m[0]);
                    String message;
                    System.out.println("Input  " + Input);
                    System.out.println("Message from client111  " + MessageInput);


                    if (Input.equalsIgnoreCase("Present")) {
                        String Port = String.valueOf(m[1]);
                        System.out.println("Message from Port  " + Port);
                        message = valueOf(m[2]);
                        System.out.println("Message from message  " + message);
                        int RemotePort_Index = 0;



                        for (int i = 0; i < REMOTE_PORT.length; i++) {

                            boolean index = false;
                            if (REMOTE_PORT[i].equals(Port)) {
                                RemotePort_Index = i;
                                index = true;
                            }
                            if (Failed_Status != -1) {
                                if (i == Failed_Status) {
                                    continue;
                                }
                            }
                            if (index == true) {
                                break;
                            }

                        }


                        System.out.println("Message from here33  ");
                        Kinga Object4 = new Kinga(message,Maximum_Priority,false, RemotePort_Index);
                        StringBuilder sb1 = new StringBuilder();
                        String divider = ":";
                        sb1.append("FindProposed");
                        sb1.append(divider);
                        priorityQueue.add(Object4);
                        double proposal = Maximum_Priority + RemotePort_Index/10.0;
                        sb1.append(proposal);
                        String Proposed = sb1.toString();
                        hashMap.put(message, Object4);
                        writer.println(Proposed);
                        writer.flush();
                        System.out.println("Message from Proposed  " + Proposed);
                        Maximum_Priority++;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (Input.equalsIgnoreCase("FoundProposed")) {
                        Double aggcount = Double.parseDouble(m[1]);
                        message = String.valueOf(m[2]);
                        Kinga proObject = hashMap.get(message);
                        System.out.println("Message from aggcount  " + aggcount);
                        priorityQueue.remove(proObject);
                        int RemotePort_Index = proObject.getRemotePort_num();
                        System.out.println("Message from RemotePort_Index  " + RemotePort_Index);
                        Maximum = aggcount;
                        int numberToCompare = (int)(aggcount + 1);
                        Maximum_Priority = Math.max(numberToCompare, Maximum_Priority);
                        System.out.println("Message from Maxp123  " + Maximum_Priority);
                        proObject.setMax_priorty(Maximum);
                        proObject.setDelivery_status(true);
                        Log.v(TAG, "Broadcasting........" + Maximum + "---" + proObject.getMessage_msg());
                        priorityQueue.add(proObject);

                        if (!MessageInput.equals(null)) {

                            writer = new PrintWriter(socket.getOutputStream());
                            writer.println("ok");

                        }

                    }
                    while (priorityQueue.size() > 0 && priorityQueue.peek().getDelivery_status()) {
                        Kinga FinalObject = priorityQueue.remove();
                        String FinalMessage = FinalObject.getMessage_msg();
                        publishProgress(FinalMessage);
                    }

                    if(Input.equalsIgnoreCase("Failed")){
                        int FailedPort = Integer.parseInt(m[1]);
                        Failed_Status = FailedPort;
                        notified = true;
                        Log.v(TAG," FailedPort : "+REMOTE_PORT[FailedPort]);


                    }

                    if (Failed_Status != -1) {

                        System.out.println("Message from failed  ");
                        System.out.println("Message from client111failed  " + Failed_Status);
                        for (Kinga Object1 : priorityQueue) {
                            if (Object1.getRemotePort_num() == Failed_Status) {
                                System.out.println("Message from getSender  " + Object1.getRemotePort_num());
                                messages.add(Object1);
                            }
                        }

                        for (Kinga Object3 : messages) {
                            System.out.println("Message from remove dead  ");
                            if (priorityQueue.contains(Object3)) {
                                priorityQueue.remove(Object3);
                                System.out.println("Message deleted " + Object3);
                            }
                        }


                    }
                    // }
                    socket.close();

                  /*  String  MessageInput="";

                    while((MessageInput=reader.readLine()) !=null) {


                        //Reference: https://developer.android.com/reference/android/os/AsyncTask.html#publishProgress(Progress...)
                        //Note: To publish updates on the UI thread while the background computation is still running
                        publishProgress(MessageInput);
                        //https://codereview.stackexchange.com/questions/149905/sending-ack-nack-for-packets

                        writer.println("ok");

                        //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                        //Note: Flushes the stream
                        writer.flush();
                        break;

                    }*/

                    //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                    //Closes the socket, flushing it first
                    socket.close();

                }}

            catch (UnknownHostException e) {
                Log.e(TAG, "serverTask UnknownHostException");
            }            catch (IOException e)
            {
                e.printStackTrace();
                Log.e(TAG, "ServerTask socket IOException");
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            Log.v("message", strReceived);

            //Reference: PA2 PartA Document
            ContentValues keyValueToInsert = new ContentValues();
            // inserting <”key-to-insert”, “value-to-insert”>
            keyValueToInsert.put("key", String.valueOf(count));
            Log.v("key", String.valueOf(count));
            keyValueToInsert.put("value", strReceived);
            Log.v("value",strReceived);
            count++;
            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );



            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            Log.v("remote1", String.valueOf(remoteTextView));
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");
            Log.v("local1", String.valueOf(localTextView));

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */



            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            try {
                int Know_FailedNode2=0;
                int Know_FailedNode = 0;
                double priorityCount = -1.0;
                String msgToSend = msgs[0].trim();
                StringBuilder sb = new StringBuilder();
                String divider=":";
                sb.append("Present");
                sb.append(divider);
                sb.append(myPort1);
                sb.append(divider);
                sb.append(msgToSend);
                String msgToSend_1= sb.toString();
                System.out.println("Message Sent  " +msgToSend_1 );

                for (int i = 0; i < REMOTE_PORT.length; i++) {
                    try {
                        if(Failed_Status!=-1){

                            if(Know_FailedNode==Failed_Status)
                            {
                                System.out.println("Message from failes here  " + Failed_Status);
                                Know_FailedNode++;
                                System.out.println("Message from count3 here  " + Know_FailedNode);
                                continue;
                            }

                        }
                        Know_FailedNode++;
                        System.out.println("Message from count3 here 22 " + Know_FailedNode);
                        String remotePort = REMOTE_PORT[i];
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort));

                        if (!msgToSend_1.equals(null) ) {

                            //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                             /*Note: 1. Invoking serverSocket and establishing the connection- accepting connection from client
                              2. PrintWriter class prints formatted representations of objects to a text-output stream.
                              3. BufferedReader class reads text from a character-input stream.
                             */

                            //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                            //Reference: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                            //Note: Flushes the stream

                            PrintWriter writer = new PrintWriter(socket.getOutputStream());
                            writer.println(msgToSend_1);
                            writer.flush();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String Proposedclient= reader.readLine();
                            System.out.println("Message from server: " + Proposedclient);
                            String[] m = Proposedclient.split(":");
                            String Proposedstring=String.valueOf(m[0]);

                            if(Proposedstring.equalsIgnoreCase("FindProposed"))
                            {
                                String ProposedValue=String.valueOf(m[1]);
                                if(priorityCount< Double.parseDouble(ProposedValue))
                                {
                                    priorityCount=Double.parseDouble(ProposedValue);
                                }
                                System.out.println("Message from maxCounter  " + priorityCount);
                                System.out.println("Message from Maxp  " + ProposedValue);
                            }
                            socket.close();

                            //String ACK = "";
                            //Ack the message from server when server says "ok", socket closes
                          /*  while ((ACK = reader.readLine()) != null) {
                                if (ACK.equals("ok")) {

                                    //Reference: https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
                                    //Closes the socket, flushing it first
                                    socket.close();
                                    break;
                                }
                            }*/
                            //  socket.close();
                            /*
                             * TODO: Fill in your client code that sends out a message.
                             */
                        }

                    }

                    catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    }
                    catch(IOException e){
                        Log.e(TAG, "IOException in Client IOException: failedcount2oo111 : " + Failed_Status);
                        if(Failed_Status==-1){
                            Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                            Failed_Status = Know_FailedNode-initial_Flag;

                        }


                    }
                    catch (Exception  e) {
                        if(Failed_Status==-1){
                            Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                            Failed_Status = Know_FailedNode-initial_Flag;
                        }
                        System.out.println("failed node"+REMOTE_PORT[i]);

                        Log.e(TAG, "Exception in Client Exception: : " + Failed_Status);

                    }
                }


                for (int i = 0; i < REMOTE_PORT.length; i++) {
                    try{
                        if(Failed_Status!=-1){
                            if(Know_FailedNode2==Failed_Status){
                                System.out.println("Message from failes here count2 " + Failed_Status);
                                Know_FailedNode2++;
                                System.out.println("Message from count2 here  " + Know_FailedNode2);
                                continue;
                            }}
                        Know_FailedNode2++;
                        System.out.println("Message from count2 here222  " + Know_FailedNode2);
                        String remotePort = REMOTE_PORT[i];
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                        socket.setSoTimeout(2000);
                        PrintWriter writer = new PrintWriter(socket.getOutputStream());
                        System.out.println("Message from hellloooo  ");
                        StringBuilder sb3 = new StringBuilder();
                        System.out.println("Message from here12333  ");
                        divider=":";
                        sb3.append("FoundProposed");
                        sb3.append(divider);
                        sb3.append(priorityCount);
                        sb3.append(divider);
                        sb3.append(msgToSend);
                        String msgToSend2=sb3.toString();
                        System.out.println("Message from proooo  " + sb3.toString());
                        System.out.println("Message from proooo233  " + msgToSend2);

                        if (!msgToSend2.equals(null)) {

                            writer.println(msgToSend2);
                            writer.flush();
                        }

                        socket.close();
                    }
                    catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    }
                    catch (SocketTimeoutException e) {
                        if (Failed_Status == -1) {
                            Failed_Status = Know_FailedNode2 - initial_Flag;
                            Log.e(TAG, "SocketTimeoutException in Client SocketTimeoutException : " + Failed_Status);
                            System.out.println("failed node socket"+REMOTE_PORT[i]);
                        }
                    }
                    catch(IOException e){
                        Log.e(TAG, "IOException in Client IOException: : " + Failed_Status);
                        if(Failed_Status==-1){
                            Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                            Failed_Status = Know_FailedNode2-initial_Flag;


                        }

                    }

                    catch(Exception e){

                        if(Failed_Status==-1){
                            Failed_node=Integer.parseInt(REMOTE_PORT[i]);
                            Failed_Status = Know_FailedNode2-initial_Flag;
                        }

                        Log.e(TAG, "Exception in Client Exception: " + Failed_Status + " " + Know_FailedNode2 + e.getStackTrace());

                    }
                }

                if(Failed_Status!=-1 && !notified) {

                    for (int i = 0; i < REMOTE_PORT.length; i++) {
                        String remotePort = REMOTE_PORT[i];
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream());
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append("Failed");
                        sb4.append(divider);
                        sb4.append(Failed_Status);
                        String ss1=sb4.toString();
                        if (!ss1.equals(null))
                        {
                            writer.println(ss1);
                            writer.flush();


                        }
                        socket.close();

                    }
                }


            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }





}
