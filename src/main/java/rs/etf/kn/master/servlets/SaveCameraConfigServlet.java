/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.model.CamStreetConfig;
import rs.etf.kn.master.model.Camera;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.model.Street;

/**
 *
 * @author NikLik
 */
public class SaveCameraConfigServlet extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");

        String line = request.getParameter("line");
        String lineLengthParam = request.getParameter("lineLength");
        String streetId = request.getParameter("streetId");
        String camId = request.getParameter("cameraId");
        Camera cam = Configuration.get().getCamById(camId);

        try {
            Point2D.Float[] linePoints = new Gson().fromJson(line, Point2D.Float[].class);
            float lineLength = Float.parseFloat(lineLengthParam);

            float metersPerPixelRatio = calcMetersPerPixelRatio(linePoints, lineLength);
            Point2D.Float[] polyPoints = (Point2D.Float[]) request.getSession().getAttribute("configCameraPolygon");

            BufferedImage reperFrame = (BufferedImage) request.getSession().getAttribute("currentFrame");
            ImageIO.write(reperFrame, "jpg", new File(Configuration.REPERS_DIR + streetId + ".jpg"));

            CamStreetConfig camStreetConfig = new CamStreetConfig(streetId, polyPoints, metersPerPixelRatio);

            LinkedList<Street> newStreets = (LinkedList<Street>) request.getSession().getAttribute("newStreets");
            for (Street s : newStreets) {
                if (s.getId().equals(streetId)) {
                    cam.getStreets().add(camStreetConfig);
                    newStreets.remove(s);
                    Configuration.get().addStreet(s);
                    response.getWriter().write("ok");
                    return;
                }
            }
            response.getWriter().write("!");
        } catch (JsonSyntaxException | NumberFormatException e) {
            response.getWriter().write(e.getMessage());
        }
    }

    private float calcMetersPerPixelRatio(Point2D.Float[] pixelPoints, float distanceInMeters) {
        float distanceInPixels = (float) Math.sqrt((pixelPoints[0].x - pixelPoints[1].x) * (pixelPoints[0].x - pixelPoints[1].x)
                + (pixelPoints[0].y - pixelPoints[1].y) * (pixelPoints[0].y - pixelPoints[1].y));

        return distanceInMeters / distanceInPixels;
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
