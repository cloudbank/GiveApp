package com.droidteahouse.give.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.droidteahouse.backdrop.BackDropIconClickListener
import com.droidteahouse.give.R
import com.droidteahouse.give.vo.Charity
import kotlinx.android.synthetic.main.charity_detail.*
import kotlinx.android.synthetic.main.charity_item.missionStatement
import kotlinx.android.synthetic.main.charity_item.name
import kotlinx.android.synthetic.main.charity_item.thumbnail
import kotlinx.android.synthetic.main.main_layout.*


class DetailFragment : BaseFragment() {


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.charity_detail, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val uri = getArguments()?.getInt("uri")
        val charity = getArguments()?.getParcelable<Charity>("charity")

        missionStatement.text = charity?.mission
        name.text = charity?.charityName
        website.text = charity?.websiteURL
        thumbnail.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                transitionName = "transName"
            }
            glide.load(uri)
                    .fitCenter()
                    .placeholder(android.R.drawable.btn_star)
                    .into(this)
        }
        val pg = (activity as AppCompatActivity?)?.findViewById<NestedScrollView>(R.id.product_grid) as NestedScrollView
        pg?.setOnTouchListener(null)
        val toolbar = (activity as AppCompatActivity?)?.findViewById<Toolbar>(R.id.app_bar)
        toolbar?.setNavigationIcon(null)

        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back_24px)
        toolbar?.setNavigationOnClickListener(View.OnClickListener {
            val nc = myNavHostFragment.findNavController()
            nc.navigate(R.id.charityFragment)

            val backdropListener = BackDropIconClickListener(
                    activity!!,
                    pg!!,
                    ContextCompat.getDrawable(activity!!, R.drawable.shr_branded_menu), // Menu open icon
                    ContextCompat.getDrawable(activity!!, R.drawable.shr_close_menu))

            toolbar.setNavigationIcon(R.drawable.shr_branded_menu)

            toolbar.invalidate()
            toolbar.requestLayout()
            toolbar.setNavigationOnClickListener(backdropListener) // Menu close icon
        })
    }


}
