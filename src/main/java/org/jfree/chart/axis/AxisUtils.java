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

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.stream.Collectors;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

/**
 * Axis utilities.
 */
public final class AxisUtils {
	public static final FontRenderContext DEFAULT_FONT_RENDER_CONTEXT = new FontRenderContext(null, true, false);

	/**
	 * Private constructor prevents object creation.
	 */
	private AxisUtils() {
		// Empty
	}

	/**
	 * Estimates the required dimension of a {@link SymbolAxis}, based on its
	 * {@link SymbolAxis#getSymbols()} and applies it to the axis. Uses the
	 * {@link #DEFAULT_FONT_RENDER_CONTEXT} to estimate.
	 * 
	 * @param axis the axis to apply the fixed dimension
	 * @param edge the edge of the {@code axis} on the plot, see
	 *             {@link XYPlot#getDomainAxisEdge(int)} or
	 *             {@link XYPlot#getRangeAxisEdge(int)}
	 * @see ValueAxis#setFixedDimension(double)
	 */
	public static void calculateAndApplyFixedDimension(SymbolAxis axis, RectangleEdge edge) {
		calculateAndApplyFixedDimension(axis, edge, DEFAULT_FONT_RENDER_CONTEXT);
	}

	/**
	 * Estimates the required dimension of a {@link SymbolAxis}, based on its
	 * {@link SymbolAxis#getSymbols()} and applies it to the axis. Uses the
	 * {@code frc} to estimate.
	 * 
	 * @param axis the axis to apply the fixed dimension
	 * @param edge the edge of the {@code axis} on the plot, see
	 *             {@link XYPlot#getDomainAxisEdge(int)} or
	 *             {@link XYPlot#getRangeAxisEdge(int)}
	 * @param frc  the font render context to use for the estimation
	 * @see ValueAxis#setFixedDimension(double)
	 */
	public static void calculateAndApplyFixedDimension(SymbolAxis axis, RectangleEdge edge, FontRenderContext frc) {
		double fixedDimension = 0;
		for (String symbol : axis.getSymbols()) {
			if (null == symbol)
				continue;
			Rectangle2D symbolBounds = axis.getTickLabelFont().getStringBounds(symbol, frc);
			fixedDimension = Math.max(fixedDimension,
					RectangleEdge.isLeftOrRight(edge) ? symbolBounds.getWidth() : symbolBounds.getHeight());
		}
		if (null != axis.getLabel()) {
			Rectangle2D labelBounds = axis.getLabelInsets()
					.createOutsetRectangle(axis.getLabelFont().getStringBounds(axis.getLabel(), frc));
			fixedDimension += RectangleEdge.isLeftOrRight(edge) ? labelBounds.getHeight() : labelBounds.getWidth();
		}
		axis.setFixedDimension(fixedDimension);
	}

	/**
	 * Estimates the required dimension of a {@link SectionAxis}, based on its
	 * section labels and applies it to the axis. Uses the
	 * {@link #DEFAULT_FONT_RENDER_CONTEXT} to estimate.
	 * 
	 * @param axis the axis to apply the fixed dimension
	 * @param edge the edge of the {@code axis} on the plot, see
	 *             {@link XYPlot#getDomainAxisEdge(int)} or
	 *             {@link XYPlot#getRangeAxisEdge(int)}
	 * @see Section#getLabel()
	 * @see ValueAxis#setFixedDimension(double)
	 */
	public static void calculateAndApplyFixedDimension(SectionAxis axis, RectangleEdge edge) {
		calculateAndApplyFixedDimension(axis, edge, DEFAULT_FONT_RENDER_CONTEXT);
	}

	/**
	 * Estimates the required dimension of a {@link SectionAxis}, based on its
	 * section labels and applies it to the axis. Uses the {@code frc} to estimate.
	 * 
	 * @param axis the axis to apply the fixed dimension
	 * @param edge the edge of the {@code axis} on the plot, see
	 *             {@link XYPlot#getDomainAxisEdge(int)} or
	 *             {@link XYPlot#getRangeAxisEdge(int)}
	 * @param frc  the font render context to use for the estimation
	 * @see Section#getLabel()
	 * @see ValueAxis#setFixedDimension(double)
	 */
	public static void calculateAndApplyFixedDimension(SectionAxis axis, RectangleEdge edge, FontRenderContext frc) {
		double fixedDimension = 0;
		for (String sectionLabel : axis.getSections().stream().map(Section::getLabel).collect(Collectors.toList())) {
			if (null == sectionLabel)
				continue;
			String tickLabel = axis.createTickLabel(sectionLabel);
			Rectangle2D symbolBounds = axis.getTickLabelFont().getStringBounds(tickLabel, frc);
			symbolBounds = axis.getTickLabelInsets().createOutsetRectangle(symbolBounds);
			fixedDimension = Math.max(fixedDimension,
					RectangleEdge.isLeftOrRight(edge) ? symbolBounds.getWidth() : symbolBounds.getHeight());
		}
		if (null != axis.getLabel()) {
			Rectangle2D labelBounds = axis.getLabelInsets()
					.createOutsetRectangle(axis.getLabelFont().getStringBounds(axis.getLabel(), frc));
			fixedDimension += RectangleEdge.isLeftOrRight(edge) ? labelBounds.getHeight() : labelBounds.getWidth();
		}
		axis.setFixedDimension(fixedDimension);
	}

	/**
	 * Calculates the number of visible ticks.
	 *
	 * @return The number of visible ticks on the axis.
	 */
	public static int calculateVisibleTickCount(TickUnit unit, Range range) {
		return (int) (Math.floor(range.getUpperBound() / unit.getSize())
				- Math.ceil(range.getLowerBound() / unit.getSize()) + 1);
	}

	/**
	 * Linear scale of a {@code value} within a {@code source} range to the
	 * corresponding value in the {@code target} range.
	 * 
	 * @param value  the value to scale
	 * @param source The range containing {@code value}
	 * @param target The range for which the {@code return} value is calculated.
	 * @return the scaled value
	 * @throws IllegalArgumentException if {@code value} is not contained by
	 *                                  {@code source}, see
	 *                                  {@link Range#contains(double)}
	 */
	public static double scaleValue(double value, Range source, Range target) {
		if (!source.contains(value)) {
			throw new IllegalArgumentException("Value should lie within source range");
		}
		return (((value - source.getLowerBound()) / source.getLength()) * target.getLength()) + target.getLowerBound();
	}
}
