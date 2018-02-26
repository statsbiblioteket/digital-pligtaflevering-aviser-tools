/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author tra
 */
@WebServlet(name = "ShowItem", urlPatterns = {"/ShowItem"})
public class ShowItem extends HttpServlet {

    //@Inject
    volatile DomsRepository repository;

    @Inject
    @Named("doms.url")
    String domsUrl;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
/*
        {
            DomsItem item = (DomsItem) request.getAttribute("item");
            if (item == null) {
                item = (DomsItem) pageContext.getAttribute("item");  // c:forEach var="item"
            }
            if (item == null) {
                ConfigurationMap map = new ConfigurationMap(ServletContextHelper.getInitParameterMap(request.getServletContext()));

                DomsRepository repository = new RepositoryConfigurator().apply(map);
                item = repository.lookup(new DomsId(request.getParameter("id")));
                request.setAttribute("item", item);
            }
        }
*/
        if (repository == null) {
            synchronized (this) {
                if (repository == null) {
                    ConfigurationMap map = new ConfigurationMap(ServletContextHelper.getInitParameterMap(request.getServletContext()));
                    //noinspection deprecation
                    repository = new RepositoryConfigurator().apply(map);
                }
            }
        }

        DomsItem item = repository.lookup(new DomsId(request.getParameter("id")));
        request.setAttribute("item", item);

        request.setAttribute( "fedoraUrl", domsUrl + "/objects/" + item.getDomsId().id());

        getServletContext().getRequestDispatcher("/WEB-INF/showItem.jsp").forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    } // </editor-fold>

}
