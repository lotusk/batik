/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.gvt;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;

/**
 * The top-level graphics node with a background color.
 *
 * @author <a href="mailto:Thierry.Kormann@sophia.inria.fr">Thierry Kormann</a>
 * @version $Id$
 */
public class CanvasGraphicsNode extends CompositeGraphicsNode {

    /** The background of this canvas graphics node. */
    protected Paint backgroundPaint;

    /**
     * Constructs a new empty <tt>CanvasGraphicsNode</tt>.
     */
    public CanvasGraphicsNode() {}

    //
    // Properties methods
    //

    /**
     * Sets the background paint of this canvas graphics node.
     * @param newBackgroundPaint the new background paint
     */
    public void setBackgroundPaint(Paint newBackgroundPaint) {
        this.backgroundPaint = newBackgroundPaint;
    }

    /**
     * Returns the background paint of this canvas graphics node.
     * @return the background paint
     */
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    //
    // Drawing methods
    //

    public void primitivePaint(Graphics2D g2d, GraphicsNodeRenderContext rc) {
        if (backgroundPaint != null) {
            g2d.setPaint(backgroundPaint);
            g2d.fill(g2d.getClip()); // Fast paint for huge background area
        }
        super.primitivePaint(g2d, rc);
    }
}
