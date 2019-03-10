package za.co.entelect.challenge.game.engine.processor

import za.co.entelect.challenge.game.engine.command.CommandExecutor
import za.co.entelect.challenge.game.engine.command.WormsCommand
import za.co.entelect.challenge.game.engine.map.WormsMap
import za.co.entelect.challenge.game.engine.player.WormsPlayer

class WormsGameProcessor {

    fun processRound(wormsMap: WormsMap, wormsCommands: Map<WormsPlayer, WormsCommand>): Boolean {
        wormsMap.errorList.clear()
        val commands = wormsCommands.entries.sortedBy { it.value.order }
                .map { (player, command) ->  CommandExecutor(player, wormsMap, command)}

        for (command in commands) {
            command.execute()
        }

        for (player in wormsMap.livingPlayers) {
            player.selectNextWorm()
        }

        wormsMap.currentRound++

        return true
    }

    val errorList: List<String>
        get() = TODO("Not Implemented")
}
