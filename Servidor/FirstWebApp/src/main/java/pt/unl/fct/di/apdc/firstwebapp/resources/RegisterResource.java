package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/register")

public class RegisterResource {

	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public RegisterResource() {
	
	}
	
	
	//Recebe input em JSON com propriedades username e password
	//Armazena timestamp de registo
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+";charset=utf-8")
	public Response doRegistrationV1(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		
		Entity user = new Entity("User", data.username);//new Entity("User", data.username);
		user.setProperty("user_pwd", data.password); //digest utils.sha ou nao
		user.setUnindexedProperty("user_creation_time",  new Date());
		datastore.put(user);
		LOG.info("User registered "+data.username);
		return Response.ok().build();
	}	
	
	
	
	//Recebe input em JSON com propriedades username, password e confirmation, email e name
	//Verifica se todos os valores fazem sentido e estao preenchidos
	//Verifica que utilizador nao existe
	/*@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doRegistrationV2(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {
		Transaction txn = datastore.beginTransaction();
		try {
			Key userKey = KeyFactory.createKey("User", data.username);
			Entity user = datastore.get(userKey);
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
		} catch (EntityNotFoundException e) {
			Entity user = new Entity("User", data.username);
			user.setProperty("user_name",  data.name);
			user.setProperty("user_pwd", data.password);
			user.setProperty("user_email",  data.email);
			user.setUnindexedProperty("user_creation_time",  new Date());
			datastore.put(txn,user);
			LOG.info("User registered "+data.username);
			txn.commit();
			return Response.ok().build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}*/
	
	
	
		@SuppressWarnings("deprecation")
		@POST
		@Path("/v3")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response doRegistrationV3(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {
			Transaction txn = datastore.beginTransaction();
			try {
				Query ctrQuery = new Query("User").addFilter("user_email", FilterOperator.EQUAL, data.email);
				
			    List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(1));
		    
			    if(all.size() > 0) {
			    	System.out.println("Email ja existe\n");
			    	txn.rollback();
			    	return Response.status(Status.NOT_ACCEPTABLE).entity("Email already exists.").build();
			    }
			    
			    else {
					Key userKey = KeyFactory.createKey("User", data.username);
					Entity user = datastore.get(userKey);
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			    }
			} catch (EntityNotFoundException e) {
				
					Entity user = new Entity("User", data.username);
					//user.setProperty("user_name",  data.name);
					user.setProperty("user_pwd", data.password);
					user.setProperty("user_email",  data.email);
					user.setProperty("user_perfil", data.perfil);
					user.setProperty("user_movel", data.movel);
					user.setProperty("user_fixo", data.fixo);
					user.setProperty("user_role", data.role);
					user.setProperty("user_morada", data.morada);
					user.setUnindexedProperty("user_fails", 0L);
					user.setUnindexedProperty("user_creation_time",  new Date());
					datastore.put(txn,user);
					LOG.info("User registered "+data.username);
					txn.commit();
					return Response.ok().build();
			    
			} finally {
				if(txn.isActive()) {
					txn.rollback();
				}
			}
		}
}
