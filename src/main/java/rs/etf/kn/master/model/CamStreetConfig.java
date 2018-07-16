/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.model;

import java.awt.geom.Point2D;

/**
 *
 * @author NikLik
 */
public class CamStreetConfig {
    
    private String streetId;
    private float metersPerPixelRatio;
    private Point2D.Float[] polyPoints = null;

    public CamStreetConfig(String streetId, Point2D.Float[] polyPoints, float metersPerPixelRatio) {
        this.streetId = streetId;
        this.polyPoints = polyPoints;
        this.metersPerPixelRatio = metersPerPixelRatio;
    }

    public CamStreetConfig(String streetId) {
        this.streetId = streetId;
    }
    

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public Point2D.Float[] getPolyPoints() {
        return polyPoints;
    }

    public void setPolyPoints(Point2D.Float[] polyPoints) {
        this.polyPoints = polyPoints;
    }

    public float getMetersPerPixelRatio() {
        return metersPerPixelRatio;
    }

    public void setMetersPerPixelRatio(float metersPerPixelRatio) {
        this.metersPerPixelRatio = metersPerPixelRatio;
    }
}
