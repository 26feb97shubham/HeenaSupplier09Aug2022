package com.heena.supplier.extras

import android.os.CountDownTimer

class MembershipCountDownTimer(val millisInFuture : Long, val countDowmInterval : Long) : CountDownTimer(millisInFuture, countDowmInterval) {
    override fun onTick(millisUntilFinished: Long) {
        var progress = millisUntilFinished/1000
    }

    override fun onFinish() {
        TODO("Not yet implemented")
    }
}