package foo;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 *
 */
public class T1 {
    private DataSource bar;

    @Resource(name = "foobar")
    public void setBar(javax.sql.DataSource bar) {
        this.bar= bar;
        System.err.println("IN SETBAR");
    }

    public String foo() {
        return "Hello World " + new java.util.Date() + " -> " + bar;
    }
}
