package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/delete")

public class DeleteResource {
	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Gson g = new Gson();
	private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public DeleteResource() {
		
	}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON+";charset=utf-8")
	public Response deleteV1(pt.unl.fct.di.apdc.firstwebapp.util.DeleteData data) {
		
		Key userKey = KeyFactory.createKey("User",  data.username);
		datastore.delete(userKey);
		LOG.info("User deleted "+data.username);
		return Response.ok().build();
	}

}
