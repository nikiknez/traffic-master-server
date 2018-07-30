package rs.etf.kn.master.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.dataSource.StreetDataManager;
import rs.etf.kn.master.dataSource.mobile.MobileStreetDataSource;
import rs.etf.kn.master.main.Main;

public class UploadMobileDataServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UploadMobileDataServlet.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        try {
            String dataParam = request.getParameter("data");
            LOG.log(Level.INFO, "Got data: {0}", dataParam);
            double[][] data = new Gson().fromJson(dataParam, double[][].class);

            int i = 0;
            com.google.maps.model.LatLng[] queryPoints = new com.google.maps.model.LatLng[data.length];
            final double[] speeds = new double[data.length];
            for (double[] p : data) {
                speeds[i] = p[2];
                queryPoints[i++] = new com.google.maps.model.LatLng(p[0], p[1]);
            }

            RoadsApi.snapToRoads(Main.geoApiContext, true, queryPoints).setCallback(new PendingResult.Callback<SnappedPoint[]>() {
                @Override
                public void onResult(SnappedPoint[] sp) {
                    MobileStreetDataSource source = (MobileStreetDataSource) StreetDataManager.getDataSource("mobile");
                    source.addData(sp, speeds);
                }

                @Override
                public void onFailure(Throwable thrwbl) {
                    LOG.severe("Got error in response");
                }
            });

            response.getWriter().write("ok");
        } catch (JsonSyntaxException e) {
            LOG.severe(e.toString());
        }
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
    }// </editor-fold>

}
