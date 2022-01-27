package com.heena.supplier.fragments

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.heena.supplier.Dialogs.NoInternetDialog
import com.heena.supplier.R
import com.heena.supplier.adapters.CardSliderAdapter
import com.heena.supplier.models.AddDeleteCardResponse
import com.heena.supplier.models.Cards
import com.heena.supplier.models.ViewCardResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_my_cards.view.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.abs

class MyCards : Fragment() {
    lateinit var CardSliderAdapter: CardSliderAdapter
    var cardsList = ArrayList<Cards>()
    private var mView : View?=null
    val fragmentsList = ArrayList<Fragment>()
    var total_no_of_cards = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView =  inflater.inflate(R.layout.fragment_my_cards, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!Utility.hasConnection(requireContext())){
            val noInternetDialog = NoInternetDialog()
            noInternetDialog.isCancelable = false
            noInternetDialog.setRetryCallback(object : NoInternetDialog.RetryInterface{
                override fun retry() {
                    viewCards()
                    noInternetDialog.dismiss()
                }
            })
            noInternetDialog.show(requireActivity().supportFragmentManager, "My Cards Fragment")
        }else{
            viewCards()
        }

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.tv_add_new_card.setSafeOnClickListener {
            findNavController().navigate(R.id.mycardsFragment_to_addNewCardFragment)
        }

        if(total_no_of_cards<=0){
            mView!!.tv_delete_card.isEnabled = false
            mView!!.tv_delete_card.isClickable = false
        }else{
            mView!!.tv_delete_card.isEnabled = true
            mView!!.tv_delete_card.isClickable = true
        }

        mView!!.tv_delete_card.setSafeOnClickListener {
            val pos = mView!!.vpCards.currentItem
            val deleteCardDialog = AlertDialog.Builder(requireContext())
            deleteCardDialog.setCancelable(false)
            deleteCardDialog.setTitle(requireContext().getString(R.string.delete_card))
            deleteCardDialog.setMessage(requireContext().getString(R.string.are_you_sure_you_want_to_delete_the_card))
            deleteCardDialog.setPositiveButton(requireContext().getString(R.string.delete), object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteCard(pos)
                    dialog!!.dismiss()
                }
            })
            deleteCardDialog.setNegativeButton(requireContext().getString(R.string.cancel), object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.cancel()
                }
            })
            deleteCardDialog.show()
        }
    }

    private fun deleteCard(pos: Int) {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_view_cards.visibility= View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id", "name", "number","cvv", "expiry_date", "type", "card_id"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(),
                cardsList!![pos].name,
                cardsList!![pos].number,
                cardsList!![pos].cvv,
                cardsList!![pos].expiry_date, "1", cardsList!![pos].id.toString()))
        val call = apiInterface.addDeleteCard(builder.build())
        call!!.enqueue(object : Callback<AddDeleteCardResponse?>{
            override fun onResponse(
                call: Call<AddDeleteCardResponse?>,
                response: Response<AddDeleteCardResponse?>
            ) {
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                                fragmentsList.removeAt(pos)
                            if (fragmentsList.size==0){
                                mView!!.tv_no_cards_found.visibility = View.VISIBLE
                                mView!!.tv_delete_card.visibility = View.GONE
                            }else{
                                mView!!.tv_no_cards_found.visibility = View.GONE
                                mView!!.tv_delete_card.visibility = View.VISIBLE
                            }
                            CardSliderAdapter.notifyDataSetChanged()
                        }else{
                            LogUtils.shortToast(requireContext(), response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<AddDeleteCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun viewCards() {
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mView!!.progressBar_view_cards.visibility= View.VISIBLE
        val call = apiInterface.showCards(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0))
        call!!.enqueue(object : Callback<ViewCardResponse?>{
            override fun onResponse(
                call: Call<ViewCardResponse?>,
                response: Response<ViewCardResponse?>
            ) {
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            mView!!.tv_no_cards_found.visibility = View.GONE
                            cardsList.clear()
                            fragmentsList.clear()
                            cardsList = (response.body()!!.cards as ArrayList<Cards>?)!!

                            if(cardsList.size==0){
                                mView!!.tv_delete_card.visibility = View.GONE
                                mView!!.tv_no_cards_found.visibility = View.VISIBLE
                            }else{
                                mView!!.tv_delete_card.visibility = View.VISIBLE
                                mView!!.tv_no_cards_found.visibility = View.GONE
                            }

                            for (i in 0 until cardsList!!.size){
                                fragmentsList.add(CardSliderFragment(cardsList!![i]))
                            }
                            total_no_of_cards = cardsList!!.size
                            CardSliderAdapter = CardSliderAdapter(requireActivity(), fragmentsList)
                            mView!!.vpCards.adapter =  CardSliderAdapter
                            // Disable clip to padding
                            mView!!.vpCards.setClipChildren(false)
                            mView!!.vpCards.setClipToPadding(false)
                            mView!!.vpCards.setOffscreenPageLimit(3)

                            mView!!.vpCards.setPageTransformer{ page: View, position: Float ->
                                page.scaleY = 1 - (0.14f * abs(position))
                            }
                        }else{
                            mView!!.tv_no_cards_found.visibility = View.VISIBLE
                        }
                    }else{
                        LogUtils.longToast(requireContext(), getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ViewCardResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(requireContext(), getString(R.string.check_internet))
                mView!!.progressBar_view_cards.visibility= View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })
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