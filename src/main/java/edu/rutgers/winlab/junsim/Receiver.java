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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents a receiver to place.
 * 
 * @author Robert Moore
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
  
  public float score = 0;
  
  static final BufferedImage hatchImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
  static final Rectangle2D hatchAnchor = new Rectangle2D.Double(0, 0, 5, 5);
  static TexturePaint hatchPaint = new TexturePaint(hatchImage, hatchAnchor);
  
  static {
    Graphics2D g2 = hatchImage.createGraphics();
    g2.setColor(FileRenderer.getReceiverColor());
    g2.fillRect(0, 0, 5, 5);
    g2.setColor(FileRenderer.getStrokeColor());
//    g2.setStroke(new BasicStroke(2));
    g2.drawLine(0, 0, 5, 5); // \
    g2.drawLine(0, 5, 5, 0); // /
    g2.dispose();
  }
  

  @Override
  public void draw(Graphics2D g, float scaleX, float scaleY) {
    AffineTransform origTransform = g.getTransform();
    Color origColor = g.getColor();
    int coverageCount = 0;

    HashMap<Transmitter, Integer> coverageCounts = new HashMap<Transmitter, Integer>();

    for (CaptureDisk d : this.coveringDisks) {
      Integer count = coverageCounts.get(d.t1);
      if (count == null) {
        count = new Integer(0);
      }
      coverageCounts.put(d.t1, Integer.valueOf(count.intValue() + 1));

    }
    coverageCount = coverageCounts.size();
    Stroke origStroke = g.getStroke();
    if (Main.gfxConfig.isDrawReceiverLines()) {
      for (Transmitter tx : coverageCounts.keySet()) {
        int totalDisks = tx.getDisks().size();
        float coverageRate = ((float) coverageCounts.get(tx).intValue())
            / totalDisks;

        Line2D line = new Line2D.Double(tx.getX() * scaleX, tx.getY() * scaleY,
            this.getX() * scaleX, this.getY() * scaleY);
        g.setStroke(new BasicStroke(FileRenderer
            .getThicknessForPercent(coverageRate), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND));
        g.setColor(FileRenderer.getColorForPercent(coverageRate));
        g.draw(line);

      }
    }
    g.setStroke(origStroke);

    if (Main.gfxConfig.isDrawReceivers()) {
      
      // Draw a partly-translucent triangle to make the receiver
      // more easily visible
      float size = FileRenderer.getRadiusForPercent(1f)*1.5f;
      GeneralPath triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
      triangle.moveTo(-size, size);
      triangle.lineTo(size, size);
      triangle.lineTo(0, -size);
      triangle.closePath();
      
      g.translate((int) (this.getX() * scaleX), (int) (this.getY() * scaleY)-1);
      
      Composite origComposite = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.setColor(FileRenderer.getBackgroundColor());
      g.fill(triangle);
      g.setComposite(origComposite);
      
      g.translate(0,1);
      
      // Now draw the receiver triangle
      size = FileRenderer.getRadiusForPercent(1f)*1.2f;
      triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
      triangle.moveTo(-size, size);
      triangle.lineTo(size, size);
      triangle.lineTo(0, -size);
      triangle.closePath();
      g.setColor(FileRenderer.getReceiverColor());
//      g.setPaint(new TexturePaint(hatchImage, hatchAnchor));
      
      Paint origPaint = g.getPaint();

      
      g.fill(triangle);
      g.setPaint(origPaint);
      g.setColor(origColor);
      g.draw(triangle);
      
      FontMetrics metrics = g.getFontMetrics();
      final String drawnString = "R" + this.coveringDisks.size() + "/"
          + coverageCount;
      Rectangle2D.Float box = (Rectangle2D.Float) metrics.getStringBounds(
          drawnString, null);
      g.setColor(FileRenderer.colorSet.getBackgroundColor());
      origComposite = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.translate((int) size, (int) -size);
//      g.fill(box);
      g.setColor(origColor);
      g.setComposite(origComposite);
//      g.drawString(drawnString, 0, 0);
    }
    g.setColor(origColor);
    g.setTransform(origTransform);
  }
}
