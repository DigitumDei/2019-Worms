package za.co.entelect.challenge.game.engine.player

import za.co.entelect.challenge.game.engine.entities.GameConfig
import za.co.entelect.challenge.game.engine.map.Point

object CommandoWorm {

    fun build(config: GameConfig, position: Point): Worm {
        return Worm(health = config.commandoWorms.initialHp,
                position = position,
                weapon = config.commandoWorms.weapon,
                diggingRange = config.commandoWorms.diggingRange,
                movementRange = config.commandoWorms.movementRage)
    }

    fun build(config: GameConfig): Worm {
        return Worm(health = config.commandoWorms.initialHp,
                weapon = config.commandoWorms.weapon,
                diggingRange = config.commandoWorms.diggingRange,
                movementRange = config.commandoWorms.movementRage)
    }
}



