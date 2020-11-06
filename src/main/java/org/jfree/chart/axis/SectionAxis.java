/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2017, by Object Refinery Limited and Contributors.
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
 * --------------
 * AxisUtils.java
 * --------------
 * (C) Copyright 2000-2020, ESI (TNO) and Contributors.
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 *
 * Changes
 * -------
 * 
 */
package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.TickLabelEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.Range;

/**
 * An {@link Axis} that stacks multiple axes into a single axis, creating
 * {@link Section}s. Use this axis to combine e.g. {@link NumberAxis} and
 * {@link SymbolAxis} in a single axis.
 */
public class SectionAxis extends ValueAxis {
	private static final long serialVersionUID = 6418801819447160805L;

	public enum TooltipMode {
		/**
		 * Don't use tooltips.
		 */
		NEVER,
		/**
		 * Always show tooltips.
		 */
		ALWAYS,
		/**
		 * <b>Default:</b> Show tooltips only when label exceeds
		 * {@link SectionsAxis#setTickLabelMaxLength(int)}.
		 */
		MAX_EXCEEDED
	};

	/** The default grid band paint. */
	public static final Paint DEFAULT_GRID_BAND_PAINT = new Color(232, 234, 232, 128);

	/** The default paint for alternate grid bands. */
	public static final Paint DEFAULT_GRID_BAND_ALTERNATE_PAINT = new Color(0, 0, 0, 0); // transparent

	/** The default value for the lower margin (0%). */
	public static final double DEFAULT_LOWER_MARGIN = 0;

	/** The default value for the upper margin (0%). */
	public static final double DEFAULT_UPPER_MARGIN = 0;

	public static final double DEFAULT_SECTION_LENGTH = 1;

	public static final double DEFAULT_SECTION_GAP = 0;

	public static final int NO_TICK_LABEL_MAX_LENGTH = -1;

	private final LinkedList<Section> sections = new LinkedList<>();

	private double defaultSectionGap;

	private int tickLabelMaxLength;

	private TooltipMode tooltipMode;

	/** Flag that indicates whether or not grid bands are visible. */
	private boolean gridBandsVisible;

	/** The paint used to color the grid bands (if the bands are visible). */
	private transient Paint defaultGridBandPaint;

	/** The paint used to fill the alternate grid bands. */
	private transient Paint defaultGridBandAlternatePaint;

	/**
	 * Default constructor.
	 */
	public SectionAxis() {
		this(null);
	}

	/**
	 * Constructs a section axis, using default values where necessary.
	 *
	 * @param label the axis label ({@code null} permitted).
	 */
	public SectionAxis(String label) {
		super(label, new StandardTickUnitSource());
		setLowerMargin(DEFAULT_LOWER_MARGIN);
		setUpperMargin(DEFAULT_UPPER_MARGIN);
		this.gridBandsVisible = true;
		this.defaultGridBandPaint = DEFAULT_GRID_BAND_PAINT;
		this.defaultGridBandAlternatePaint = DEFAULT_GRID_BAND_ALTERNATE_PAINT;
		this.defaultSectionGap = DEFAULT_SECTION_GAP;
		this.tickLabelMaxLength = NO_TICK_LABEL_MAX_LENGTH;
		this.tooltipMode = TooltipMode.MAX_EXCEEDED;
	}

	/**
	 * Get the maximum number of section label characters visible.
	 * 
	 * @return the maximum number of section label characters visible
	 * @see #setTickLabelMaxLength(int)
	 */
	public int getTickLabelMaxLength() {
		return tickLabelMaxLength;
	}

	/**
	 * Set the maximum number of section label characters visible. If a section
	 * label is longer, its name will be clipped to {@link #getTickLabelMaxLength()}
	 * and post-fixed with '...'.
	 * 
	 * @param tickLabelMaxLength a positive number or
	 *                           {@link #NO_TICK_LABEL_MAX_LENGTH}.
	 */
	public void setTickLabelMaxLength(int tickLabelMaxLength) {
		this.tickLabelMaxLength = tickLabelMaxLength;
		fireChangeEvent();
	}

