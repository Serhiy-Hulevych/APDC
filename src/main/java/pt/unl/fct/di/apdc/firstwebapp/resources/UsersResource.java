package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.LinkedList;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthTokenUsers;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthTokenV2;
import pt.unl.fct.di.apdc.firstwebapp.util.FailedToken;

@Path("/users")

public class UsersResource {
	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public UsersResource() {
		
	}
	
	
	//funciona... devolve token com todos os users
	@GET
	@Path("/v1")   
	@Produces(MediaType.APPLICATION_JSON+";charset=utf-8")
	public Response getUsersV1() {
		
       Query ctrQuery = new Query("User");
 
       List<String> users = new LinkedList<String>();
       List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(10));
       
       for(int i=0; i<all.size(); i++) {
    	   users.add(all.get(i).getKey().getName());
       }
       
       AuthTokenUsers token = new AuthTokenUsers(users);
       return Response.ok(g.toJson(token)).build();
       
	}
	
	
	//funciona
	@SuppressWarnings("deprecation")
	@POST //@GET
	@Path("v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getStats(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		
		Query ctrQuery = new Query("User").addFilter("user_email", FilterOperator.EQUAL, data.username);
		
	    List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(1));
	    
	    Key userKey;
	    
	    if(all.size() > 0)
	    	userKey = all.get(0).getKey();
	    else
	    	userKey = KeyFactory.createKey("User",  data.username);
		
		try {
			Entity user = datastore.get(userKey);
			AuthTokenV2 token = new AuthTokenV2(data.username, (String) user.getProperty("user_morada"), (String) user.getProperty("user_role"));
			return Response.ok(g.toJson(token)).build();
		} catch (EntityNotFoundException e) {
			LOG.warning("Didn't found username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}

	}
	

	@SuppressWarnings("deprecation")
	@POST
	@Path("v3")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response getFailedLogs(pt.unl.fct.di.apdc.firstwebapp.util.LoginData data) {
		
		Query ctrQuery = new Query("User").addFilter("user_email", FilterOperator.EQUAL, data.username);
		
	    List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(1));
	    
	    Key userKey;
	    
	    if(all.size() > 0)
	    	userKey = all.get(0).getKey();
	    else
	    	userKey = KeyFactory.createKey("User",  data.username);
	    
	    try {
			Entity user = datastore.get(userKey);
			FailedToken token = new FailedToken((long) user.getProperty("user_fails"));
			return Response.ok(g.toJson(token)).build();
		} catch (EntityNotFoundException e) {
			LOG.warning("Didn't found username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}

	}
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	@POST
	@Path("v4")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response saveSettings(pt.unl.fct.di.apdc.firstwebapp.util.RegisterData data) {
		
		Query ctrQuery = new Query("User").addFilter("user_email", FilterOperator.EQUAL, data.email);
		
	    List<Entity> all = datastore.prepare(ctrQuery).asList(FetchOptions.Builder.withLimit(1));
	    
	    Key userKey;
	    
	    if(all.size() > 0)
	    	return Response.status(Status.NOT_ACCEPTABLE).build();
	    else
	    	
	    	userKey = KeyFactory.createKey("User",  data.username);
	    
		    try {
				Entity user = datastore.get(userKey);
				user.setProperty("user_email",  data.email);
				user.setProperty("user_perfil", data.perfil);
				user.setProperty("user_movel", data.movel);
				user.setProperty("user_fixo", data.fixo);
				user.setProperty("user_morada", data.morada);
				datastore.put(user);
				return Response.status(Status.OK).build();
			} catch (EntityNotFoundException e) {
				LOG.warning("Didn't found username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}

	}

}
