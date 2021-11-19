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
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.adapters.OffersAndDiscountsListingAdapter
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_filtered_offers_and_discounts.view.*

class FilteredOffersAndDiscountsFragment : Fragment() {
    lateinit var offersAndDiscountsListingAdapter: OffersAndDiscountsListingAdapter
    var mView : View ? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_filtered_offers_and_discounts, container, false)
        return mView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        mView!!.rv_filtered_offers_n_disc_listing.layoutManager = LinearLayoutManager(requireContext(),  LinearLayoutManager.VERTICAL,
            false)

        offersAndDiscountsListingAdapter = OffersAndDiscountsListingAdapter(requireContext(), object : ClickInterface.OnRecyclerItemClick{
            override fun OnClickAction(position: Int) {
            }

        })

        mView!!.rv_filtered_offers_n_disc_listing.adapter = offersAndDiscountsListingAdapter
        offersAndDiscountsListingAdapter.notifyDataSetChanged()
    }
    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}