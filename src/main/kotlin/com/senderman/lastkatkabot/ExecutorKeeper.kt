package com.senderman.lastkatkabot

import com.senderman.lastkatkabot.admincommands.*
import com.senderman.lastkatkabot.bnc.commands.*
import com.senderman.lastkatkabot.usercommands.*
import com.senderman.neblib.AbstractExecutorKeeper

internal class ExecutorKeeper(handler: LastkatkaBotHandler, db:DBService) : AbstractExecutorKeeper() {

    init {
        sequenceOf(
            // user commands
            Action(handler),
            PayRespects(handler),
            Cake(handler),
            Help(handler, commandExecutors),
            GetInfo(handler),
            BNCHelp(handler),
            Pair(handler),
            LastPairs(handler),
            PinList(handler),
            Weather(handler, db),
            MarryMe(handler, db),
            Divorce(handler),
            FeedBack(handler),
            BNCTop(handler),
            BNCStart(handler),
            BNCInfo(handler),
            BNCStop(handler),
            BNCRuin(handler),
            DuelStart(handler),
            Row(handler),
            GetRow(handler),
            ShortInfo(handler),
            // AdoptChild(handler),

            // admin commands
            GoodNeko(handler),
            TransferStats(handler),
            Update(handler),
            CleanChats(),
            Announce(handler),
            SetupHelp(handler),
            Owners(handler),
            Prem(handler),
            Nekos(handler),
            Critical(handler),
            BadNeko(handler),
            AddPremium(handler),
            Owner(handler),
            Stats(handler),
            RunAPI(handler)
        ).forEach { register(it) }
    }

}
