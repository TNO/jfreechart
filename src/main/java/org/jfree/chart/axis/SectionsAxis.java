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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.TickLabelEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.Args;
import org.jfree.data.Range;

/**
 * An {@link Axis} that stacks multiple axes into a single axis, creating
 * sections. Use this axis to combine e.g. {@link NumberAxis} and
 * {@link SymbolAxis} in a single axis.
 */
public class SectionsAxis extends ValueAxis {
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

	private final SortedSet<Section> sections = new TreeSet<>();

	private double sectionGap;

	private int tickLabelMaxLength;

	private TooltipMode tooltipMode;

	/** Flag that indicates whether or not grid bands are visible. */
	private boolean gridBandsVisible;

	/** The paint used to color the grid bands (if the bands are visible). */
	private transient Paint gridBandPaint;

	/** The paint used to fill the alternate grid bands. */
	private transient Paint gridBandAlternatePaint;

	/**
	 * Default constructor.
	 */
	public SectionsAxis() {
		this(null);
	}

	/**
	 * Constructs a sections axis, using default values where necessary.
	 *
	 * @param label the axis label ({@code null} permitted).
	 */
	public SectionsAxis(String label) {
		super(label, new StandardTickUnitSource());
		setLowerMargin(DEFAULT_LOWER_MARGIN);
		setUpperMargin(DEFAULT_UPPER_MARGIN);
		this.gridBandsVisible = true;
		this.gridBandPaint = DEFAULT_GRID_BAND_PAINT;
		this.gridBandAlternatePaint = DEFAULT_GRID_BAND_ALTERNATE_PAINT;
		this.sectionGap = DEFAULT_SECTION_GAP;
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
	 * {@link SectionsAxis}
	 * 
	 * @param sectionLabel the label to format
	 * @return a tick label that adheres to the restrictions of this
	 *         {@link SectionsAxis}
	 */
	protected String createTickLabel(String sectionLabel) {
		if (null == sectionLabel || tickLabelMaxLength < 0 || sectionLabel.length() <= tickLabelMaxLength) {
			return sectionLabel;
		}
		return sectionLabel.substring(0, tickLabelMaxLength) + "...";
	}

	public TooltipMode getTooltipMode() {
		return tooltipMode;
	}

	public void setTooltipMode(TooltipMode tooltipMode) {
		Args.nullNotPermitted(tooltipMode, "TooltipMode");
		this.tooltipMode = tooltipMode;
	}

	public Collection<Section> getSections() {
		return Collections.unmodifiableCollection(sections);
	}

	public double getSectionGap() {
		return sectionGap;
	}

	public void setSectionGap(double sectionGap) {
		this.sectionGap = sectionGap;
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
	 * @see #setGridBandPaint(Paint)
	 * @see #isGridBandsVisible()
	 */
	public Paint getGridBandPaint() {
		return this.gridBandPaint;
	}

	/**
	 * Sets the grid band paint and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 *
	 * @param paint the paint (<code>null</code> not permitted).
	 *
	 * @see #getGridBandPaint()
	 */
	public void setGridBandPaint(Paint paint) {
		Args.nullNotPermitted(paint, "paint");
		this.gridBandPaint = paint;
		fireChangeEvent();
	}

	/**
	 * Returns the paint used for alternate grid bands.
	 *
	 * @return The paint (never <code>null</code>).
	 *
	 * @see #setGridBandAlternatePaint(Paint)
	 * @see #getGridBandPaint()
	 */
	public Paint getGridBandAlternatePaint() {
		return this.gridBandAlternatePaint;
	}

	/**
	 * Sets the paint used for alternate grid bands and sends a
	 * {@link AxisChangeEvent} to all registered listeners.
	 *
	 * @param paint the paint (<code>null</code> not permitted).
	 *
	 * @see #getGridBandAlternatePaint()
	 * @see #setGridBandPaint(Paint)
	 */
	public void setGridBandAlternatePaint(Paint paint) {
		Args.nullNotPermitted(paint, "paint");
		this.gridBandAlternatePaint = paint;
		fireChangeEvent();
	}

	public Section nextSection(String label) {
		return nextSection(label, DEFAULT_SECTION_LENGTH, sectionGap);
	}

	public Section nextSection(String label, double length) {
		return nextSection(label, length, sectionGap);
	}

	public Section nextSection(String label, double length, double gap) {
		if (length <= 0) {
			throw new IllegalArgumentException("Section length should be greather than zero.");
		}
		if (gap < 0) {
			throw new IllegalArgumentException("Section gap should be greather than or equal to zero.");
		}
		double sectionLowerBound = 0;
		if (!sections.isEmpty()) {
			sectionLowerBound = sections.last().getUpperBound() + gap;
		}
		Section section = new Section(new Range(sectionLowerBound, sectionLowerBound + length), label);
		sections.add(section);
		return section;
	}

	protected List<Section> getSectionsInRange() {
		return sections.stream().filter(s -> getRange().intersects(s.getRange())).collect(Collectors.toList());
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
				lower = sections.first().getLowerBound();
				upper = sections.last().getUpperBound();
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
		createTickLabelEntities(g2, cursor, dataArea, edge, state, plotState);

		return state;
	}

	protected void createTickLabelEntities(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge,
			AxisState state, PlotRenderingInfo plotState) {
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

		for (Section section : getSectionsInRange()) {
			yMin = valueToJava2D(section.getRange().getLowerBound(), dataArea, RectangleEdge.LEFT);
			yMax = valueToJava2D(section.getRange().getUpperBound(), dataArea, RectangleEdge.LEFT);

			if (useAlternatePaint) {
				if (null != section.getGridBandAlternatePaint()) {
					g2.setPaint(section.getGridBandAlternatePaint());
				} else {
					g2.setPaint(this.gridBandAlternatePaint);
				}
			} else {
				if (null != section.getGridBandPaint()) {
					g2.setPaint(section.getGridBandPaint());
				} else {
					g2.setPaint(this.gridBandPaint);
				}
			}
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

	protected List<ValueTick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		throw new UnsupportedOperationException("This axis does not support horizontal plotting yet!");
	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results
	 * in the tick label list (ready for drawing).
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

		sections.stream().filter(s -> getRange().contains(s.getRange().getCentralValue())).forEach(s -> {
			String sectionLabel = s.getLabel();
			String tickLabel = createTickLabel(sectionLabel);
			SectionTick sectionTick = new SectionTick(TickType.MINOR, s.getRange().getCentralValue(), tickLabel,
					textAnchor, rotationAnchor, angle);
			switch (getTooltipMode()) {
			case MAX_EXCEEDED:
				if (Objects.equals(sectionLabel, tickLabel)) {
					// No need to add tooltip
					break;
				}
			case ALWAYS:
				sectionTick.setTooltipText(sectionLabel);
				break;
			default:
				// Nothing to do
				break;
			}

			// Avoid to draw overlapping ticks labels
			Shape tickTextBounds = calculateTickTextBounds(sectionTick, g2, cursor, dataArea, edge);
			if (!intersect(ticksTextArea, tickTextBounds)) {
				ticksTextArea.add(new Area(tickTextBounds));
				ticks.add(sectionTick);
			}
		});

		sections.stream().filter(s -> s.axis != null).forEach(s -> {
			Range sectionRange = s.getRange();
			double yLower = valueToJava2D(sectionRange.getLowerBound(), dataArea, edge);
			double yUpper = valueToJava2D(sectionRange.getUpperBound(), dataArea, edge);
			// Calculate ticks for the scaled data area
			Rectangle2D sectionDataArea = new Rectangle2D.Double(dataArea.getX(), yLower, dataArea.getWidth(),
					Math.abs(yUpper - yLower));

			List<?> sectionTicks = s.axis.refreshTicks(g2, new AxisState(cursor), sectionDataArea, edge);
			for (Object tick : sectionTicks) {
				if (NumberTick.class.equals(tick.getClass())) {
					NumberTick nt = (NumberTick) tick;
					// Scale the ticks back to the original data area
					double scaledValue = AxisUtils.scaleValue(nt.getValue(), s.axis.getRange(), sectionRange);
					NumberTick scaledTick = new NumberTick(nt.getTickType(), scaledValue, nt.getText(),
							nt.getTextAnchor(), nt.getRotationAnchor(), nt.getAngle());

					// Avoid to draw overlapping ticks labels
					Shape tickTextBounds = calculateTickTextBounds(scaledTick, g2, cursor, dataArea, edge);
					if (intersect(ticksTextArea, tickTextBounds)) {
						// Just add the tick, but without label
						ticks.add(new NumberTick(nt.getTickType(), scaledValue, null, nt.getTextAnchor(),
								nt.getRotationAnchor(), nt.getAngle()));
					} else {
						ticksTextArea.add(new Area(tickTextBounds));
						ticks.add(scaledTick);
					}
				} else {
					System.err.println("Tick type not supported for section axis: " + tick);
				}
				continue;
			}
		});
		return ticks;
	}

	private boolean intersect(Shape a, Shape b) {
		final Area areaA = new Area(a);
		final Area areaB = new Area(b);
		areaA.intersect(areaB);
		return !areaA.isEmpty();
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

	@Override
	protected void fireChangeEvent() {
		// Avoiding accessibility exceptions
		super.fireChangeEvent();
	}

	public class Section implements Comparable<Section>, Serializable, AxisChangeListener {
		private static final long serialVersionUID = 1028149280898345210L;

		private final Range range;

		private String label;
		private ValueAxis axis;
		/** The paint used to color the grid bands (if the bands are visible). */
		private transient Paint gridBandPaint;
		/** The paint used to fill the alternate grid bands. */
		private transient Paint gridBandAlternatePaint;

		protected Section(Range range, String label) {
			this.range = range;
			this.label = label;
		}

		public Range getRange() {
			return range;
		}

		/** @see #getRange() */
		public double getLowerBound() {
			return range.getLowerBound();
		}

		/** @see #getRange() */
		public double getUpperBound() {
			return range.getUpperBound();
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
			fireChangeEvent();
		}

		public Paint getGridBandPaint() {
			return gridBandPaint;
		}

		public void setGridBandPaint(Paint paint) {
			this.gridBandPaint = paint;
			fireChangeEvent();
		}

		public void setGridBandAlternatePaint(Paint paint) {
			this.gridBandAlternatePaint = paint;
			fireChangeEvent();
		}

		public Paint getGridBandAlternatePaint() {
			return null == gridBandAlternatePaint ? gridBandPaint : gridBandAlternatePaint;
		}

		public Range getGridBandRange() {
			return axis.getRange();
		}

		public void setGridBandNumberRange(Range gridBandRange, boolean isInteger) {
			NumberAxis gridBandAxis = new NumberAxis(this.label);
			if (isInteger) {
				gridBandAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			}
			gridBandAxis.setMinorTickCount(1);
			setGridBandAxis(gridBandRange, gridBandAxis);
		}

		protected void setGridBandAxis(Range gridBandRange, ValueAxis gridBandAxis) {
			if (null != this.axis) {
				this.axis.removeChangeListener(Section.this);
			}
			this.axis = gridBandAxis;
			if (null != this.axis) {
				this.axis.addChangeListener(this);
				this.axis.setRange(gridBandRange, true, true);
			}
		}

		@Override
		public void axisChanged(AxisChangeEvent event) {
			fireChangeEvent();
		}

		@Override
		public int compareTo(Section o) {
			return Double.compare(range.getCentralValue(), o.range.getCentralValue());
		}
	}

	protected static class SectionTick extends ValueTick {
		private static final long serialVersionUID = 4133486505758570577L;

		private String tooltipText;

		public SectionTick(TickType tickType, double value, String label, TextAnchor textAnchor,
				TextAnchor rotationAnchor, double angle) {
			super(tickType, value, label, textAnchor, rotationAnchor, angle);
		}

		public void setTooltipText(String tooltipText) {
			this.tooltipText = tooltipText;
		}

		public String getTooltipText() {
			return tooltipText;
		}
	}
}
