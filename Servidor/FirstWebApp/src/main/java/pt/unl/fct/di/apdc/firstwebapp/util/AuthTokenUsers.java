package pt.unl.fct.di.apdc.firstwebapp.util;
import java.util.List;
import java.util.UUID;
import com.google.appengine.api.datastore.Entity;

	public class AuthTokenUsers {
		
	public List<String> users;

	public AuthTokenUsers(List<String> users) {
		this.users = users;
	}
}
