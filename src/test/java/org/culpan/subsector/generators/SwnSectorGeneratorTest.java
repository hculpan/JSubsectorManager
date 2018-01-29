package org.culpan.subsector.generators;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: harry
 * Date: 3/18/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwnSectorGeneratorTest {
    @Test
    public void testProcess() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SwnSectorGenerator g = new SwnSectorGenerator();
        g.process(out);
        System.out.println(out.toString());
    }
}
