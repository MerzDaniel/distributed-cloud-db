package server.kv;

public class DbError extends Exception {
    public DbError(Exception e) {
        super(e);
    }
}
