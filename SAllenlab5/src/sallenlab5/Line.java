/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sallenlab5;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 *
 * @author sallen
 */
public class Line implements Serializable
{
    
    public Point2D.Double mTo, mFrom;
    public int width;
    public transient Color color;
    
    public Line(Point2D.Double to, Point2D.Double from, int width, Color color) {
        this.mTo = to;
        this.mFrom = from;
        this.width = width;
        this.color = color;
    }
    
    public void draw(Canvas canv) {
        GraphicsContext gc = canv.getGraphicsContext2D();
        gc.setLineWidth(width);
        gc.setStroke(color);
        if(canv.contains(mTo.x, mTo.y)) {
            gc.strokeLine(mFrom.x, mFrom.y, mTo.x, mTo.y);
        }       
    }
    
    public void writeObject(ObjectOutputStream out) {
        try {
            out.defaultWriteObject();
        } catch (IOException ex) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readObject(ObjectInputStream in) {
        try {
            in.defaultReadObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, e);
        }
    }    
    
    public static Color getNextColor(Color curColor) {

        if(curColor == Color.BLACK) {
            return Color.RED;
        } else if (curColor == Color.RED) {
            return Color.BLUE;
        } else if (curColor == Color.GREEN) {
            return Color.BLACK;
        } else if (curColor == Color.BLUE) {
            return Color.GREEN;
        }
        return Color.BLACK;
    }
}
