/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2020, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * -----------------
 * XYEdgeRenderer.java
 * -----------------
 * (C) Copyright 2020, by ESI (TNO).
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 * Contributor(s):   -;
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.BooleanList;
import org.jfree.chart.util.LineUtils;
import org.jfree.chart.util.ShapeList;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

public class XYEdgeRenderer extends AbstractXYItemRenderer implements XYItemRenderer {
	private static final long serialVersionUID = -754074656456575380L;
	
	public static final boolean DEFAULT_SHAPE_ROTATE = true;
	
	private final ShapeList beginShapes = new ShapeList();
	private final BooleanList beginShapesRotate = new BooleanList();
	private final ShapeList endShapes = new ShapeList();
	private final BooleanList endShapesRotate = new BooleanList();
	private final BooleanList drawCrossingLines = new BooleanList();
	
	private Shape baseBeginShape = null;
	private Boolean baseBeginShapeRotate = null;
	private Shape baseEndShape = null;
	private Boolean baseEndShapeRotate = null;
	private boolean baseShapeRotate = false;
	private boolean baseDrawCrossingLines = true;
	
	public XYEdgeRenderer() {
		this.baseShapeRotate = DEFAULT_SHAPE_ROTATE;
		setDefaultShape(new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0));
		setAutoPopulateSeriesShape(false);
	}
	
	public void setBaseDrawCrossingLines(boolean drawCrossingLines, boolean notify) {
		this.baseDrawCrossingLines = drawCrossingLines;
		if (notify) {
			fireChangeEvent();
		}
	}
	
	/**
	 * @return true to render lines which start and end outside, but cross the visible area of the plot 
	 */
	public boolean getBaseDrawCrossingLines() {
		return this.baseDrawCrossingLines;
	}
	
	/**
	 * @see #getBaseDrawCrossingLines()
	 */
	public boolean getSeriesDrawCrossingLines(int series) {
		Boolean drawCrossingLines = this.drawCrossingLines.getBoolean(series);
		return null == drawCrossingLines ? getBaseDrawCrossingLines() : drawCrossingLines;
	}

	public void setSeriesDrawCrossingLines(int series, boolean drawCrossingLines, boolean notify) {
		this.drawCrossingLines.setBoolean(series, drawCrossingLines);
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public void setBaseShapeRotate(boolean baseShapeRotate) {
		this.baseShapeRotate = baseShapeRotate;
	}
	
	public boolean getBaseShapeRotate(boolean baseShapeRotate) {
		return this.baseShapeRotate;
	}
	
	public void setBaseBeginShape(Shape baseBeginShape, boolean notify) {
		this.baseBeginShape = baseBeginShape;
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public Shape getBaseBeginShape() {
		return baseBeginShape;
	}
	
	public void setSeriesBeginShape(int series, Shape shape, boolean notify) {
		this.beginShapes.setShape(series, shape);
		if (notify) {
			fireChangeEvent();
		}
	}

	public Shape getSeriesBeginShape(int series) {
		Shape shape = this.beginShapes.getShape(series);
		return null == shape ? this.baseBeginShape : shape;
	}

	public void setSeriesBeginShapeRotate(int series, boolean rotate, boolean notify) {
		this.beginShapesRotate.setBoolean(series, rotate);
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public void setBaseBeginShapeRotate(Boolean baseBeginShapeRotate, boolean notify) {
		this.baseBeginShapeRotate = baseBeginShapeRotate;
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public Boolean getBaseBeginShapeRotate() {
		return null == this.baseBeginShapeRotate ? this.baseShapeRotate : this.baseBeginShapeRotate;
	}
	
	public boolean getSeriesBeginShapeRotate(int series) {
		Boolean rotate = this.beginShapesRotate.getBoolean(series);
		return null == rotate ? getBaseBeginShapeRotate() : rotate;
	}
	
	public void setBaseEndShape(Shape baseEndShape, boolean notify) {
		this.baseEndShape = baseEndShape;
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public Shape getBaseEndShape() {
		return baseEndShape;
	}

	public void setSeriesEndShape(int series, Shape shape, boolean notify) {
		this.endShapes.setShape(series, shape);
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public Shape getSeriesEndShape(int series) {
		Shape shape = this.endShapes.getShape(series);
		return null == shape ? this.baseEndShape : shape;
	}
	
	public void setBaseEndShapeRotate(Boolean baseEndShapeRotate, boolean notify) {
		this.baseEndShapeRotate = baseEndShapeRotate;
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public Boolean getBaseEndShapeRotate() {
		return null == this.baseEndShapeRotate ? this.baseShapeRotate : this.baseEndShapeRotate;
	}
	
	public void setSeriesEndShapeRotate(int series, boolean rotate, boolean notify) {
		this.endShapesRotate.setBoolean(series, rotate);
		if (notify) {
			fireChangeEvent();
		}
	}
	
	public boolean getSeriesEndShapeRotate(int series) {
		Boolean rotate = this.endShapesRotate.getBoolean(series);
		return null == rotate ? getBaseEndShapeRotate() : rotate;
	}
	
	@Override
	public LegendItem getLegendItem(int datasetIndex, int series) {
		LegendItem item = super.getLegendItem(datasetIndex, series);
		item.setShape(lookupLegendShape(series));
		return item;
	}

	@Override
	public Shape lookupSeriesShape(int series) {
		Shape shape = getSeriesEndShape(series);
		if (null == shape) {
			shape = getSeriesBeginShape(series);
		}
		if (null == shape) {
			shape = super.lookupSeriesShape(series);
		}
		return shape;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea, XYPlot plot, XYDataset data,
			PlotRenderingInfo info) {
		XYItemRendererState state = super.initialise(g2, dataArea, plot, data, info);
		// disable visible items optimization - it doesn't work for this renderer...
		state.setProcessVisibleItemsOnly(false);
		return state;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
			XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
			CrosshairState crosshairState, int pass) {
        if (!getItemVisible(series, item) || ! (dataset instanceof IntervalXYDataset<?>)) {
            return;
        }

        IntervalXYDataset<?> intervalDataset = (IntervalXYDataset) dataset;
        double X0 = intervalDataset.getStartXValue(series, item);
        double Y0 = intervalDataset.getStartYValue(series, item);
        double X1 = intervalDataset.getEndXValue(series, item);
        double Y1 = intervalDataset.getEndYValue(series, item);
        // only draw if we have good values
		if (Double.isNaN(X0) || Double.isNaN(Y0) || Double.isNaN(X1) || Double.isNaN(Y1)) {
			return;
		}        

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double transX0 = domainAxis.valueToJava2D(X0, dataArea, xAxisLocation);
        double transY0 = rangeAxis.valueToJava2D(Y0, dataArea, yAxisLocation);
        double transX1 = domainAxis.valueToJava2D(X1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(Y1, dataArea, yAxisLocation);
        
		// only draw if we have good values
		if (Double.isNaN(transX0) || Double.isNaN(transY0) || Double.isNaN(transX1) || Double.isNaN(transY1)) {
			return;
		}

        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            state.workingLine.setLine(transY0, transX0, transY1, transX1);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            state.workingLine.setLine(transX0, transY0, transX1, transY1);
        }
    	boolean edgeVisible = drawEdge(g2, state, dataArea, dataset, series, item);
        if (edgeVisible && isItemLabelVisible(series, item)) {
        	double centerX = transX0 * 0.5 + transX1 * 0.5;
        	double centerY = transY0 * 0.5 + transY1 * 0.5;
        	drawItemLabel(g2, orientation, dataset, series, item, centerX, centerY, false);
        }
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean drawEdge(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, XYDataset dataset,
			int series, int item) {
		Line2D line = state.workingLine;
		
		boolean beginVisible = dataArea.contains(line.getP1());
		boolean endVisible = dataArea.contains(line.getP2());
        if (!beginVisible && !endVisible && !getSeriesDrawCrossingLines(series)) {
        	return false;
        }
		
		// Copy the line before clipping as we need it to create begin/end shapes
		Line2D fullLine = (Line2D) line.clone();
        boolean lineVisible = LineUtils.clipLine(line, dataArea);
        if (!lineVisible) {
        	return false;
        }
        
	    Stroke itemStroke = getItemStroke(series, item);
        g2.setStroke(itemStroke);
        g2.setPaint(getItemPaint(series, item));
    	g2.draw(line);
		
        Shape beginShape = null;
        if (beginVisible) {
        	beginShape = createBeginShape(fullLine, series);
        	if (null != beginShape) {
        		g2.fill(beginShape);
        	}
        }
        Shape endShape = null;
        if (endVisible) {
        	endShape = createEndShape(fullLine, series);
        	if (null != endShape) {
        		g2.fill(endShape);
        	}
        }
        
        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
        	Area hotspot = new Area(ShapeUtils.createLineRegion(line, calculateLineWidth(itemStroke)));
            if (null != beginShape) {
            	hotspot.add(new Area(beginShape));
            }
            if (null != endShape) {
            	hotspot.add(new Area(endShape));
            }
            hotspot.intersect(new Area(dataArea));
            addEntity(entities, hotspot, dataset, series, item, 0.0, 0.0);
        }
        return true;
	}
	
	protected Shape createBeginShape(Line2D line, int series) {
		Shape shape = getSeriesBeginShape(series);
		if (null == shape) {
			return null;
		}
		double tx = shape.getBounds2D().getCenterX();
		double ty = shape.getBounds2D().getCenterY();
		AffineTransform aff = new AffineTransform();
		aff.translate(line.getX1() - tx, line.getY1() - ty);
		if (getSeriesBeginShapeRotate(series)) {
			double angle = Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());
			aff.rotate(angle, tx, ty);
		}
		return aff.createTransformedShape(shape);
	}
	
	protected Shape createEndShape(Line2D line, int series) {
		Shape shape = getSeriesEndShape(series);
		if (null == shape) {
			return null;
		}
		double tx = shape.getBounds2D().getCenterX();
		double ty = shape.getBounds2D().getCenterY();
		AffineTransform aff = new AffineTransform();
		aff.translate(line.getX2() - tx, line.getY2() - ty);
		if (getSeriesEndShapeRotate(series)) {
			double angle = Math.atan2(line.getY1() - line.getY2(), line.getX1() - line.getX2());
			aff.rotate(angle, tx, ty);
		}
		return aff.createTransformedShape(shape);
	}
	
	protected float calculateLineWidth(Stroke stroke) {
		return (float) stroke.createStrokedShape(new Line2D.Double(0, 0, 100, 0)).getBounds2D().getHeight();
	}
}