	/**
	 * Creates a tick label that adheres to the restrictions of this
	 * {@link SectionAxis}
	 * 
	 * @param sectionLabel the label to format
	 * @return a tick label that adheres to the restrictions of this
	 *         {@link SectionAxis}
	 * @see #setTickLabelMaxLength(int)
	 */
	protected String createTickLabel(String sectionLabel) {
		if (null == sectionLabel || tickLabelMaxLength < 0 || sectionLabel.length() <= tickLabelMaxLength) {
			return sectionLabel;
		}
		return sectionLabel.substring(0, tickLabelMaxLength) + "...";
	}

	/**
	 * Returns the {@link TooltipMode} to use for this axis.
	 * 
	 * @return the {@link TooltipMode} to use for this axis.
	 * @see #setTooltipMode(TooltipMode)
	 */
	public TooltipMode getTooltipMode() {
		return tooltipMode;
	}

	/**
	 * Sets the {@link TooltipMode} to use for this axis.
	 * 
	 * @param tooltipMode the {@link TooltipMode} to use for this axis.
	 * @see TooltipMode
	 */
	public void setTooltipMode(TooltipMode tooltipMode) {
		Args.nullNotPermitted(tooltipMode, "TooltipMode");
		this.tooltipMode = tooltipMode;
	}

	/**
	 * Returns <code>true</code> if the grid bands are showing, and
	 * <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the grid bands are showing, and
	 *         <code>false</code> otherwise.
	 *
	 * @see #setGridBandsVisible(boolean)
	 */
	public boolean isGridBandsVisible() {
		return this.gridBandsVisible;
	}

	/**
	 * Sets the visibility of the grid bands and notifies registered listeners that
	 * the axis has been modified.
	 *
	 * @param flag the new setting.
	 *
	 * @see #isGridBandsVisible()
	 */
	public void setGridBandsVisible(boolean flag) {
		this.gridBandsVisible = flag;
		fireChangeEvent();
	}

	/**
	 * Returns the paint used to color the grid bands.
	 *
	 * @return The grid band paint (never <code>null</code>).
	 *
	 * @see #setDefaultGridBandPaint(Paint)
	 * @see #isGridBandsVisible()
	 */
	public Paint getDefaultGridBandPaint() {
		return this.defaultGridBandPaint;
	}

	/**
	 * Sets the grid band paint and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 *
	 * @param paint the paint (<code>null</code> not permitted).
	 *
	 * @see #getDefaultGridBandPaint()
	 */
	public void setDefaultGridBandPaint(Paint paint) {
		Args.nullNotPermitted(paint, "paint");
		this.defaultGridBandPaint = paint;
		fireChangeEvent();
	}
	
	/**
	 * Returns the paint used for the grid band of the section.
	 *
	 * @return The paint (never <code>null</code>).
	 *
	 * @see #setDefaultGridBandPaint(Paint)
	 * @see Section#setGridBandPaint(Paint)
	 */
	protected Paint getGridBandPaint(Section section) {
		Args.nullNotPermitted(section, "section");
		Paint paint = section.getGridBandPaint();
		if (paint == null) {
			paint = getDefaultGridBandPaint();
		}
		return paint;
	}

	/**
	 * Returns the paint used for alternate grid bands.
	 *
	 * @return The paint (never <code>null</code>).
	 *
	 * @see #setDefaultGridBandAlternatePaint(Paint)
	 * @see #isGridBandsVisible()
	 */
	public Paint getDefaultGridBandAlternatePaint() {
		return this.defaultGridBandAlternatePaint;
	}

	/**
	 * Sets the paint used for alternate grid bands and sends a
	 * {@link AxisChangeEvent} to all registered listeners.
	 *
	 * @param paint the paint (<code>null</code> not permitted).
	 *
	 * @see #getDefaultGridBandAlternatePaint()
	 * @see #setDefaultGridBandPaint(Paint)
	 */
	public void setDefaultGridBandAlternatePaint(Paint paint) {
		Args.nullNotPermitted(paint, "paint");
		this.defaultGridBandAlternatePaint = paint;
		fireChangeEvent();
	}

	/**
	 * Returns the paint used for the alternate grid band of the section.
	 *
	 * @return The paint (never <code>null</code>).
	 *
	 * @see #setDefaultGridBandAlternatePaint(Paint)
	 * @see Section#setGridBandAlternatePaint(Paint)
	 */
	protected Paint getGridBandAlternatePaint(Section section) {
		Args.nullNotPermitted(section, "section");
		Paint paint = section.getGridBandAlternatePaint();
		if (paint == null) {
			paint = getDefaultGridBandAlternatePaint();
		}
		return paint;
	}

