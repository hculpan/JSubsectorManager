package org.culpan.subsector.generators

/**
 * Created with IntelliJ IDEA.
 * User: harry
 * Date: 3/18/13
 * Time: 9:19 PM
 * To change this template use File | Settings | File Templates.
 */
class DiceRoller {
    private final static Random rand = new Random()

    def static totalDice(num, sides) {
        def total = 0
        (1..num).each { i ->
            total += rand.nextInt(sides) + 1
        }
        return total
    }
}
