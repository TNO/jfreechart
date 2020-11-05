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
 * XYScaledSeries.java
 * --------------
 * (C) Copyright 2000-2020, ESI (TNO) and Contributors.
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 *
 * Changes
 * -------
 * 
 */
package org.jfree.data.xy;

import org.jfree.chart.axis.Section;
import org.jfree.chart.axis.SectionAxis;
import org.jfree.data.Range;

/**
 * A special {@link XYSeries} that allows to scale its values to fit a certain
 * range. This series can be used to bound values to a specific {@link Section}
 * on a {@link SectionAxis}.
 *
 * @param <K> the key type
 * @see SectionAxis
 */
public class XYScaledSeries<K extends Comparable<K>> extends XYSeries<K> {
	private static final long serialVersionUID = 5321740573688656080L;
	private final Range scaledXRange;
	private final Range scaledYRange;

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by x-value, and duplicate x-values will be
	 * allowed (these defaults can be modified with another constructor).
	 *
	 * @param key    the series key ({@code null} not permitted).
	 * @param XScale the scale to use for the X-axis, {@code null} if scaling is not
	 *               required for the X-axis.
	 * @param XScale the scale to use for the Y-axis, {@code null} if scaling is not
	 *               required for the Y-axis.
	 */
	public XYScaledSeries(K key, Range XScale, Range YScale) {
		this(key, XScale, YScale, true, true);
	}

	/**
	 * Constructs a new empty series, with the auto-sort flag set as requested, and
	 * duplicate values allowed.
	 *
	 * @param key      the series key ({@code null} not permitted).
	 * @param XScale   the scale to use for the X-axis, {@code null} if scaling is
	 *                 not required for the X-axis.
	 * @param XScale   the scale to use for the Y-axis, {@code null} if scaling is
	 *                 not required for the Y-axis.
	 * @param autoSort a flag that controls whether or not the items in the series
	 *                 are sorted.
	 */
	public XYScaledSeries(K key, Range XScale, Range YScale, boolean autoSort) {
		this(key, XScale, YScale, autoSort, true);
	}

	/**
	 * Constructs a new xy-series that contains no data. You can specify whether or
	 * not duplicate x-values are allowed for the series.
	 *
	 * @param key                   the series key ({@code null} not permitted).
	 * @param XScale                the scale to use for the X-axis, {@code null} if
	 *                              scaling is not required for the X-axis.
	 * @param XScale                the scale to use for the Y-axis, {@code null} if
	 *                              scaling is not required for the Y-axis.
	 * @param autoSort              a flag that controls whether or not the items in
	 *                              the series are sorted.
	 * @param allowDuplicateXValues a flag that controls whether duplicate x-values
	 *                              are allowed.
	 */
	public XYScaledSeries(K key, Range XScale, Range YScale, boolean autoSort, boolean allowDuplicateXValues) {
		super(key, autoSort, allowDuplicateXValues);
		this.scaledXRange = XScale;
		this.scaledYRange = YScale;
	}

	@Override
	public double getMinX() {
		return null == scaledXRange ? super.getMinX() : scaledXRange.getLowerBound();
	}

	@Override
	public double getMinY() {
		return null == scaledYRange ? super.getMinY() : scaledYRange.getLowerBound();
	}

	@Override
	public double getMaxX() {
		return null == scaledXRange ? super.getMaxX() : scaledXRange.getUpperBound();
	}

	@Override
	public double getMaxY() {
		return null == scaledYRange ? super.getMaxY() : scaledYRange.getUpperBound();
	}

	@Override
	public Number getX(int index) {
		Number original = super.getX(index);
		if (null == original || null == scaledXRange)
			return original;

		double originalPercentage = (original.doubleValue() - super.getMinX()) / (super.getMaxX() - super.getMinX());
		return scaledXRange.getLowerBound() + (originalPercentage * scaledXRange.getLength());
	}

	@Override
	public Number getY(int index) {
		Number original = super.getY(index);
		if (null == original || null == scaledYRange)
			return original;

		double originalPercentage = (original.doubleValue() - super.getMinY()) / (super.getMaxY() - super.getMinY());
		return scaledYRange.getLowerBound() + (originalPercentage * scaledYRange.getLength());
	}

	/**
	 * Returns the smallest unscaled (a.k.a. original) x-value in the series,
	 * ignoring any Double.NaN values. This method returns Double.NaN if there is no
	 * smallest x-value (for example, when the series is empty).
	 *
	 * @return The smallest x-value.
	 *
	 * @see #getMaxX()
	 *
	 * @since 1.0.13
	 */
	public double getUnscaledMinX() {
		return super.getMinX();
	}

	/**
	 * Returns the smallest unscaled (a.k.a. original) y-value in the series,
	 * ignoring any null and Double.NaN values. This method returns Double.NaN if
	 * there is no smallest y-value (for example, when the series is empty).
	 *
	 * @return The smallest y-value.
	 *
	 * @see #getMaxY()
	 *
	 * @since 1.0.13
	 */
	public double getUnscaledMinY() {
		return super.getMinY();
	}

	/**
	 * Returns the largest unscaled (a.k.a. original) x-value in the series,
	 * ignoring any Double.NaN values. This method returns Double.NaN if there is no
	 * largest x-value (for example, when the series is empty).
	 *
	 * @return The largest x-value.
	 *
	 * @see #getMinX()
	 *
	 * @since 1.0.13
	 */
	public double getUnscaledMaxX() {
		return super.getMaxX();
	}

	/**
	 * Returns the largest unscaled (a.k.a. original) y-value in the series,
	 * ignoring any Double.NaN values. This method returns Double.NaN if there is no
	 * largest y-value (for example, when the series is empty).
	 *
	 * @return The largest y-value.
	 *
	 * @see #getMinY()
	 *
	 * @since 1.0.13
	 */
	public double getUnscaledMaxY() {
		return super.getMaxY();
	}

	/**
	 * Returns the unscaled (a.k.a. original) x-value at the specified index.
	 *
	 * @param index the index (zero-based).
	 *
	 * @return The x-value (never {@code null}).
	 */
	public Number getUnscaledX(int index) {
		return super.getX(index);
	}

	/**
	 * Returns the unscaled (a.k.a. original) y-value at the specified index.
	 *
	 * @param index the index (zero-based).
	 *
	 * @return The y-value (possibly {@code null}).
	 */
	public Number getUnscaledY(int index) {
		return super.getY(index);
	}
}
