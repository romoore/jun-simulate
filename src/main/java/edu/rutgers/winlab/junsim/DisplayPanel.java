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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import delaunay.Pnt;
import delaunay.Triangle;
import delaunay.Triangulation;

/**
 * @author Robert Moore
 */
public class DisplayPanel extends JPanel {

  private Collection<Drawable> devices = new LinkedList<Drawable>();
  private Collection<CaptureDisk> disks = new LinkedList<CaptureDisk>();
  private Collection<Point2D> points = new LinkedList<Point2D>();
  private List<Collection<Point2D>> rankedPoints = null;
  private List<Integer> ranks = null;
  private Collection<Receiver> receiverPoints = new LinkedList<Receiver>();
  private Collection<CaptureDiskGroup> groups = new LinkedList<CaptureDiskGroup>();

  private Color backgroundColor = Color.BLACK;
  private Color fontColor = Color.WHITE;

  private int pointRadius = 1;

  private boolean doDelaunay = false;
  private static int initialSize = 30000; // Size of initial triangle
  protected Triangle initialTriangle = null;

  public DisplayPanel() {
   this(false);
  }
  
  public DisplayPanel(final boolean doDelaunay) {
    super();
    this.setPreferredSize(new Dimension(640, 480));
    this.initialTriangle = new Triangle(new Pnt(-initialSize, -initialSize),
        new Pnt(initialSize, -initialSize), new Pnt(0, initialSize));
    
    this.doDelaunay = doDelaunay;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!this.isVisible()) {
      return;
    }
    this.render(g, this.getWidth(), this.getHeight());

  }

  public void render(Graphics g, int width, int height) {

    // Figure-out the scaling based on aspect-ratios

    float displayRatio = 1f * width / height;
    float scale = 1;
    // float scaleY = 1;
    // Widescreen

    Graphics2D g2 = (Graphics2D) g;

    AffineTransform origTransform = g2.getTransform();

    if (displayRatio > 1.001f) {
      scale = height / Main.config.universeHeight;
      int marginX = width - (int) (Main.config.universeWidth * scale);
      g2.translate(marginX / 2, 0);
    }
    // Tall-screen
    else if (displayRatio < 0.999f) {
      scale = width / Main.config.universeWidth;
      int marginY = height - (int) (Main.config.universeHeight * scale);
      g2.translate(0, marginY / 2);
    }

    // Draw background color
    g2.setColor(this.backgroundColor);
    g2.fillRect(0, 0, (int) Main.config.universeWidth + 1,
        (int) Main.config.universeHeight + 1);

    // Capture disks (for overlapping arcs)
    g2.setColor(Color.RED);

    for (CaptureDisk d : this.disks) {
      d.draw(g2, scale, scale);
    }

    // Solution points
    g2.setColor(Color.GREEN);

    for (Point2D p : this.points) {
      g2.fillOval((int) (p.getX() * scale) - 1, (int) (p.getY() * scale) - 1,
          2, 2);
    }

    // Ranked points (for binned experiments)
//    Composite origComposite = g2.getComposite();
//    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
    int pointDiam = this.pointRadius * 2;
    if (this.rankedPoints != null) {
     
      
      
      
      if (this.doDelaunay) {
        int totalPointSize = 0;
        for(Collection<Point2D> counter : this.rankedPoints){
          totalPointSize += counter.size();
        }
        JProgressBar prog = new JProgressBar(0, totalPointSize);
        prog.setStringPainted(true);
        JFrame frame = new JFrame("Progress!");
        frame.add(prog);
        frame.pack();
        frame.setVisible(true);
        totalPointSize = 0;
        Triangulation dt = new Triangulation(this.initialTriangle);
        HashMap<Pnt, Color> pointColors = new HashMap<Pnt, Color>();
        // int i = this.rankedPoints.size()-1;
        float numRanks = this.rankedPoints.size();
        for (int i = this.rankedPoints.size() - 1; i >= 0; --i) {
          Collection<Point2D> usedPoints = this.rankedPoints.get(i);
          // for (Collection<Point2D> points : this.rankedPoints) {
          float hue = (i / numRanks) * 0.9f;
          Color c = Color.getHSBColor(hue, .9f, .9f);
          
          
          for (Point2D point : usedPoints) {
            Pnt newPnt = new Pnt(point.getX() * scale, point.getY() * scale);
            dt.delaunayPlace(newPnt);
            
            pointColors.put(newPnt,c);
            ++totalPointSize;
            prog.setValue(totalPointSize);
          }
          
//          totalPointSize += usedPoints.size();
//          prog.setValue(totalPointSize);
          

        }
        
        
        HashSet<Pnt> done = new HashSet<Pnt>(this.initialTriangle);
        prog.setMaximum(dt.size());
        prog.setValue(0);
        int currTri = 0;
        for (Triangle triangle : dt) {
          currTri++;
          prog.setValue(currTri);
          for (Pnt site : triangle) {
            if (done.contains(site))
              continue;
            done.add(site);
            List<Triangle> list = dt.surroundingTriangles(site, triangle);
            Color c = pointColors.get(site);
            if(list.contains(this.initialTriangle)){
              c = Color.BLACK;
            }
            Pnt[] vertices = new Pnt[list.size()];
            int j = 0;
            for (Triangle tri : list)
              vertices[j++] = tri.getCircumcenter();
            
            g2.setColor(c);
            this.draw(g, vertices);

          }
        }
        frame.dispose();
      } else {
        float numRanks = this.rankedPoints.size();
        for (int i = 0; i < this.rankedPoints.size();++i) {
          Collection<Point2D> thePoints = this.rankedPoints.get(i);
          // for (Collection<Point2D> points : this.rankedPoints) {
          float hue = (i / numRanks) * 0.9f;
          Color c = Color.getHSBColor(hue, .9f, .9f);
          g2.setColor(c);

          for (Point2D p : thePoints) {
            g2.fillOval((int) (p.getX() * scale) - this.pointRadius,
                (int) (p.getY() * scale) - this.pointRadius, pointDiam, pointDiam);
          }
          

        }
        

        // ++i;
      }
    }

//    g2.setComposite(origComposite);

    // Transmitters
    g2.setColor(this.fontColor);
    for (Drawable d : this.devices) {
      d.draw(g2, scale, scale);
    }

    g2.setColor(Color.BLUE);
    for (Receiver p : this.receiverPoints) {
      p.draw(g2, scale, scale);
    }

    // Capture disks groups
    if (!this.groups.isEmpty()) {
      float numGroups = this.groups.size();
      int i = 0;
      for (CaptureDiskGroup grp : this.groups) {
        float hue = i / numGroups;
        Color c = Color.getHSBColor(hue, .9f, .9f);
        g2.setColor(c);
        grp.draw(g2, scale, scale);
        ++i;
      }
    }

    g2.setTransform(origTransform);

    /*
     * Legend goes from maxX-30 -> maxX-10
     * from minY+10 -> minY+110
     * Looks like this:
     * +-----+
     * | High|
     * | Med |
     * | Low |
     * +-----+
     */
    if (this.rankedPoints != null) {
      FontMetrics metrics = g2.getFontMetrics();
      int fontHeight = metrics.getHeight();
      int fontWidth = metrics.stringWidth("0000");

      Rectangle2D legendBox = new Rectangle2D.Float(width - fontWidth - 40,
          10 - fontHeight, width - 2, 110 + fontHeight);

      g2.setColor(this.backgroundColor);
      g2.fill(legendBox);
      g2.setColor(this.fontColor);
      g2.draw(legendBox);

      float numRanks = this.rankedPoints.size();

      float rankHeight = 100f / numRanks;
      float currStartY = 110;

      float lastFontStart = height;

      float maxRankSize = 0;
      for (Collection<Point2D> coll : this.rankedPoints) {
        int size = coll.size();
        if (size > maxRankSize) {
          maxRankSize = size;
        }
      }

      int numRanksInt = this.rankedPoints.size();

      for (int i = 0; i < numRanksInt; ++i, currStartY -= rankHeight) {
        int numPoints = this.rankedPoints.get(i).size();
        float hue = (i / numRanks) * 0.9f;
        Color c = Color.getHSBColor(hue, .9f, .9f);
        g2.setColor(c);
        float barWidth = (numPoints / maxRankSize) * 30;
        if (barWidth < 1) {
          barWidth = 1;
        }

        Rectangle2D.Float rect = new Rectangle2D.Float(width - fontWidth - 35,
            currStartY, barWidth, rankHeight);
        g2.fill(rect);
        if (currStartY < lastFontStart - fontHeight) {
          g2.setColor(this.fontColor);
          g2.drawString(String.format("%d", this.ranks.get(i)), width
              - fontWidth - 5, currStartY + (fontHeight / 2f));
          lastFontStart = currStartY;
        }

      }
    }
    g2.setColor(this.fontColor);

    g2.drawString(
        "T" + this.devices.size() + " R" + this.receiverPoints.size(), 0,
        height);
  }

  /**
   * Draw a polygon.
   * 
   * @param polygon
   *          an array of polygon vertices
   * @param fillColor
   *          null implies no fill
   */
  public void draw(final Graphics g, Pnt[] polygon) {

    int[] x = new int[polygon.length];
    int[] y = new int[polygon.length];
    for (int i = 0; i < polygon.length; i++) {
      x[i] = (int) polygon[i].coord(0);
      y[i] = (int) polygon[i].coord(1);
    }
    g.fillPolygon(x, y, polygon.length);
  }

  public void setTransmitters(Collection<Transmitter> devices) {
    this.devices.clear();
    this.devices.addAll(devices);
    this.repaint(10);
  }

  public void clear() {
    this.devices.clear();
    this.disks.clear();
    this.points.clear();
    this.groups.clear();
    this.rankedPoints = null;
    this.ranks = null;
    this.repaint(10);
  }

  public void setCaptureDisks(Collection<CaptureDisk> disks) {
    this.disks.clear();
    this.disks.addAll(disks);
    this.repaint(10);
  }

  public void setSolutionPoints(Collection<Point2D> points) {
    this.points.clear();
    this.points.addAll(points);
    this.repaint(10);
  }

  public void setRankedSolutionPoints(List<Collection<Point2D>> points,
      List<Integer> ranks) {
    this.rankedPoints = points;
    this.ranks = ranks;
  }

  public void setReceiverPoints(Collection<Receiver> points) {
    this.receiverPoints.clear();
    this.receiverPoints.addAll(points);
    this.repaint(10);
  }

  public void setCaptureDiskGroups(Collection<CaptureDiskGroup> groups) {
    this.groups.clear();
    this.groups.addAll(groups);
    this.repaint(10);
  }
}