	/**
	 * Returns the absolute gap value to use between sections.
	 * 
	 * @return the absolute gap value to use between sections.
	 * @see #setDefaultSectionGap(double)
	 */
	public double getDefaultSectionGap() {
		return defaultSectionGap;
	}

	/**
	 * Sets the absolute gap value to use between sections.
	 * 
	 * @param sectionGap the absolute gap value to use between sections.
	 */
	public void setDefaultSectionGap(double sectionGap) {
		this.defaultSectionGap = sectionGap;
	}

	/**
	 * Returns an unmodifiable {@link List} containing all {@link Section}s that are
	 * currently contained by this axis.
	 * 
	 * @return all {@link Section}s that are currently contained by this axis.
	 * @see #nextSection(String, double, double)
	 */
	public List<Section> getSections() {
		return Collections.unmodifiableList(sections);
	}

	/**
	 * Creates and adds a new {@link Section} to this axis with the specified
	 * <code>label</code>, {@link #DEFAULT_SECTION_LENGTH} and
	 * {@link #getDefaultSectionGap()}.
	 * 
	 * @param label the label for the section
	 * @return the created section
	 */
	public Section nextSection(String label) {
		return nextSection(label, DEFAULT_SECTION_LENGTH, defaultSectionGap);
	}

	/**
	 * Creates and adds a new {@link Section} to this axis with the specified
	 * <code>label, length</code> and {@link #getDefaultSectionGap()}.
	 * 
	 * @param label  the label for the section
	 * @param length the length of the section on this axis
	 * @return the created section
	 */
	public Section nextSection(String label, double length) {
		return nextSection(label, length, defaultSectionGap);
	}

	/**
	 * Creates and adds a new {@link Section} to this axis with the specified
	 * <code>label, length and gap</code>.
	 * 
	 * @param label  the label for the section
	 * @param length the length of the section on this axis
	 * @param gap    the gap between the section and the previous section on this
	 *               axis
	 * @return the created section
	 */
	public Section nextSection(String label, double length, double gap) {
		Args.requireGreaterThanZero(length, "length");
		Args.requireNonNegative(gap, "gap");
		double sectionLowerBound = 0;
		if (!sections.isEmpty()) {
			sectionLowerBound = sections.getLast().getRange().getUpperBound() + gap;
		}
		Section section = new Section(this, new Range(sectionLowerBound, sectionLowerBound + length), label);
		sections.add(section);
		return section;
	}
	
	/**
	 * Receives notification of a section change.
	 *
	 * @param section the section.
	 */
	protected void sectionChanged(Section section) {
		fireChangeEvent();
	}


	@Override
	public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
		Range range = getRange();
		double axisMin = range.getLowerBound();
		double axisMax = range.getUpperBound();

