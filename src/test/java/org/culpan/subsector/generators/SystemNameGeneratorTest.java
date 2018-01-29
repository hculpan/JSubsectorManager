package org.culpan.subsector.generators;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: harry
 * Date: 3/19/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class SystemNameGeneratorTest {
    @Test
    public void testGetSystemName() throws Exception {
        SystemNameGenerator g = new SystemNameGenerator();
        for (int i = 0; i < 20; i++) {
            System.out.println(g.getSystemName());
        }
    }
}
