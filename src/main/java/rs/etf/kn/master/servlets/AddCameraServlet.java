/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.dataSource.camera.CamProcessingManager;
import rs.etf.kn.master.model.Camera;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.model.Location;

/**
 *
 * @author NikLik
 */
public class AddCameraServlet extends HttpServlet {

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

        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String ipAddress = request.getParameter("ipAddress");
        String l = request.getParameter("location");
        try {
            Location location = new Gson().fromJson(l, Location.class);
            String id = Configuration.get().getNextId() + "";
            Camera newCam = null;
            if ("file".equals(type)) {
                String srcFile = Configuration.TEMP_DIR + "upload.mp4";
                String dstFile = Configuration.VIDEOS_DIR + id + ".mp4";
                Files.move(Paths.get(srcFile), Paths.get(dstFile), StandardCopyOption.REPLACE_EXISTING);
                newCam = new Camera(id, name, type, null, "/videos/" + id + ".mp4", location);
                Configuration.get().addCamera(newCam);
            }else if ("ip".equals(type)){
                newCam = new Camera(id, name, type, ipAddress, null, location);
                Configuration.get().addCamera(newCam);
            }
            if(newCam != null){
                CamProcessingManager.addCamera(newCam);
            }
            response.getWriter().write(new Gson().toJson(newCam));
        } catch (JsonSyntaxException e) {
            response.getWriter().write("{error: \"" + e.getMessage() + "\" }");
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
