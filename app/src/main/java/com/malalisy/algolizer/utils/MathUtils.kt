package com.malalisy.algolizer.utils

fun distance(x1: Double, y1: Double, x2: Double, y2: Double) =
    Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

fun midPoint(x1: Float, y1: Float, x2: Float, y2: Float) =
    (x1 + x2) / 2 to (y1 + y2) / 2