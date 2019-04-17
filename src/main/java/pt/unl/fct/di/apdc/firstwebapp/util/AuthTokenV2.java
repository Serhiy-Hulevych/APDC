package pt.unl.fct.di.apdc.firstwebapp.util;
import java.util.UUID;

	public class AuthTokenV2 {
		
	public static final long EXPIRATION_TIME = 1000*60*5; //2h
	public String username;
	public String tokenID;
	public long creationData;
	public long expirationData;
	public String address;
	public String role;

	public AuthTokenV2(String username, String address, String role) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
		this.role = role;
		this.address = address;
	}
}
