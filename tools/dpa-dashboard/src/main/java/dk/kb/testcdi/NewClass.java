/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.kb.testcdi;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import java.util.function.Function;

import javax.enterprise.inject.Produces;
import javax.servlet.ServletContext;

/**
 * TODO: TEMPORAY DUMMY CLASS FOR TESTING.
 *
 * @author tra
 */
public class NewClass {

//    @javax.enterprise.inject.Produces
//    @javax.inject.Named("doms.username")
//    public String produceDomsUsername() {
//        return "DOMSUSERNAME!";
//    }
//    @javax.enterprise.inject.Produces
//    public ConfigurationMap produceConfigurationMap(ServletContext servletContext) {
//        Map<String, String> map = new HashMap<>();
//        map.put("doms.username", servletContext.getInitParameter("doms.username"));
//        map.put("doms.password", "PASSWORD");
//
//        ConfigurationMap map2 = new ConfigurationMap(map);
//        return map2;
//    }
//    @javax.enterprise.inject.Produces
//    @javax.inject.Named("doms.password")
//    public String produceDomsPassword(ConfigurationMap map) {
//        return map.getRequired("doms.password");
//    }
    //   @Produces
    //   public QuerySpecification produceQuerySpecification() {
    //       throw new IllegalStateException();
    //   }
//    @Produces
//    public DomsRepository produceDomsRepository() {
//        throw new IllegalStateException();
//    }
    @Produces
    public DefaultToolMXBean produceDefaultToolMXBean() {
        throw new IllegalStateException();
    }
    @Produces
    public Function<Throwable, String> getStacktraceForToolResultsReportTest() {
        return Throwables::getStackTraceAsString;
    }

}
