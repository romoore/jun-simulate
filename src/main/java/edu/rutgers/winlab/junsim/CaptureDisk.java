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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * @author Robert Moore
 * 
 */
public class CaptureDisk implements Drawable {
  /**
   * The capture disk of (t1,t2);
   */
  public Ellipse2D.Float disk;
  /**
   * The transmitter that successfully transmits a packet.
   */
  public Transmitter t1;
  /**
   * The transmitter that collides.
   */
  public Transmitter t2;

  @Override
  public boolean equals(Object o) {
    if (o instanceof CaptureDisk) {
      return this.equals((CaptureDisk) o);
    }
    return super.equals(o);
  }

  public boolean equals(CaptureDisk c) {
    if (this.t1.equals(c.t1) && this.t2.equals(c.t2)) {
      return true;
    }
    return this.disk.equals(c.disk);
  }

  public void draw(Graphics2D g) {
    AffineTransform origTransform = g.getTransform();
    // g.translate(this.disk.getMinX(), this.disk.getMinY());
    g.draw(this.disk);
    g.setTransform(origTransform);
  }

  @Override
  public int hashCode() {
    return this.disk.hashCode();
  }
}