package com.dev.heenasupplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.R
import com.dev.heenasupplier.adapters.ImageViewAdapter
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_view_image.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
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

        imageViewAdapter = ImageViewAdapter(requireContext(), gallery)
        mView!!.rv_images.adapter = imageViewAdapter
        imageViewAdapter!!.notifyDataSetChanged()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ViewImageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}