package com.hesine.mock.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Deploy {

	public static void main(String[] args) {		
		compile();    
		deploy();
	}
	
	private static void compile(){
		 try { 
			 Process a = Runtime.getRuntime().exec("cmd /c E:/download/apache-maven-3.3.3/bin/mvn package");
	         BufferedReader br1 = new BufferedReader(new InputStreamReader( a.getInputStream(),"gbk"));  
	         String s1 = "";  
	         while((s1=br1.readLine())!= null){  
	             System.out.println(s1);  
	         }
		 }catch (Exception e) {
            e.printStackTrace();
         }		 
	}
	
	private static void deploy(){
		 try {                        	                      
            Process a = Runtime.getRuntime().exec("fab -f E:/hichat/hichat/hichat-access/src/scripts/fabfile.py deploy.get_hosts deploy.deploy");
            BufferedReader br1 = new BufferedReader(new InputStreamReader( a.getInputStream(),"gbk"));  
            String s1 = "";  
            while((s1=br1.readLine())!= null){  
                System.out.println(s1);  
            }              
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
