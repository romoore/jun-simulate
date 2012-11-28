/*
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents a receiver to place.
 * 
 * @author Robert Moore
 * 
 */
public class Receiver extends Point2D.Float implements Drawable {

  /**
   * Auto-generated.
   */
  private static final long serialVersionUID = -6214708115166326996L;
  /**
   * The set of covering disks that overlap this receiver's position.
   */
  Collection<CaptureDisk> coveringDisks = new LinkedList<CaptureDisk>();

  @Override
  public void draw(Graphics2D g, float scaleX, float scaleY) {
    AffineTransform origTransform = g.getTransform();
    Color origColor = g.getColor();
    // g.translate((int)this.getX(),(int)this.getY());

    g.setColor(Color.YELLOW);
    
    Set<Transmitter> coveredTxers = new HashSet<Transmitter>();
    
    for (CaptureDisk d : this.coveringDisks) {
      Line2D line = new Line2D.Double(d.t1.getX()*scaleX, d.t1.getY()*scaleY,this.getX()*scaleX,this.getY()*scaleY);
//      Line2D line = new Line2D.Double(d.disk.getCenterX() * scaleX,
//          d.disk.getCenterY() * scaleY, this.getX() * scaleX, this.getY()
//              * scaleY);
      coveredTxers.add(d.t1);
      g.draw(line);
    }

    g.setColor(origColor);
    g.drawString("R" + coveredTxers.size(), (int) (this.getX() * scaleX),
      (int) (this.getY() * scaleY));
//    g.drawString("R" + this.coveringDisks.size(), (int) (this.getX() * scaleX),
//        (int) (this.getY() * scaleY));
    g.setTransform(origTransform);
  }
}