		double min = 0.0;
		double max = 0.0;
		if (RectangleEdge.isTopOrBottom(edge)) {
			min = area.getX();
			max = area.getMaxX();
		} else if (RectangleEdge.isLeftOrRight(edge)) {
			max = area.getMinY();
			min = area.getMaxY();
		}
		if (isInverted()) {
			return max - ((value - axisMin) / (axisMax - axisMin)) * (max - min);
		} else {
			return min + ((value - axisMin) / (axisMax - axisMin)) * (max - min);
		}
	}

	@Override
	public double java2DToValue(double java2dValue, Rectangle2D area, RectangleEdge edge) {
		Range range = getRange();
		double axisMin = range.getLowerBound();
		double axisMax = range.getUpperBound();

		double min = 0.0;
		double max = 0.0;
		if (RectangleEdge.isTopOrBottom(edge)) {
			min = area.getX();
			max = area.getMaxX();
		} else if (RectangleEdge.isLeftOrRight(edge)) {
			min = area.getMaxY();
			max = area.getY();
		}
		if (isInverted()) {
			return axisMax - (java2dValue - min) / (max - min) * (axisMax - axisMin);
		} else {
			return axisMin + (java2dValue - min) / (max - min) * (axisMax - axisMin);
		}
	}

	@Override
	public void configure() {
		if (isAutoRange()) {
			autoAdjustRange();
		}
	}

	@Override
	public Range calculateAutoRange(boolean adhereToMax) {
		// no plot, no data
		if (getPlot() instanceof ValueAxisPlot) {
			// ensure that all the sections are displayed
			double lower;
			double upper;
			if (sections.isEmpty()) {
				Range r = getDefaultAutoRange();
				lower = r.getLowerBound();
				upper = r.getUpperBound();
			} else {
				lower = sections.getFirst().getRange().getLowerBound();
				upper = sections.getLast().getRange().getUpperBound();
			}

			double fixedAutoRange = getFixedAutoRange();
			if (adhereToMax && fixedAutoRange > 0.0) {
				Range aligned = getAutoRangeAlign().align(new Range(lower, upper), fixedAutoRange);
				lower = aligned.getLowerBound();
				upper = aligned.getUpperBound();
			} else {
				double range = upper - lower;
				// ensure the auto-range is at least <minRange> in size...
				double minRange = getAutoRangeMinimumSize();
				if (range < minRange) {
					upper = (upper + lower + minRange) / 2;
					lower = (upper + lower - minRange) / 2;
				}

				// Add the margins
				upper += upper * getUpperMargin();
				lower -= lower * getLowerMargin();
			}

			return new Range(lower, upper);
		}
		return null;
	}

	@Override
	public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
			PlotRenderingInfo plotState) {
		// draw the tick marks and labels...
		AxisState state = drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);

		if (this.gridBandsVisible) {
			drawGridBands(g2, plotArea, dataArea, edge);
		}

		if (getAttributedLabel() != null) {
			state = drawAttributedLabel(getAttributedLabel(), g2, plotArea, dataArea, edge, state);

		} else {
			state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
		}
		createAndAddEntity(cursor, state, dataArea, edge, plotState);
		createTickLabelEntities(g2, cursor, state, dataArea, edge, plotState);

		return state;
	}

	/**
	 * Creates the {@link TickLabelEntity}s for this axis.
	 *
	 * @param g2        the graphics device ({@code null} not permitted).
	 * @param cursor    the initial cursor value.
	 * @param state     the axis state after completion of the drawing with a
	 *                  possibly updated cursor position.
	 * @param dataArea  the data area.
	 * @param edge      the edge ({@code null} not permitted).
	 * @param plotState the PlotRenderingInfo from which a reference to the entity
	 *                  collection can be obtained.
	 */
	protected void createTickLabelEntities(Graphics2D g2, double cursor, AxisState state, Rectangle2D dataArea,
			RectangleEdge edge, PlotRenderingInfo plotState) {
		if (plotState == null || plotState.getOwner() == null || plotState.getOwner().getEntityCollection() == null) {
			return; // no need to create entity if we can't save it anyways...
		}
		EntityCollection entityCollection = plotState.getOwner().getEntityCollection();
		for (Object tick : state.getTicks()) {
			if (tick instanceof SectionTick && ((SectionTick) tick).getTooltipText() != null) {
				Shape tickTextBounds = calculateTickTextBounds((SectionTick) tick, g2, cursor, dataArea, edge);
				entityCollection.add(new TickLabelEntity(tickTextBounds, ((SectionTick) tick).getTooltipText(), null));
			}
		}
	}

	/**
	 * Draws the grid bands. Alternate bands are colored using
	 * <CODE>gridBandPaint</CODE> (<CODE>DEFAULT_GRID_BAND_PAINT</CODE> by default).
	 *
	 * @param g2       the graphics target (<code>null</code> not permitted).
	 * @param plotArea the area within which the plot is drawn (<code>null</code>
	 *                 not permitted).
	 * @param dataArea the data area to which the axes are aligned
	 *                 (<code>null</code> not permitted).
	 * @param edge     the edge to which the axis is aligned (<code>null</code> not
	 *                 permitted).
	 */
	protected void drawGridBands(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge) {
		Shape savedClip = g2.getClip();
		g2.clip(dataArea);
		if (RectangleEdge.isTopOrBottom(edge)) {
			drawGridBandsHorizontal(g2, plotArea, dataArea, edge);
		} else if (RectangleEdge.isLeftOrRight(edge)) {
			drawGridBandsVertical(g2, plotArea, dataArea, edge);
		}
		g2.setClip(savedClip);
	}

	/**
	 * Draws the grid bands for the axis when it is at the top or bottom of the
	 * plot.
	 *
	 * @param g2       the graphics target (<code>null</code> not permitted).
	 * @param plotArea the area within which the plot is drawn (not used here).
	 * @param dataArea the area for the data (to which the axes are aligned,
	 *                 <code>null</code> not permitted).
	 * @param edge     the edge to which the axis is aligned (<code>null</code> not
	 *                 permitted).
	 */
	protected void drawGridBandsHorizontal(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea,
			RectangleEdge edge) {
		throw new UnsupportedOperationException("This axis does not support horizontal plotting yet!");
	}

	/**
	 * Draws the grid bands for an axis that is aligned to the left or right of the
	 * data area (that is, a vertical axis).
	 *
	 * @param g2                  the graphics target (<code>null</code> not
	 *                            permitted).
	 * @param plotArea            the area within which the plot is drawn (not used
	 *                            here).
	 * @param dataArea            the area for the data (to which the axes are
	 *                            aligned, <code>null</code> not permitted).
	 * @param firstGridBandIsDark True: the first grid band takes the color of
	 *                            <CODE>gridBandPaint</CODE>. False: the second grid
	 *                            band takes the color of
	 *                            <CODE>gridBandPaint</CODE>.
	 * @param ticks               a list of ticks (<code>null</code> not permitted).
	 */
	protected void drawGridBandsVertical(Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea,
			RectangleEdge edge) {

		boolean useAlternatePaint = true;
		double xx = dataArea.getX();
		double yMax, yMin;

		// gets the outline stroke width of the plot
		double outlineStrokeWidth = 1.0;
		Stroke outlineStroke = getPlot().getOutlineStroke();
		if (outlineStroke != null && outlineStroke instanceof BasicStroke) {
			outlineStrokeWidth = ((BasicStroke) outlineStroke).getLineWidth();
		}

		for (Section section : sections) {
			if (!getRange().intersects(section.getRange())) {
				continue;
			}

			yMin = valueToJava2D(section.getRange().getLowerBound(), dataArea, RectangleEdge.LEFT);
			yMax = valueToJava2D(section.getRange().getUpperBound(), dataArea, RectangleEdge.LEFT);

			g2.setPaint(useAlternatePaint ? getGridBandAlternatePaint(section) : getGridBandPaint(section));
			useAlternatePaint = !useAlternatePaint;

			Rectangle2D band = new Rectangle2D.Double(xx + outlineStrokeWidth, Math.min(yMin, yMax),
					dataArea.getMaxX() - xx - outlineStrokeWidth, Math.abs(yMax - yMin));
			g2.fill(band);
		}
	}
	
	@Override
	public List<ValueTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
		if (RectangleEdge.isTopOrBottom(edge)) {
			return refreshTicksHorizontal(g2, dataArea, edge);
		} else if (RectangleEdge.isLeftOrRight(edge)) {
			return refreshTicksVertical(g2, dataArea, edge);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results
	 * in the tick label list (ready for drawing). This method is called when the
	 * axis is at the top or bottom of the chart (so the axis is "horizontal").
	 *
	 * @param g2       the graphics device.
	 * @param dataArea the area in which the plot should be drawn.
	 * @param edge     the location of the axis.
	 *
	 * @return The ticks.
	 */
	protected List<ValueTick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		throw new UnsupportedOperationException("This axis does not support horizontal plotting yet!");
	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results
	 * in the tick label list (ready for drawing). This method is called when the
	 * axis is at the left or right of the chart (so the axis is "vertical").
	 *
	 * @param g2       the graphics device.
	 * @param dataArea the area in which the plot should be drawn.
	 * @param edge     the location of the axis.
	 *
	 * @return The ticks.
	 */
	protected List<ValueTick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);

		double cursor = 0.0;
		TextAnchor textAnchor;
		TextAnchor rotationAnchor;
		double angle;
		if (isVerticalTickLabels()) {
			if (edge == RectangleEdge.LEFT) {
				textAnchor = TextAnchor.BOTTOM_CENTER;
				rotationAnchor = TextAnchor.BOTTOM_CENTER;
				angle = -Math.PI / 2.0;
			} else {
				textAnchor = TextAnchor.BOTTOM_CENTER;
				rotationAnchor = TextAnchor.BOTTOM_CENTER;
				angle = Math.PI / 2.0;
			}
		} else {
			if (edge == RectangleEdge.LEFT) {
				textAnchor = TextAnchor.CENTER_RIGHT;
				rotationAnchor = TextAnchor.CENTER_RIGHT;
				angle = 0.0;
			} else {
				textAnchor = TextAnchor.CENTER_LEFT;
				rotationAnchor = TextAnchor.CENTER_LEFT;
				angle = 0.0;
			}
		}

		// Creating the ticks and check if labels do not overlap
		List<ValueTick> ticks = new ArrayList<>();
		Area ticksTextArea = new Area();

		for (Section section : sections) {
			if (!getRange().contains(section.getRange().getCentralValue())) {
				continue;
			}
			SectionTick sectionTick = createSectionTick(TickType.MINOR, section.getRange().getCentralValue(),
					section.getLabel(), textAnchor, rotationAnchor, angle);
			// Avoid to draw overlapping ticks labels
			Shape tickTextBounds = calculateTickTextBounds(sectionTick, g2, cursor, dataArea, edge);
			if (!intersect(ticksTextArea, tickTextBounds)) {
				ticksTextArea.add(new Area(tickTextBounds));
				ticks.add(sectionTick);
			}
		}

		for (Section section : sections) {
			ValueAxis gridBandAxis = section.getGridBandAxis();
			if (null == gridBandAxis) {
				continue;
			}

			Range gridBandRange = gridBandAxis.getRange();
			Range sectionRange = section.getRange();
			double yLower = valueToJava2D(sectionRange.getLowerBound(), dataArea, edge);
			double yUpper = valueToJava2D(sectionRange.getUpperBound(), dataArea, edge);
			// Calculate ticks for the scaled data area
			Rectangle2D sectionDataArea = new Rectangle2D.Double(dataArea.getX(), yLower, dataArea.getWidth(),
					Math.abs(yUpper - yLower));

			List<?> sectionTicks = gridBandAxis.refreshTicks(g2, new AxisState(cursor), sectionDataArea, edge);
			for (Object tick : sectionTicks) {
				ValueTick sectionTick = (ValueTick) tick;
				// Scale the ticks back to the original data area
				double scaledValue = AxisUtils.scaleValue(sectionTick.getValue(), gridBandRange, sectionRange);
				SectionTick scaledTick = createSectionTick(sectionTick.getTickType(), scaledValue,
						sectionTick.getText(), sectionTick.getTextAnchor(), sectionTick.getRotationAnchor(),
						sectionTick.getAngle());

				// Avoid to draw overlapping ticks labels
				Shape tickTextBounds = calculateTickTextBounds(scaledTick, g2, cursor, dataArea, edge);
				if (intersect(ticksTextArea, tickTextBounds)) {
					// Just add the tick, but without label
					ticks.add(createSectionTick(sectionTick.getTickType(), scaledValue, null,
							sectionTick.getTextAnchor(), sectionTick.getRotationAnchor(), sectionTick.getAngle()));
				} else {
					ticksTextArea.add(new Area(tickTextBounds));
					ticks.add(scaledTick);
				}
			}
		}
		return ticks;
	}
	
	private SectionTick createSectionTick(TickType tickType, double value, String label, TextAnchor textAnchor,
			TextAnchor rotationAnchor, double angle) {
		final String tickLabel = createTickLabel(label);
		final SectionTick sectionTick = new SectionTick(tickType, value, tickLabel, textAnchor, rotationAnchor, angle);
		
		switch (getTooltipMode()) {
			case MAX_EXCEEDED:
				if (Objects.equals(label, tickLabel)) {
					// No need to add tooltip
					break;
				}
			case ALWAYS:
				sectionTick.setTooltipText(label);
				break;
			default:
				// Nothing to do
				break;
		}
		
		return sectionTick;
	}

	private Shape calculateTickTextBounds(ValueTick tick, Graphics2D g2, double cursor, Rectangle2D dataArea,
			RectangleEdge edge) {
		String tickText = tick.getText();
		if (null == tickText || tickText.isEmpty()) {
			return new Area();
		}

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);

		float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);
		return TextUtils.calculateRotatedStringBounds(tickText, g2, anchorPoint[0], anchorPoint[1],
				tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor());
	}

	private boolean intersect(Shape a, Shape b) {
		final Area areaA = new Area(a);
		final Area areaB = new Area(b);
		areaA.intersect(areaB);
		return !areaA.isEmpty();
	}
}
