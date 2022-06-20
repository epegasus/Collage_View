package dev.pegasus.collagemaker.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import dev.pegasus.collage.CollageViewFactory
import dev.pegasus.collage.views.AbstractCollageView
import dev.pegasus.collagemaker.R
import dev.pegasus.collagemaker.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val layoutHashmap = LinkedHashMap<Int, CollageViewFactory.CollageLayoutType>()
    private lateinit var collageViewFactory: CollageViewFactory
    private lateinit var collageView: AbstractCollageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return getView(inflater, container, R.layout.fragment_home)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initLayoutTypes()
        initCollage()

    }

    private fun initLayoutTypes() {
        layoutHashmap[R.drawable.ic_collage_2_image_vertical] = CollageViewFactory.CollageLayoutType.TWO_IMAGE_VERTICAL
        layoutHashmap[R.drawable.ic_collage_2_image_horizontal] = CollageViewFactory.CollageLayoutType.TWO_IMAGE_HORIZONTAL
        layoutHashmap[R.drawable.ic_collage_type_3image0] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_0
        layoutHashmap[R.drawable.ic_collage_type_3image1] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_1
        layoutHashmap[R.drawable.ic_collage_type_3image2] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_2
        layoutHashmap[R.drawable.ic_collage_type_3image3] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_3
        layoutHashmap[R.drawable.ic_collage_type_3image4] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_HORIZONTAL
        layoutHashmap[R.drawable.ic_collage_type_3image5] = CollageViewFactory.CollageLayoutType.THREE_IMAGE_VERTICAL
        layoutHashmap[R.drawable.ic_collage_type_4image0] = CollageViewFactory.CollageLayoutType.FOUR_IMAGE_0
        layoutHashmap[R.drawable.ic_collage_type_4image1] = CollageViewFactory.CollageLayoutType.FOUR_IMAGE_1
        layoutHashmap[R.drawable.ic_collage_type_4image2] = CollageViewFactory.CollageLayoutType.FOUR_IMAGE_2
        layoutHashmap[R.drawable.ic_collage_type_4image3] = CollageViewFactory.CollageLayoutType.FOUR_IMAGE_3
        layoutHashmap[R.drawable.ic_collage_type_4image4] = CollageViewFactory.CollageLayoutType.FOUR_IMAGE_4

        layoutHashmap[R.drawable.ic_collage_type_6image1] = CollageViewFactory.CollageLayoutType.SIX_IMAGE_1
    }

    private fun initCollage() {
        binding.clContainerHome.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (binding.clContainerHome.height > 0) {
                        initCollageViewFactory()
                        setUI()
                        binding.clContainerHome.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
    }

    private fun initCollageViewFactory() {
        collageViewFactory = CollageViewFactory(
            context = globalContext,
            attrs = null,
            layoutWidth = binding.clContainerHome.width,
            layoutHeight = binding.clContainerHome.height,
            isBorderEnabled = false,
            imageUris = getImageUris()
        )
    }

    private fun getImageUris(): Array<Uri?> {
        val uri1 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_1}")
        val uri2 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_2}")
        val uri3 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_3}")
        val uri4 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_4}")
        val uri5 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_5}")
        val uri6 = Uri.parse("android.resource://${globalContext.packageName}/${R.drawable.image_6}")
        return arrayOf(uri1, uri2, uri3, uri4, uri5, uri6)
    }

    private fun setUI() {
        val layoutList = layoutHashmap.values.toList()
        val collageLayoutType = layoutList[13]
        onCollageTypeSelected(collageLayoutType)
    }

    private fun onCollageTypeSelected(collageLayoutType: CollageViewFactory.CollageLayoutType) {
        collageView = collageViewFactory.getView(collageLayoutType)
        binding.clContainerHome.removeAllViews()
        binding.clContainerHome.addView(collageView)
    }
}