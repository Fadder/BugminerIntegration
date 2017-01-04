package executor.monitoring;

import java.io.*;


public class IORedirecter implements Runnable{
    InputStreamReader input;
    OutputStreamWriter output;

    public IORedirecter(InputStream in,OutputStream out){
        input=new InputStreamReader(in);
        output=new OutputStreamWriter(out);
    }

    public void run(){
        char[] buffer= new char[255];
        int charcount;

        try {
            while((charcount = input.read(buffer,0,255)) >= 0){
                output.write(buffer,0,charcount);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
