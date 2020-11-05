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
 * Section.java
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

import java.awt.Paint;
import java.io.Serializable;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.util.Args;
import org.jfree.data.Range;
import org.jfree.data.xy.XYScaledSeries;

/**
 * A section is a sub-{@link Range} on a {@link SectionAxis}. A section can have
 * its own {@link ValueAxis} to scale its sub-range into the axis range.
 * 
 * @see SectionAxis
 * @see {@link XYScaledSeries}
 */
public class Section implements Comparable<Section>, Serializable, AxisChangeListener {
	private static final long serialVersionUID = 1028149280898345210L;

	private final SectionAxis axis;

	private final Range range;

	private String label;

	private ValueAxis gridBandAxis;

	/** The paint used to color the grid bands (if the bands are visible). */
	private transient Paint gridBandPaint;

	/** The paint used to fill the alternate grid bands. */
	private transient Paint gridBandAlternatePaint;

	/**
	 * Constructs a section.
	 * 
	 * @param axis  the section axis that this section belongs to ({@code null} not
	 *              permitted)
	 * @param range the sub-range on the {@code axis} for this section ({@code null}
	 *              not permitted)
	 * @param label the section label ({@code null} permitted)
	 */
	public Section(SectionAxis axis, Range range, String label) {
		Args.nullNotPermitted(axis, "axis");
		Args.nullNotPermitted(range, "range");
		this.axis = axis;
		this.range = range;
		this.label = label;
	}

	/**
	 * Returns the section axis that this section belongs to.
	 * 
	 * @return the section axis that this section belongs to (never {@code null}).
	 */
	public SectionAxis getAxis() {
		return axis;
	}

	/**
	 * Returns the sub-range on the section axis for this section
	 * 
	 * @return the sub-range on the section axis for this section (never
	 *         {@code null})
	 */
	public Range getRange() {
		return range;
	}

	/**
	 * Returns the section label
	 * 
	 * @return the section label (possibly {@code null})
	 * @see #setLabel(String)
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label for the section.
	 * 
	 * @param label the section label ({@code null} permitted)
	 */
	public void setLabel(String label) {
		this.label = label;
		fireChangeEvent();
	}

	/**
	 * Returns the paint for the section grid band.
	 * 
	 * @return the section grid band paint (possibly {@code null})
	 * @see #setGridBandPaint(Paint)
	 */
	public Paint getGridBandPaint() {
		return gridBandPaint;
	}

	/**
	 * Sets the paint for the section grid band.
	 * 
	 * @param paint the section grid band paint ({@code null} permitted)
	 * @see SectionAxis#setBaseGridBandPaint(Paint)
	 */
	public void setGridBandPaint(Paint paint) {
		this.gridBandPaint = paint;
		fireChangeEvent();
	}

	/**
	 * Returns the alternate paint for the section grid band. If not set, returns
	 * the section {@link #getGridBandPaint()}.
	 * 
	 * @return the section grid band alternate paint (possibly {@code null})
	 * @see #setGridBandAlternatePaint(Paint)
	 */
	public Paint getGridBandAlternatePaint() {
		return null == this.gridBandAlternatePaint ? this.gridBandPaint : this.gridBandAlternatePaint;
	}

	/**
	 * Sets the alternate paint for the section grid band.
	 * 
	 * @param paint the section grid band alternate paint ({@code null} permitted)
	 * @see SectionAxis#setBaseGridBandAlternatePaint(Paint)
	 */
	public void setGridBandAlternatePaint(Paint paint) {
		this.gridBandAlternatePaint = paint;
		fireChangeEvent();
	}

	/**
	 * A convenience method that sets a new {@link NumberAxis} with a
	 * <code>gridBandRange</code> and optionally configured to show integer ticks
	 * only.
	 * 
	 * @param gridBandRange the range to set on this grid band axis
	 * @param isInteger     if <code>treu</code> the grid band axis show integer
	 *                      ticks only
	 * @see #setGridBandAxis(ValueAxis)
	 * @see NumberAxis#createIntegerTickUnits()
	 * @see XYScaledSeries
	 */
	public void setGridBandNumberRange(Range gridBandRange, boolean isInteger) {
		NumberAxis axis = new NumberAxis(this.label);
		if (isInteger) {
			axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		axis.setMinorTickCount(1);
		axis.setRange(range, true, false);
		setGridBandAxis(axis);
	}

	/**
	 * Returns the {@link ValueAxis} to be rendered on the section of the
	 * {@link SectionAxis}, {@code null} if not applicable.
	 * 
	 * @return the section grid band axis (possibly {@code null})
	 * @see #setGridBandAxis(ValueAxis)
	 */
	public ValueAxis getGridBandAxis() {
		return this.gridBandAxis;
	}

	/**
	 * Set the {@link ValueAxis} to be rendered on the section of the
	 * {@link SectionAxis}, {@code null} if not applicable.
	 * 
	 * @param axis the grid band axis ({@code null} permitted)
	 */
	public void setGridBandAxis(ValueAxis axis) {
		if (null != this.gridBandAxis) {
			this.gridBandAxis.removeChangeListener(this);
		}
		this.gridBandAxis = axis;
		if (null != this.gridBandAxis) {
			this.gridBandAxis.addChangeListener(this);
		}
		fireChangeEvent();
	}

	@Override
	public void axisChanged(AxisChangeEvent event) {
		fireChangeEvent();
	}

	/**
	 * Sends an {@link AxisChangeEvent} to all registered listeners of the
	 * {@link SectionAxis}.
	 */
	protected void fireChangeEvent() {
		axis.sectionChanged(this);
	}

	@Override
	public int compareTo(Section o) {
		return Double.compare(getRange().getCentralValue(), o.getRange().getCentralValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Section other = (Section) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equals(other.axis))
			return false;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return label;
	}
}