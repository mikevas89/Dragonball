package Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GameMain {
	
	public static void main(String[] args){
		
		System.out.println("Starting..");
		
		Process pro;
		try {  
			String[] cmd = {"/bin/sh","-c", "java ./bin/Node/Server"};
            Process p = Runtime.getRuntime().exec(cmd);  
            BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                System.out.println(line);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
	}

}
