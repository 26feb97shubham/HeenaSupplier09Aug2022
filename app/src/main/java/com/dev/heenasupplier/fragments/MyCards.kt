package com.dev.heenasupplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.heenasupplier.Dialogs.NoInternetDialog
import com.dev.heenasupplier.R
import com.dev.heenasupplier.adapters.CardSliderAdapter
import com.dev.heenasupplier.adapters.MyCardsAdapter
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.Utility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_cards.*
import kotlin.math.abs

class MyCards : Fragment() {
    lateinit var myCardsAdapter: MyCardsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_my_cards, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    noInternetDialog.dismiss()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Cards Fragment")
        }

        requireActivity().iv_back.setOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        val fragmentsList = ArrayList<Fragment>()
        fragmentsList.add(CardSliderFragment())
        fragmentsList.add(CardSliderFragment())
        vpCards.adapter =  CardSliderAdapter(requireActivity(), fragmentsList)
        // Disable clip to padding
        vpCards.setClipChildren(false);
        vpCards.setClipToPadding(false);
        vpCards.setOffscreenPageLimit(3);

        vpCards.setPageTransformer{ page: View, position: Float ->
            page.scaleY = 1 - (0.14f * abs(position))
        }

       /* rv_savedcards.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        myCardsAdapter = MyCardsAdapter(requireContext())
        rv_savedcards.adapter = myCardsAdapter
        myCardsAdapter.notifyDataSetChanged()*/


        tv_edit_card.setOnClickListener {
            findNavController().navigate(R.id.mycardsFragment_to_editCardDetailsFragment)
        }

        tv_add_new_card.setOnClickListener {
            findNavController().navigate(R.id.mycardsFragment_to_addNewCardFragment)
        }
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