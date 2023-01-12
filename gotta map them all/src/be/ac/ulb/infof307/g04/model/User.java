package be.ac.ulb.infof307.g04.model;


/**
 * Contains all the informations concerning a user
 */
public class User {
	private int ID;
	private String username;
	private String pwd;
	private String mail;

	public User(){};

	public User(String username, String pwd, String mail){
		this.username = username;
		this.pwd = pwd;
		this.mail = mail;
	}

	public User(int id, String username, String pwd, String mail) {
		this.ID = id;
		this.username = username;
		this.pwd = pwd;
		this.mail = mail;
	}

	public int getID() {
		return ID;
	}
	public void setID(int ID) {
		this.ID = ID;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}

	
	@Override
	public String toString() {
		return "User [ID=" + ID + ", username=" + username + ", pwd=" + pwd + ", mail=" + mail + "]";
	}
}