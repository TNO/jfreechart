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
 * SectionTick.java
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

import org.jfree.chart.ui.TextAnchor;

/**
 * A labeled tick with the option for tooltip text.
 */
public class SectionTick extends ValueTick {
	private static final long serialVersionUID = 4133486505758570577L;

	private String tooltipText;

	/**
	 * Creates a new tick.
	 *
	 * @param tickType       the tick type.
	 * @param value          the value.
	 * @param label          the label.
	 * @param textAnchor     the part of the label that is aligned with the anchor
	 *                       point.
	 * @param rotationAnchor defines the rotation point relative to the text.
	 * @param angle          the rotation angle (in radians).
	 */
	public SectionTick(TickType tickType, double value, String label, TextAnchor textAnchor, TextAnchor rotationAnchor,
			double angle) {
		super(tickType, value, label, textAnchor, rotationAnchor, angle);
	}

	/**
	 * Returns the tool tip text for the tick. This will be displayed in a
	 * {@link org.jfree.chart.ChartPanel} when the mouse pointer hovers over the
	 * tick.
	 *
	 * @return The tool tip text (possibly {@code null}).
	 *
	 * @see #setToolTipText(String)
	 */
	public String getTooltipText() {
		return tooltipText;
	}

	/**
	 * Sets the tool tip text for the tick.
	 *
	 * @param text the tool tip text ({@code null} permitted).
	 *
	 * @see #getToolTipText()
	 */
	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}
}