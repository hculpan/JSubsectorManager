package org.culpan.subsector.generators

import groovy.xml.MarkupBuilder
import org.apache.bsf.util.IndentWriter

/**
 * Created with IntelliJ IDEA.
 * User: harry
 * Date: 3/18/13
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
class SwnSectorGenerator {
    final static String tagDescriptorsPath = 'tagdescriptors.xml'

    static def tagDescriptors

    def static loadTagDescriptors() {
        println('Loading tag descriptors')
        tagDescriptors = new XmlSlurper().parse(SwnSectorGenerator.class.getClassLoader().getResourceAsStream(tagDescriptorsPath))
        println("Finished loading tag descriptors, found ${tagDescriptors.tag.size()} tags")
    }

    def process(String filename) {
        FileOutputStream out = null
        try {
            out = new FileOutputStream(filename)
            process(out)
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    def protected determineAtmosphere(builder) {
        def num = DiceRoller.totalDice(2, 6)
        if (num <=2)        { return builder.atmosphere('Corrosive') }
        else if (num <= 3)  { return builder.atmosphere('Inert gas') }
        else if (num <= 4)  { return builder.atmosphere('Airless or thin') }
        else if (num <= 9)  { return builder.atmosphere('Breathable') }
        else if (num <= 10) { return builder.atmosphere('Thick') }
        else if (num <= 11) { return builder.atmosphere('Invasive, toxic') }
        else                { return builder.atmosphere('Corrosive and invasive') }
    }

    def protected determineTemperature(builder) {
        def num = DiceRoller.totalDice(2, 6)
        if (num <=2)        { return builder.temperature('Frozen') }
        else if (num <= 3)  { return builder.temperature('Variable cold-to-temperate') }
        else if (num <= 5)  { return builder.temperature('Cold') }
        else if (num <= 8)  { return builder.temperature('Temperate') }
        else if (num <= 10) { return builder.temperature('Warm') }
        else if (num <= 11) { return builder.temperature('Variable temperate-to-warm') }
        else                { return builder.temperature('Burning') }
    }

    def protected determineBiosphere(builder) {
        def num = DiceRoller.totalDice(2, 6)
        if (num <=2)        { return builder.biosphere('Biosphere remnants') }
        else if (num <= 3)  { return builder.biosphere('Microbial life') }
        else if (num <= 5)  { return builder.biosphere('No native') }
        else if (num <= 8)  { return builder.biosphere('Human-miscible') }
        else if (num <= 10) { return builder.biosphere('Immiscible') }
        else if (num <= 11) { return builder.biosphere('Hybrid') }
        else                { return builder.biosphere('Engineered') }
    }

    def protected determinePopulations(builder) {
        def popSize
        def popDescr
        def num = DiceRoller.totalDice(2, 6)

        if (num <=2) {
            popDescr = 'Failed colony'
            popSize = '0'
        } else if (num <= 3)  {
            popDescr = 'Outpost'
            popSize = Integer.toString(DiceRoller.totalDice(1, 99)) + '00'
        } else if (num <= 5)  {
            popDescr = 'Tens of thousands'
            popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + Integer.toString(DiceRoller.totalDice(1, 10) - 1) + ',000'
        } else if (num <= 8)  {
            popDescr = 'Hundreds of thousands'
            popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + Integer.toString(DiceRoller.totalDice(1, 99)).padLeft(2, '0') + ',000'
        } else if (num <= 10) {
            popDescr = 'Millions'
            popSize = Integer.toString(DiceRoller.totalDice(1, 999)) + ',' + Integer.toString(DiceRoller.totalDice(1, 999)).padLeft(3, '0') + ',000'
        } else if (num <= 11) {
            popDescr = 'Billions'
            popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + ',' + Integer.toString(DiceRoller.totalDice(1, 999)).padLeft(3, '0') + ',' + Integer.toString(DiceRoller.totalDice(1, 99)).padLeft(2, '0') + '0,000'
        } else {
            popDescr = 'Alien'
            num = DiceRoller.totalDice(1, 6)
            if (num <= 1) {
                popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + Integer.toString(DiceRoller.totalDice(1, 10) - 1) + ',000'
            } else if (num <= 3) {
                popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + Integer.toString(DiceRoller.totalDice(1, 99)).padLeft(2, '0') + ',000'
            } else if (num <= 5) {
                popSize = Integer.toString(DiceRoller.totalDice(1, 999)) + ',' + Integer.toString(DiceRoller.totalDice(1, 999)).padLeft(3, '0') + ',000'
            } else if (num <= 6) {
                popSize = Integer.toString(DiceRoller.totalDice(1, 9)) + ',' + Integer.toString(DiceRoller.totalDice(1, 999)).padLeft(3, '0') + ',' + Integer.toString(DiceRoller.totalDice(1, 99)).padLeft(2, '0') + '0,000'
            }
        }

        return builder.population {
            value( "${popSize}" )
            description( "${popDescr}" )
        }
    }

    def determineTechLevel(builder) {
        def techValue
        def techDescr
        def num = DiceRoller.totalDice(2, 6)

        if (num <=2) {
            techDescr = 'Stone age'
            techValue = '0'
        } else if (num <= 3)  {
            techDescr = 'Medieval'
            techValue = '1'
        } else if (num <= 4)  {
            techDescr = 'Nineteenth-century'
            techValue = '2'
        } else if (num <= 6)  {
            techDescr = 'Twentieth-century'
            techValue = '3'
        } else if (num <= 10) {
            techDescr = 'Baseline postech'
            techValue = '4'
        } else if (num <= 11) {
            techDescr = 'Postech with specialties'
            techValue = '4+'
        } else {
            techDescr = 'Pretech'
            techValue = '5'
        }

        return builder.'tech-level' {
            value( "${techValue}" )
            description( "${techDescr}" )
        }
    }

    def determineSize(builder) {
        def val
        def descr
        def num = DiceRoller.totalDice(2, 6)

        if (num <= 0) {
            descr = 'Negligible'
            val = '800 km'
        } else if (num <= 1)  {
            descr = '0.05'
            val = '1,600 km'
        } else if (num <= 2)  {
            descr = '0.15'
            val = '3,200 km'
        } else if (num <= 3)  {
            descr = '0.25'
            val = '4,800 km'
        } else if (num <= 4)  {
            descr = '0.35'
            val = '6,400 km'
        } else if (num <= 5)  {
            descr = '0.45'
            val = '8,000 km'
        } else if (num <= 6) {
            descr = '0.7'
            val = '9,600 km'
        } else if (num <= 8) {
            descr = '0.9'
            val = '11,200 km'
        } else if (num <= 10) {
            descr = '1.0'
            val = '12,800 km'
        } else if (num <= 11) {
            descr = '1.25'
            val = '14,400 km'
        } else {
            descr = '1.4'
            val = '16,000 km'
        }

        return builder.'world-size' {
            value( "${val}" )
            gravity( "${descr}" )
        }
    }

    def determineGasGiant(builder) {
        if (DiceRoller.totalDice(2, 6) < 10) {
            return builder.'gas-giant' ( 'yes' )
        } else {
            return builder.'gas-giant' ( 'no' )
        }
    }

    def determineName(builder) {
        def name = SystemNameGenerator.getSystemName()
        builder.name( name.substring(0, 1) + name.substring(1).toLowerCase() )
    }

    def determineTags(builder) {
        def num1 = DiceRoller.totalDice(1, tagDescriptors.tag.size()) - 1
        def num2
        while (true) {
            num2 = DiceRoller.totalDice(1, tagDescriptors.tag.size()) - 1
            if (num2 != num1) {
                break
            }
        }

        builder.tags ( count: '2' ) {
            tag(tagDescriptors.tag[num1]['@name'])
            tag(tagDescriptors.tag[num2]['@name'])
        }

        def travelZone1 = tagDescriptors.tag[num1]['travel-zone']
        def travelZone2 = tagDescriptors.tag[num2]['travel-zone']
        if (travelZone1 == 'red' || travelZone2 == 'red') {
            builder.'travel-zone' ( 'red' )
        } else if (travelZone1 == 'amber' || travelZone2 == 'amber') {
            builder.'travel-zone' ( 'amber' )
        }

        addDescriptors(builder, 'enemies', 'enemy', num1, num2)
        addDescriptors(builder, 'friends', 'friend', num1, num2)
        addDescriptors(builder, 'complications', 'complication', num1, num2)
        addDescriptors(builder, 'things', 'thing', num1, num2)
        addDescriptors(builder, 'places', 'place', num1, num2)
    }

    def addDescriptors(builder, plural, singular, num1, num2) {
        def descrList = tagDescriptors.tag[num1]."${plural}"."${singular}".collect{ it.text() }
        descrList.addAll(tagDescriptors.tag[num2]."${plural}"."${singular}".collect{ it.text() })
        def itemIndexList = []
        3.times{
            while (true) {
                def num = DiceRoller.totalDice(1, descrList.size()) - 1
                if (!(num in itemIndexList)) {
                    itemIndexList << num
                    break
                }
            }
        }
        builder."${plural}" ( count: "${itemIndexList.size()}" ) {
            for (num in itemIndexList) {
                "${singular}"( descrList[num] )
            }
        }

    }

    def protected buildStar(builder, x, y) {
        if (DiceRoller.totalDice(1, 100) < 35) {
            return builder.star( 'id': Integer.toString(x).padLeft(2, '0') + Integer.toString(y).padLeft(2, '0') ) {
                determineName(builder)
                determineAtmosphere(builder)
                determineTemperature(builder)
                determineBiosphere(builder)
                determinePopulations(builder)
                determineTechLevel(builder)
                determineSize(builder)
                determineGasGiant(builder)
                determineTags(builder)
            }
        } else {
            return null
        }
    }

    def protected buildStars(builder) {
        return builder.stars {
            for (x in 1..8) {
                for (y in 1..10) {
                    buildStar(builder, x, y)
                }
            }
        }
    }

    def protected buildTradeRoutes(builder) {
        return builder.'trade-routes'()
    }

    def process(OutputStream out) {
        if (tagDescriptors == null) {
            loadTagDescriptors()
        }

        def xml = new MarkupBuilder(new IndentWriter(out))

        xml.subsector {
            buildStars(xml)
            buildTradeRoutes(xml)
        }
    }
}
