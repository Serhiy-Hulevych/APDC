package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

import com.google.api.client.http.HttpHeaders;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.gson.Gson;

	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

public class LoginResource {

	/**
	 * A Logger Object
	 */
		
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();
	
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public LoginResource() { }
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		if(data.username.equals("serhiy") && data.password.equals("teste")) {
		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}
	
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if(!username.equals("serhiy")) {
		return Response.ok().entity(g.toJson(false)).build();
		} else {
		return Response.ok().entity(g.toJson(true)).build();
		}
	}
	
	
	//Recebe input em JSON com propriedades username e password
	//Verifica se a passe e correcta e devolve token
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLoginV1(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		LOG.fine("attempt to login user: " + data.username);
		
		Key userKey = KeyFactory.createKey("User",  data.username);
		try {
			Entity user = datastore.get(userKey);
			String hashedPWD = (String) user.getProperty("user_pwd");
			if(hashedPWD.equals(data.password)) {
				AuthToken token = new AuthToken(data.username);
				LOG.info("User '"+data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();
			} else {
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (EntityNotFoundException e) {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}
	}
	
	
	
	//Recebe input em JSON com propriedades username e password
	//Verifica se a passe e correcta e devolve token
	//Cria um registo com log da conexao com local da conexao e IP e atualiza contador com numero de logins com sucesso e falhados
	/*@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLoginV2(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.info("Attempt to login user: "+ data.username);
		
		Transaction txn = datastore.beginTransaction();
		Key userKey = KeyFactory.createKey("User", data.username);
		try {
			Entity user = datastore.get(userKey);
			
			//Obtains the user login statistics
			Query ctrQuery = new Query("UserStats").setAncestor(userKey);
			List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withDefaults());
			Entity ustats = null;
			if(results.isEmpty()) {
				ustats = new Entity("UserStats", user.getKey());
				ustats.setProperty("user_stats_logins",  0L);
				ustats.setProperty("user_stats_failed", 0L);
			} else {
				ustats = results.get(0);
			}
			
			String hashedPWD = (String) user.getProperty("user_pwd");
			if(hashedPWD.equals(data.password)) {
				//Password correct
				
				//Construct the logs
				Entity log = new Entity("UserLog", user.getKey());
				log.setProperty("user_login_ip", request.getRemoteAddr());
				log.setProperty("user_login_host", request.getRemoteHost());
				log.setProperty("user_login_latlon", headers.getHeaderStringValues("X-AppEngine-CityLatLong"));
				log.setProperty("user_login_city", headers.getHeaderStringValues("X-AppEngine-City"));
				log.setProperty("user_login_country", headers.getHeaderStringValues("X-AppEngine-Country"));
				log.setProperty("user_login_time", new Date());
				//Get the user statistics and updates it
				ustats.setProperty("user_stats_logins", 1L + (long) ustats.getProperty("user_stats_logins"));
				ustats.setProperty("user_stats_failed", 0L);
				ustats.setProperty("user_stats_last", new Date());
				
				//Bath operation
				List<Entity> logs = Arrays.asList(log,ustats);
				datastore.put(txn,logs);
				txn.commit();
				
				//Return token
				AuthToken token = new AuthToken(data.username);
				LOG.info("User '" + data.username + "' logged in sucessfully");
				return Response.ok(g.toJson(token)).build();
			} else {
				// Incorrect password
				ustats.setProperty("user_stats_failed",  1L + (long) ustats.getProperty("user_stats_failed"));
				datastore.put(txn,ustats);
				txn.commit();
				
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (EntityNotFoundException e) {
			// Username does not exist
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}*/
	
	
	@SuppressWarnings("deprecation")
	@POST
	@Path("/v3")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLoginV3(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		LOG.fine("attempt to login user: " + data.username);
		
		Query ctrQuery = new Query("User").addFilter("user_email", FilterOperator.EQUAL, data.username);
		
	    List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(1));
	    
	    Key userKey;
	    
	    if(all.size() > 0)
	    	userKey = all.get(0).getKey();
	    else
	    	userKey = KeyFactory.createKey("User",  data.username);
	    
		try {
			Entity user = datastore.get(userKey);
			String hashedPWD = (String) user.getProperty("user_pwd");
			if(hashedPWD.equals(data.password)) {
				user.setUnindexedProperty("user_fails", 0L);
				datastore.put(user);
				AuthToken token = new AuthToken(data.username);
				LOG.info("User '"+data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();
			} else {
				long temp = (long) (user.getProperty("user_fails"))+1L;
				user.setUnindexedProperty("user_fails", temp);
				datastore.put(user);
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (EntityNotFoundException e) {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}
		
		/*Key userKey = KeyFactory.createKey("User",  data.username);
		System.out.println(userKey.getId());
		try {
			Entity user = datastore.get(userKey);
			String hashedPWD = (String) user.getProperty("user_pwd");
			if(hashedPWD.equals(data.password)) {
				AuthToken token = new AuthToken(data.username);
				LOG.info("User '"+data.username + "' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();
			} else {
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (EntityNotFoundException e) {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}*/
	}
	
	
	//Recebe input em JSON com o useranem e a password
	//Obtem as horas dos logins das ultimas 24 horas
	/*@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getInfo(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		Key userKey = KeyFactory.createKey("User", data.username);
		try {
			Entity user = datastore.get(userKey);
			String hashedPWD = (String) user.getProperty("user_pwd");
			if(hashedPWD.equals(data.password)) {
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				Date yesterday = cal.getTime();
				
				//Obtain the user login statistics
				Filter propertyFilter = new FilterPredicate("user_login_time", FilterOperator.GREATER_THAN_OR_EQUAL, yesterday);
				Query ctrQuery = new Query("UserLog").setAncestor(KeyFactory.createKey("User", data.username))
								.setFilter((com.google.appengine.api.datastore.Query.Filter) propertyFilter)
								.addSort("user_login_time", SortDirection.DESCENDING);
				ctrQuery.addProjection(new PropertyProjection("user_login_time", Date.class));
				List<Entity> results = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(3));
				
				List<Date> loginDates = new ArrayList();
				for(Entity userlog:results) {
					loginDates.add((Date) userlog.getProperty("user_login_time"));
				}
				return Response.ok(g.toJson(loginDates)).build();
			} else {
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (EntityNotFoundException e) {
			// TODO
		}
	}*/
		
}
