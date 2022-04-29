package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.adapters.ImageViewAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_view_image.view.*

class ViewImageFragment : Fragment() {
    private var gallery = ArrayList<String>()
    private var mView  :View?=null
    private var imageViewAdapter : ImageViewAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gallery = it.getStringArrayList("gallery") as ArrayList<String>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_view_image, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }


        if (gallery.size==0){
            mView!!.rv_images.visibility = View.GONE
            mView!!.tv_no_images_found.visibility = View.VISIBLE
        }else{
            mView!!.rv_images.visibility = View.VISIBLE
            mView!!.tv_no_images_found.visibility = View.GONE
        }

        mView!!.rv_images.layoutManager = LinearLayoutManager(requireContext(),
        LinearLayoutManager.HORIZONTAL,
        false)

        imageViewAdapter = ImageViewAdapter(requireActivity(), gallery)
        mView!!.rv_images.adapter = imageViewAdapter
        imageViewAdapter!!.notifyDataSetChanged()
    }
}