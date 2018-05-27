/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.LinkedList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.model.Location;
import rs.etf.kn.master.model.Street;
import rs.etf.kn.master.model.User;

/**
 *
 * @author NikLik
 */
public class AddStreetServlet extends HttpServlet {

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

        String path = request.getParameter("path");

        User owner = (User) request.getSession().getAttribute("user");

        Location[] p = new Gson().fromJson(path, Location[].class);
        Street newStreet = new Street(Configuration.get().getNextId() + "", p, owner.getUsername(), null, null, null);
        
        LinkedList<Street> newStreets = (LinkedList<Street>) request.getSession().getAttribute("newStreets");
        if(newStreets == null){
            newStreets = new LinkedList<>();
            request.getSession().setAttribute("newStreets", newStreets);
        }
        newStreets.add(newStreet);

        String responseText = "{\"owner\":\"" + newStreet.getOwner() + "\",\"id\":\"" + newStreet.getId() + "\"}";
        System.out.println(responseText);
        response.getWriter().write(responseText);
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
