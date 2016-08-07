/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.BoundingBox;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;

public class AbstractShape extends AbstractRenderable implements Attributable, Highlightable {

    protected ShapeAttributes attributes;

    protected ShapeAttributes highlightAttributes;

    protected ShapeAttributes activeAttributes;

    protected boolean highlighted;

    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

    @WorldWind.PathType
    protected int pathType = WorldWind.GREAT_CIRCLE;

    protected int pickedObjectId;

    protected Color pickColor = new Color();

    protected Sector boundingSector = new Sector();

    protected BoundingBox boundingBox = new BoundingBox();

    public AbstractShape() {
        this.attributes = new ShapeAttributes();
    }

    public AbstractShape(ShapeAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public ShapeAttributes getAttributes() {
        return this.attributes;
    }

    @Override
    public void setAttributes(ShapeAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public ShapeAttributes getHighlightAttributes() {
        return this.highlightAttributes;
    }

    @Override
    public void setHighlightAttributes(ShapeAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
    }

    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
        return this.altitudeMode;
    }

    public void setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        this.reset();
    }

    @WorldWind.PathType
    public int getPathType() {
        return this.pathType;
    }

    public void setPathType(@WorldWind.PathType int pathType) {
        this.pathType = pathType;
        this.reset();
    }

    @Override
    protected void doRender(RenderContext rc) {
        // Don't render anything if the shape is not visible.
        if (!this.intersectsFrustum(rc)) {
            return;
        }

        // Select the currently active attributes. Don't render anything if the attributes are unspecified.
        this.determineActiveAttributes(rc);
        if (this.activeAttributes == null) {
            return;
        }

        // Keep track of the drawable count to determine whether or not this shape has enqueued drawables.
        int drawableCount = rc.drawableCount();
        if (rc.pickMode) {
            this.pickedObjectId = rc.nextPickedObjectId();
            this.pickColor = PickedObject.identifierToUniqueColor(this.pickedObjectId, this.pickColor);
        }

        // Enqueue drawables for processing on the OpenGL thread.
        this.makeDrawable(rc);

        // Enqueue a picked object that associates the shape's drawables with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(this.pickedObjectId, this, rc.currentLayer));
        }
    }

    protected void reset() {
    }

    protected boolean intersectsFrustum(RenderContext rc) {
        return this.boundingBox.isUnitBox() || this.boundingBox.intersectsFrustum(rc.frustum);
    }

    protected void determineActiveAttributes(RenderContext rc) {
        if (this.highlighted && this.highlightAttributes != null) {
            this.activeAttributes = this.highlightAttributes;
        } else {
            this.activeAttributes = this.attributes;
        }
    }

    protected void makeDrawable(RenderContext rc) {
    }
}