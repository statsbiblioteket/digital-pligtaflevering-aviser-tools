package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 *
 */
@WebServlet(name = "ExecuteQ", urlPatterns = {"/ExecuteQ"})
public class ExecuteQServlet extends HttpServlet {

    @Inject
    DomsRepository repository;

    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String q = request.getParameter("q");

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<h1>Yes " + new java.util.Date() + "</h1>");
            long count = repository.count(new SBOIQuerySpecification(q));
            out.println(count + ".");

            request.setAttribute("l", repository.query(new SBOIQuerySpecification(q)).collect(Collectors.toList()));

            // and go back to presenter jsp
            getServletContext().getRequestDispatcher("/showQ.jsp").forward(request, response);

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
