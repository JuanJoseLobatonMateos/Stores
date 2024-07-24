package com.jlobatonm.stores

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    val position = parent.getChildAdapterPosition(view) // item position
    val column = position % spanCount // item column

    if (includeEdge) {
        outRect.left = spacing - column * spacing / spanCount // Left spacing
        outRect.right = (column + 1) * spacing / spanCount // Right spacing

        // Adjusting top and bottom spacing
        if (position < spanCount) { // top edge
            outRect.top = spacing
        }
        outRect.bottom = spacing // item bottom
    } else {
        val columnSpacing = spacing / spanCount
        outRect.left = column * columnSpacing // Left spacing for each column
        outRect.right = columnSpacing - (column + 1) * columnSpacing // Right spacing

        // Since we want uniform spacing, apply the same spacing value for top and bottom
        if (position >= spanCount) {
            outRect.top = spacing // Top spacing for rows after the first
        }
    }
}
}
