package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	//public String name;
	public String email;
	public String username;
	public String password;
	//public String password_confirmation;
	public String role;
	public String perfil;
	public String fixo;
	public String movel;
	public String morada;
	
	public RegisterData() {
		
	}
	
	public RegisterData(String username, String password, String email, String role, String perfil, String morada, String movel, String fixo) {
		//this.name = name;
		this.email = email;
		this.username = username;
		this.password = password;
		this.role = role;
		this.perfil = perfil;
		this.movel = movel;
		this.fixo = fixo;
		this.morada = morada;
		//this.password_confirmation = password_confirmation;
	}
}
	
	
