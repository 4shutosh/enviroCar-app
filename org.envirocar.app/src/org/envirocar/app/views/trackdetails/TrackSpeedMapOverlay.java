/**
 * Copyright (C) 2013 - 2019 the enviroCar community
 *
 * This file is part of the enviroCar app.
 *
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.app.views.trackdetails;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.envirocar.core.entity.Measurement;
import org.envirocar.core.entity.Track;
import org.envirocar.core.logging.Logger;
import java.util.List;

/**
 * @author dewall
 */
public class TrackSpeedMapOverlay extends MapLayer{
    private static final Logger LOG = Logger.getLogger(TrackSpeedMapOverlay.class);

    private final Track mTrack;

    protected LatLngBounds mTrackBoundingBox;
    protected LatLngBounds mViewBoundingBox;
    protected LatLngBounds mScrollableLimitBox;

    /**
     * Constructor.
     *
     * @param track the track to create a overlay for.
     */
    public TrackSpeedMapOverlay(Track track) {
        super();
        mTrack = track;
        if(track.getMeasurements()!=null && track.getMeasurements().size() > 1)
            initPath();
        else
        {
            addPoint(7.635147738274369, 51.96057578167202);
            addPoint(7.635078051137631, 51.96024289279303);
            setBoundingBoxes();
        }
    }

    /**
     * Initializes the track path and the bounding boxes required by the mapviews.
     */
    private void initPath() {

        List<Measurement> measurementList = mTrack.getMeasurements();

        // For each measurement value add the longitude and latitude coordinates as a new
        // mappoint to the point list. In addition, try to find out the maximum and minimum
        // lon/lat coordinates for the zoom value of the mapview.
        for (Measurement measurement : measurementList) {
            double latitude = measurement.getLatitude();
            double longitude = measurement.getLongitude();

            if(latitude == 0.0 || longitude == 0.0) {
                LOG.warn("An coordinate was 0.0");
                continue;
            }
            addPoint(latitude, longitude);
        }
        setBoundingBoxes();
    }

    protected void setBoundingBoxes(){
        // The bounding box of the pathoverlay.
        mTrackBoundingBox = new LatLngBounds.Builder()
                .includes(latLngs)
                .build();

        // The view bounding box of the pathoverlay
        mViewBoundingBox = LatLngBounds.from(
                mTrackBoundingBox.getLatNorth() + 0.01,
                mTrackBoundingBox.getLonEast() + 0.01,
                mTrackBoundingBox.getLatSouth() - 0.01,
                mTrackBoundingBox.getLonWest() - 0.01);

        // The bounding box that limits the scrolling of the mapview.
        mScrollableLimitBox = LatLngBounds.from(
                mTrackBoundingBox.getLatNorth() + 0.05,
                mTrackBoundingBox.getLonEast() + 0.05,
                mTrackBoundingBox.getLatSouth() - 0.05,
                mTrackBoundingBox.getLonWest() - 0.05);
    }

    /**
     * Gets the {@link BoundingBox} of the track.
     *
     * @return the BoundingBox of the track.
     */
    public LatLngBounds getTrackBoundingBox() {
        return mTrackBoundingBox;
    }

    /**
     * Gets the view {@link BoundingBox} of the track, which is a slightly buffered bounding box
     * for zoom purposes of the track.
     *
     * @return the BoundingBox of the track.
     */
    public LatLngBounds getViewBoundingBox() {
        return mViewBoundingBox;
    }

    /**
     * Gets the {@link BoundingBox} that is used as a scrollable limit of the track in the mapview.
     *
     * @return the BoundingBox of the track.
     */
    public LatLngBounds getScrollableLimitBox() {
        return mScrollableLimitBox;
    }

//    @Override
//    public int getNumberOfPoints() {
//        return mPoints.size();
//    }
//
//    @Override
//    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
//        final int size = this.mPoints.size();
//
//        // nothing to paint
//        if (shadow || size < 2) {
//            return;
//        }
//
//        final Projection pj = mapView.getProjection();
//
//        // precompute new points to the intermediate projection.
//        for (; this.mPointsPrecomputed < size; this.mPointsPrecomputed++) {
//            final PointF pt = this.mPoints.get(this.mPointsPrecomputed);
//            pj.toMapPixelsProjected((double) pt.x, (double) pt.y, pt);
//
//            Paint paint = new Paint();
//            paint.setColor(getColor(mValues.get(this.mPointsPrecomputed)));
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(5);
//            mPaints.add(paint);
//        }
//
//        PointF screenPoint0 = null; // points on screen
//        PointF screenPoint1;
//        PointF projectedPoint0; // points from the points list
//        PointF projectedPoint1;
//
//        // clipping rectangle in the intermediate projection, to avoid performing projection.
//        final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());
//
//        mPath.rewind();
//        boolean needsDrawing = !mOptimizePath;
//        projectedPoint0 = this.mPoints.get(size - 1);
//        mLineBounds.set((int) projectedPoint0.x, (int) projectedPoint0.y,
//                (int) projectedPoint0.x, (int) projectedPoint0.y);
//
//        mPaths.clear();
//
//        for (int i = size - 2; i >= 0; i--) {
//            // compute next points
//            projectedPoint1 = this.mPoints.get(i);
//
//            //mLineBounds needs to be computed
//            mLineBounds.union((int) projectedPoint1.x, (int) projectedPoint1.y);
//
//            // the starting point may be not calculated, because previous segment was out
//            // of clip bounds
//            if (screenPoint0 == null) {
//                screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
//            }
//
//            screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);
//
//            // skip this point, too close to previous point
//            if (Math.abs(screenPoint1.x - screenPoint0.x) +
//                    Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
//                continue;
//            }
//
//            Path segment = new Path();
//            segment.moveTo(screenPoint0.x, screenPoint0.y);
//            segment.lineTo(screenPoint1.x, screenPoint1.y);
//            mPaths.add(segment);
//
//            // update starting point to next position
//            projectedPoint0 = projectedPoint1;
//            screenPoint0.x = screenPoint1.x;
//            screenPoint0.y = screenPoint1.y;
//
//            if (mOptimizePath) {
//                needsDrawing = true;
//                mLineBounds.set((int) projectedPoint0.x, (int) projectedPoint0.y,
//                        (int) projectedPoint0.x, (int) projectedPoint0.y);
//            }
//        }
//
//        if (needsDrawing) {
//            for (int i = 0, s = mPaths.size(); i < s; i++) {
//                Path path = mPaths.get(i);
//                Paint p = mPaints.get(i);
//                float w = p.getStrokeWidth();
//                p.setStrokeWidth(w / mapView.getScale());
//                canvas.drawPath(path, p);
//                p.setStrokeWidth(w);
//            }
//        }
//    }
//
//    private int getColor(Double value) {
//        if (value == null) {
//            return Color.BLACK;
//        }
//        ArgbEvaluator ev = new ArgbEvaluator();
//        return (int) ev.evaluate((float) (value/80f), Color.RED, Color.GREEN);
////        if (value < 50.0) {
////            return Color.GREEN;
////        }
////        return Color.RED;
//    }
}
