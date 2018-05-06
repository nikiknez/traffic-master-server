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
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;
import rs.etf.kn.master.opencv.PerspectiveTransformator;

/**
 *
 * @author NikLik
 */
public class ConfigCameraServlet extends HttpServlet {

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

        try {
            String poly = request.getParameter("poly");
            Point2D.Float[] polyPoints = new Gson().fromJson(poly, Point2D.Float[].class);
            request.getSession().setAttribute("configCameraPolygon", polyPoints);
            
            BufferedImage pausedFrame = (BufferedImage) request.getSession().getAttribute("currentFrame");
            
            String videoFileName = request.getParameter("videoFileName");
            String videoTime = request.getParameter("videoTime");
            if(videoFileName != null && videoTime != null){
                double videoTimeValue = Double.parseDouble(videoTime);
                pausedFrame = OpenCV.readFrame(Configuration.BASE_DIR + videoFileName, videoTimeValue);
                request.getSession().setAttribute("currentFrame", pausedFrame);
            }
            OpenCV.scalePoints(polyPoints, pausedFrame.getWidth(), pausedFrame.getHeight());
            BufferedImage outImg = PerspectiveTransformator.fourPointTransform(pausedFrame, polyPoints);
            request.getSession().setAttribute("transformedImage", outImg);
            ImageIO.write(outImg, "jpg", new File(Configuration.TEMP_DIR + "transformed.jpg"));
            
            response.getWriter().write("ok");
        } catch (NumberFormatException | JsonSyntaxException | NullPointerException | FileNotFoundException e) {
            response.getWriter().write(e.getMessage());
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
