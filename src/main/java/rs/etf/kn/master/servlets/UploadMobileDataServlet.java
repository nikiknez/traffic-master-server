package rs.etf.kn.master.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.maps.PendingResult;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.dataSource.StreetDataManager;
import rs.etf.kn.master.dataSource.StreetDataSource;
import rs.etf.kn.master.dataSource.mobile.MobileStreetData;
import rs.etf.kn.master.main.Main;
import rs.etf.kn.master.model.Location;

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
        response.setContentType("application/json");

        try {
            String dataParam = request.getParameter("data");
            LOG.log(Level.INFO, "Got data: {0}", dataParam);
            final double[][] data = new Gson().fromJson(dataParam, double[][].class);

            int i = 0;
            com.google.maps.model.LatLng[] queryPoints = new com.google.maps.model.LatLng[data.length];
            for (double[] p : data) {
                queryPoints[i++] = new com.google.maps.model.LatLng(p[0], p[1]);
            }

            RoadsApi.snapToRoads(Main.geoApiContext, true, queryPoints).setCallback(new PendingResult.Callback<SnappedPoint[]>() {
                @Override
                public void onResult(SnappedPoint[] t) {
                    LOG.log(Level.WARNING, "Got {0} points in result", t.length);
                    StreetDataSource mobileSource = StreetDataManager.getDataSource("mobile");
                    List<Location> path = new LinkedList<>();
                    int origIdx = 0;
                    String lastStreetId = t[0].placeId;
                    double speed = 0;
                    for (SnappedPoint sp : t) {
                        if (sp.originalIndex != -1) {
                            origIdx = sp.originalIndex;
                        }
                        Location l = new Location(sp.location.lat, sp.location.lng);

                        if (!sp.placeId.equals(lastStreetId)) {
                            speed /= path.size();
                            MobileStreetData msdata = new MobileStreetData(path, (int) speed);
                            if (mobileSource.getData(lastStreetId) != null) {
                                MobileStreetData oldData = (MobileStreetData) mobileSource.getData(lastStreetId);
                                Location lastPoint = oldData.getPath().get(oldData.getPath().size() - 1);
                                if (lastPoint.equals(l)) {
                                    oldData.getPath().addAll(path);
                                    oldData.setIntensity((int) speed);
                                    msdata = oldData;
                                }
                            }
                            mobileSource.addData(lastStreetId, msdata);
                            speed = 0;
                            path = new LinkedList<>();
                        }
                        lastStreetId = sp.placeId;
                        speed += data[origIdx][2];
                        path.add(l);
                        if (sp == t[t.length - 1]) {
                            speed /= path.size();
                            MobileStreetData msdata = new MobileStreetData(path, (int) speed);
                            mobileSource.addData(lastStreetId, msdata);
                        }
                    }
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
