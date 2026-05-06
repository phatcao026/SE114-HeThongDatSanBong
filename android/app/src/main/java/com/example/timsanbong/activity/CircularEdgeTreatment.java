package com.example.timsanbong.activity;

import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.ShapePath;

/**
 * Custom EdgeTreatment that creates a circular notch on an edge.
 * Used for the floating action button notch in the navigation bar.
 * Creates a concave circular cutout at the top-center of the nav bar.
 */
public class CircularEdgeTreatment extends EdgeTreatment {

    private final float cradleRadius;

    public CircularEdgeTreatment(float cradleRadius, boolean isTopEdge) {
        this.cradleRadius = cradleRadius;
    }

    @Override
    public void getEdgePath(float length, float center, float interpolation, ShapePath shapePath) {
        if (interpolation == 0f) {
            // No notch, just draw a straight line
            shapePath.lineTo(length, 0f);
            return;
        }

        float notchRadius = cradleRadius * interpolation;
        float notchCenter = center;
        
        // X positions where the notch circle meets the Y=0 line
        float notchLeftX = notchCenter - notchRadius;
        float notchRightX = notchCenter + notchRadius;
        
        // Draw line from left edge to start of notch
        shapePath.lineTo(notchLeftX, 0f);

        // Draw arc from left to center to right using quadratic bezier curves
        // Center of notch circle is at (notchCenter, -notchRadius)
        float controlPointY = -notchRadius * 0.552f; // 0.552 ≈ tan(π/8), optimal for circle approximation
        
        // Left arc: from (notchLeftX, 0) down to (notchCenter, -notchRadius)
        shapePath.quadToPoint(
            notchLeftX,
            controlPointY,
            notchCenter,
            -notchRadius
        );
        
        // Right arc: from (notchCenter, -notchRadius) back up to (notchRightX, 0)
        shapePath.quadToPoint(
            notchRightX,
            controlPointY,
            notchRightX,
            0f
        );

        // Draw line from right edge of notch to the end
        shapePath.lineTo(length, 0f);
    }
}




