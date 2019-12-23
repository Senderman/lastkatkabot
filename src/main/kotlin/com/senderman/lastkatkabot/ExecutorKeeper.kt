package com.senderman.lastkatkabot

import com.senderman.lastkatkabot.admincommands.*
import com.senderman.lastkatkabot.bnc.commands.*
import com.senderman.lastkatkabot.usercommands.*
import com.senderman.neblib.AbstractExecutorKeeper

internal class ExecutorKeeper(handler: LastkatkaBotHandler) : AbstractExecutorKeeper() {

    init {
        // user commands
        register(Action(handler))
        register(Dice(handler))
        register(PayRespects(handler))
        register(Cake(handler))
        register(Help(handler, commandExecutors))
        register(GetInfo(handler))
        register(BNCHelp(handler))
        register(Pair(handler))
        register(LastPairs(handler))
        register(PinList(handler))
        register(Weather(handler))
        register(MarryMe(handler))
        register(Divorce(handler))
        register(FeedBack(handler))
        register(BNCTop(handler))
        register(BNCStart(handler))
        register(BNCInfo(handler))
        register(BNCStop(handler))
        register(BNCRuin(handler))
        register(DuelStart(handler))
        register(Row(handler))
        register(GetRow(handler))
        register(ShortInfo(handler))

        // admin commands
        register(GoodNeko(handler))
        register(TransferStats(handler))
        register(Update(handler))
        register(CleanChats())
        register(Announce(handler))
        register(SetupHelp(handler))
        register(Owners(handler))
        register(Prem(handler))
        register(Nekos(handler))
        register(Critical(handler))
        register(BadNeko(handler))
        register(AddPremium(handler))
        register(Owner(handler))
        register(Stats(handler))
        register(RunAPI(handler))
    }

}
