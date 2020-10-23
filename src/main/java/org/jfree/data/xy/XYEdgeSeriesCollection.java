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
 * XYEdgeSeriesCollection.java
 * -----------------
 * (C) Copyright 2020, by ESI (TNO).
 *
 * Original Author:  Yuri Blankenstein (for ESI (TNO));
 * Contributor(s):   -;
 *
 */

package org.jfree.data.xy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.ArrayList;

import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.general.DatasetChangeEvent;

public class XYEdgeSeriesCollection<S extends Comparable<S>> extends AbstractIntervalXYDataset<S>
		implements IntervalXYDataset<S>, VetoableChangeListener, PublicCloneable, Serializable {
	private static final long serialVersionUID = -371610910545736704L;

	private final ArrayList<XYEdgeSeries<S>> data = new ArrayList<>();

	public XYEdgeSeriesCollection() {
		// Empty
	}

	public XYEdgeSeriesCollection(XYEdgeSeries<S> series) {
		addSeries(series);
	}

    /**
     * Adds a series to the collection and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series ({@code null} not permitted).
     * 
     * @throws IllegalArgumentException if the key for the series is null or
     *     not unique within the dataset.
     */
    public void addSeries(XYEdgeSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (getSeriesIndex(series.getKey()) >= 0) {
            throw new IllegalArgumentException(
                "This dataset already contains a series with the key " 
                + series.getKey());
        }
        this.data.add(series);
        series.addChangeListener(this);
        series.addVetoableChangeListener(this);
        fireDatasetChanged();
    }

	public void removeAllSeries() {
		data.forEach(s -> s.removeChangeListener(this));
		data.clear();
		fireDatasetChanged();
	}

	@Override
	public int getSeriesCount() {
		return data.size();
	}

	public XYEdgeSeries<S> getSeries(int series) {
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		return data.get(series);
	}

    /**
     * Returns the index of the series with the specified key, or -1 if no
     * series has that key.
     * 
     * @param key  the key ({@code null} not permitted).
     * 
     * @return The index.
     */
    public int getSeriesIndex(S key) {
        Args.nullNotPermitted(key, "key");
        int seriesCount = getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
        	XYEdgeSeries<S> series = this.data.get(i);
            if (key.equals(series.getKey())) {
                return i;
            }
        }
        return -1;
    }

    @Override
	public S getSeriesKey(int series) {
		return getSeries(series).getKey();
	}

	@Override
	public int getItemCount(int series) {
		return getSeries(series).getItemCount();
	}

	@Override
	public Number getX(int series, int item) {
		return getSeries(series).getX(item);
	}

	@Override
	public Number getY(int series, int item) {
		return getSeries(series).getY(item);
	}

	@Override
	public Number getStartX(int series, int item) {
		return getSeries(series).getStartX(item);
	}

	@Override
	public Number getEndX(int series, int item) {
		return getSeries(series).getEndX(item);
	}

	@Override
	public Number getStartY(int series, int item) {
		return getSeries(series).getStartY(item);
	}

	@Override
	public Number getEndY(int series, int item) {
		return getSeries(series).getEndY(item);
	}

	@Override
	public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
		// if it is not the series name, then we have no interest
		if (!"Key".equals(e.getPropertyName())) {
			return;
		}

		// to be defensive, let's check that the source series does in fact
		// belong to this collection
		@SuppressWarnings("unchecked")
		XYEdgeSeries<S> s = (XYEdgeSeries<S>) e.getSource();
		if (getSeriesIndex(s.getKey()) == -1) {
			throw new IllegalStateException(
					"Receiving events from a series " + "that does not belong to this collection.");
		}
		// check if the new series name already exists for another series
		@SuppressWarnings("unchecked")
		S key = (S) e.getNewValue();
		if (getSeriesIndex(key) >= 0) {
			throw new PropertyVetoException("Duplicate key2", e);
		}
	}
}
