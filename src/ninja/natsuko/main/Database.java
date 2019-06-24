package ninja.natsuko.main;

import java.io.IOException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class Database {
	private MongoClient mongoClient;
	
	public DB database;
	
	public Database(MongoClientURI uri, String database) throws IOException {
		this.mongoClient = new MongoClient(uri);
		this.database = this.mongoClient.getDB(database);
	}
}
