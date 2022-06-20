package dev.pegasus.collage.views

import android.net.Uri

interface CollageView {

  fun imageCount(): Int

  fun setImageAt(index: Int, uri: Uri?)
}