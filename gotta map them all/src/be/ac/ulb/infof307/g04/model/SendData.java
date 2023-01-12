package be.ac.ulb.infof307.g04.model;

public class SendData <T> {
	String token;
	String user;
	T data;
	
	public SendData(){
		
	}
	public SendData(String token, String user,T data) {
		super();
		this.token = token;
		this.user = user;
		this.data = data;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	
}
