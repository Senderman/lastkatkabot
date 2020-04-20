package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.bnc.VoteBnc
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class Callbacks(handler:LastkatkaBotHandler) {

    private val callbackHandlers: MutableMap<String, CallbackHandler> = HashMap()

    init {
        sequenceOf(
            AcceptMarriage(handler),
            AnswerFeedback(handler),
            BlockUser(handler),
            CakeNot(handler),
            CakeOk(handler),
            CloseMenu(handler),
            DeleteAdmin(handler),
            DeleteNeko(handler),
            DeletePremium(handler),
            DenyMarriage(handler),
            JoinDuel(handler),
            PayRespects(handler),
            VoteBnc(handler)
        ).forEach(::register)
    }

    fun findHandler(query: CallbackQuery): CallbackHandler?{
        return callbackHandlers[query.data.split(" ", limit = 2)[0].trim() + " "]
    }

    private fun register(callbackHandler: CallbackHandler){
        callbackHandlers[callbackHandler.trigger] = callbackHandler
    }

    companion object {
        //const val CALLBACK_REGISTER_IN_TOURNAMENT = "register_in_tournament "
        const val PAY_RESPECTS = "pay_respects "
        const val CAKE_OK = "cake_ok "
        const val CAKE_NOT = "cake_not "
        const val JOIN_DUEL = "join_duel "
        const val CLOSE_MENU = "close_menu "
        const val DELETE_ADMIN = "deleteuser_admin "
        const val DELETE_NEKO = "deleteuser_neko "
        const val DELETE_PREM = "deleteuser_prem "
        const val VOTE_BNC = "vote_bnc "
        const val ACCEPT_MARRIAGE = "acc_marr "
        const val DENY_MARRIAGE = "deny_marr "
        const val ANSWER_FEEDBACK = "answ_feedback "
        const val BLOCK_USER = "block_user "
        const val ADOPT_CHILD = "adopt "
        const val DECLINE_CHILD = "dec_adopt "
    }
}