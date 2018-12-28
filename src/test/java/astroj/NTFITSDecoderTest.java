package astroj;

import ij.plugin.FITSDecoder;
import org.junit.Test;

import static org.junit.Assert.*;

public class NTFITSDecoderTest {

    @Test
    public void setPath() {
        FITSDecoder fd = new NTFITSDecoder();
        assertNotNull(fd);
    }

}
