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
 * XYEdgeDataItem.java
 * -----------------
 * (C) Copyright 2020, by ESI (TNO).
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 * Contributor(s):   -;
 *
 */

package org.jfree.data.xy;

import java.io.Serializable;

import org.jfree.chart.util.Args;

public class XYEdgeDataItem implements Serializable {
	private static final long serialVersionUID = -7116745427210808035L;

	private final Number x0;
	private final Number y0;
	private final Number x1;
	private final Number y1;

	public XYEdgeDataItem(Number x0, Number y0, Number x1, Number y1) {
		Args.nullNotPermitted(x0, "x0");
		Args.nullNotPermitted(x1, "x1");
		Args.nullNotPermitted(y0, "y0");
		Args.nullNotPermitted(y1, "y1");
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
	}

	public boolean isNaN() {
		return Double.isNaN(getX0Value()) || Double.isNaN(getX1Value()) || Double.isNaN(getY0Value())
				|| Double.isNaN(getY1Value());
	}

	public Number getX0() {
		return x0;
	}

	public Number getY0() {
		return y0;
	}

	public Number getX1() {
		return x1;
	}

	public Number getY1() {
		return y1;
	}

	public double getX0Value() {
		return x0.doubleValue();
	}

	public double getY0Value() {
		return y0.doubleValue();
	}

	public double getX1Value() {
		return x1.doubleValue();
	}

	public double getY1Value() {
		return y1.doubleValue();
	}
}
