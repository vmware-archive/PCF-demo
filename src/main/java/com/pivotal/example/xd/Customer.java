package com.pivotal.example.xd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Customer implements Serializable { 
	private String fName;
	private String lName;
	private String gender;
	private int id;
	
	public Customer(String fName, String lName, String gender, int id) {
		this.setfName(fName);
		this.setlName(lName);
		this.setGender(gender);
		this.setId(id);
	}
	/**
	 * @return the fName
	 */
	public String getfName() {
		return fName;
	}
	/**
	 * @return the lName
	 */
	public String getlName() {
		return lName;
	}
	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	/**
	 * @param lName the lName to set
	 */
	public void setlName(String lName) {
		this.lName = lName;
	}
	/**
	 * @param fName the fName to set
	 */
	public void setfName(String fName) {
		this.fName = fName;
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

	    public static Customer fromBytes(byte[] body) {
	        Customer obj = null;
	        try {
	            ByteArrayInputStream bis = new ByteArrayInputStream(body);
	            ObjectInputStream ois = new ObjectInputStream(bis);
	            obj = (Customer) ois.readObject();
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
	    @Override
	    public String toString() {
	    	return this.id + "," + this.fName + "," + this.lName + "," + this.gender + "\n";
	    }
}
