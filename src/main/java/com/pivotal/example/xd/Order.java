package com.pivotal.example.xd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Order implements Serializable {

	private String state;
	private int amount;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	   public byte[] toBytes() {
	        byte[]bytes;
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try{
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            oos.writeObject(this);
	            oos.flush();
	            oos.reset();
	            bytes = baos.toByteArray();
	            oos.close();
	            baos.close();
	        } catch(IOException e){
	            throw new RuntimeException(e);
	        }
	        return bytes;
	    }

	    public static Order fromBytes(byte[] body) {
	        Order obj = null;
	        try {
	            ByteArrayInputStream bis = new ByteArrayInputStream(body);
	            ObjectInputStream ois = new ObjectInputStream(bis);
	            obj = (Order) ois.readObject();
	            ois.close();
	            bis.close();
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }
	        catch (ClassNotFoundException ex) {
	            ex.printStackTrace();
	        }
	        return obj;
	    }	
	
}
