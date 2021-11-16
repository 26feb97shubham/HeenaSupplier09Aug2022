package com.dev.heenasupplier.`interface`

import com.dev.heenasupplier.models.CategoryItem
import java.util.*

interface ClickInterface {
    interface OnRecyclerItemClick{
        fun OnClickAction(position: Int)
    }

    interface OnGalleryItemClick{
        fun OnClickAction(position: Int, )
    }

    interface OnCategoryItemClick{
        fun OnClickAction(position: Int, category : CategoryItem)
    }

    interface onServicesItemClick{
        fun onServicClick(position: Int)
        fun onServiceDele(position: Int)
        fun onServiceEdit(position: Int)
    }

    interface CalendarListener {
        fun onFirstDateSelected(startDate: Calendar?)
        fun onDateRangeSelected(startDate: Calendar?, endDate: Calendar?)
    }

    interface onOffersItemClick{
        fun onOfferDelete(position: Int)
        fun onOfferEdit(position: Int)
    }


    interface OnBookServiceClick{
        fun OnBookService()
    }

    interface OnButtonClick{
        fun OnButtonClick()
    }

    interface OnAddAddressClick{
        fun OnAddAddress()
    }

    interface OnServiceClick{
        fun OnAddService(position: Int, service: String)
    }

    interface OnAddressClick{
        fun OnAddress()
    }

    interface OnAvailableNaqashaClick{
        fun OnAvailableNaqasha()
    }

    interface OnCancelledServiceClick{
        fun OnCancelledService()
    }

    interface OnCancelServiceClick{
        fun OnCancelService(rsn_for_cancellation: String?)
    }

    interface OnRateNaqashaClick{
        fun OnRateNaqasha()
    }

    interface OnRescheduleServiceClick{
        fun OnRescheduleService()
    }

    interface OnChatWithAdminClick{
        fun OnChatWithAdmin()
    }
}