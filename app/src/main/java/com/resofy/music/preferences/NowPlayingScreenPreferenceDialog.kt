
package com.resofy.music.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.resofy.music.App
import com.resofy.music.R
import com.resofy.music.databinding.PreferenceNowPlayingScreenItemBinding
import com.resofy.music.extensions.*
import com.resofy.music.fragments.NowPlayingScreen
import com.resofy.music.util.PreferenceUtil
import com.resofy.music.util.ViewUtil
import com.bumptech.glide.Glide

// Únicos temas disponibles
private val AVAILABLE_SCREENS = listOf(
    NowPlayingScreen.Normal,
    NowPlayingScreen.Peek,
    NowPlayingScreen.Gradient,
    NowPlayingScreen.Material
)

class NowPlayingScreenPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    private val mLayoutRes = R.layout.preference_dialog_now_playing_screen

    override fun getDialogLayoutResource(): Int {
        return mLayoutRes
    }

    init {
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            context.colorControlNormal(),
            SRC_IN
        )
    }
}

class NowPlayingScreenPreferenceDialog : DialogFragment(), ViewPager.OnPageChangeListener {

    private var viewPagerPosition: Int = 0

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        this.viewPagerPosition = position
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater
            .inflate(R.layout.preference_dialog_now_playing_screen, null)
        val viewPager = view.findViewById<ViewPager>(R.id.now_playing_screen_view_pager)
            ?: throw IllegalStateException("Dialog view must contain a ViewPager with id 'now_playing_screen_view_pager'")

        viewPager.adapter = NowPlayingScreenAdapter(requireContext())
        viewPager.addOnPageChangeListener(this)
        viewPager.pageMargin = ViewUtil.convertDpToPixel(32f, resources).toInt()

        // Posicionar en el tema actual; si no está en la lista, empezar en 0
        val currentIndex = AVAILABLE_SCREENS.indexOf(PreferenceUtil.nowPlayingScreen)
        viewPager.currentItem = if (currentIndex >= 0) currentIndex else 0

        return materialDialog(R.string.pref_title_now_playing_screen_appearance)
            .setCancelable(false)
            .setPositiveButton(R.string.set) { _, _ ->
                PreferenceUtil.nowPlayingScreen = AVAILABLE_SCREENS[viewPagerPosition]
            }
            .setView(view)
            .create()
            .colorButtons()
    }

    companion object {
        fun newInstance(): NowPlayingScreenPreferenceDialog {
            return NowPlayingScreenPreferenceDialog()
        }
    }
}

private class NowPlayingScreenAdapter(private val context: Context) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val nowPlayingScreen = AVAILABLE_SCREENS[position]

        val inflater = LayoutInflater.from(context)
        val binding = PreferenceNowPlayingScreenItemBinding.inflate(inflater, collection, true)
        Glide.with(context).load(nowPlayingScreen.drawableResId).into(binding.image)
        binding.title.setText(nowPlayingScreen.titleRes)
        binding.proText.hide()
        return binding.root
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int = AVAILABLE_SCREENS.size

    override fun isViewFromObject(view: View, instance: Any): Boolean = view === instance

    override fun getPageTitle(position: Int): CharSequence =
        context.getString(AVAILABLE_SCREENS[position].titleRes)
}