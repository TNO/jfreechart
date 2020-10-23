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
 * XYEdgeSeries.java
 * -----------------
 * (C) Copyright 2020, by ESI (TNO).
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 * Contributor(s):   -;
 *
 */

package org.jfree.data.xy;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.util.Args;
import org.jfree.data.general.Series;

public class XYEdgeSeries<S extends Comparable<S>> extends Series<S> {
	private static final long serialVersionUID = 7175710875342448296L;

	private final List<XYEdgeDataItem> items = new ArrayList<>();

	public XYEdgeSeries(S key) {
		super(key);
	}

	public XYEdgeSeries(S key, String description) {
		super(key, description);
	}

	public void add(XYEdgeDataItem item, boolean notify) {
		Args.nullNotPermitted(item, "item");
		items.add(item);
		if (notify) {
			fireSeriesChanged();
		}
	}

	public void removeAllSeries() {
		items.clear();
		fireSeriesChanged();
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public XYEdgeDataItem getDataItem(int item) {
		if ((item < 0) || (item >= getItemCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		return items.get(item);
	}

	public Number getX(int item) {
		XYEdgeDataItem dataItem = getDataItem(item);
		return (dataItem.getX0Value() * 0.5) + (dataItem.getX1Value() * 0.5);
	}

	public Number getY(int item) {
		XYEdgeDataItem dataItem = getDataItem(item);
		return (dataItem.getY0Value() * 0.5) + (dataItem.getY1Value() * 0.5);
	}

	public Number getStartX(int item) {
		return getDataItem(item).getX0Value();
	}

	public Number getEndX(int item) {
		return getDataItem(item).getX1Value();
	}

	public Number getStartY(int item) {
		return getDataItem(item).getY0Value();
	}

	public Number getEndY(int item) {
		return getDataItem(item).getY1Value();
	}
}
