package astroj;

import ij.io.FileInfo;
import ij.plugin.FITSDecoder;

import java.io.IOException;

// This class is only discovered by reflection in FITSReader.
// Therefore we have to suppress the unused warning.
@SuppressWarnings("unused")
public class NTFITSDecoder extends FITSDecoder {

    /**
     * In addition to returning a FileInfo, this method must fill in bscale and bzero,
     * and also set up the StringBuffer info for later use by getHeaderInfo().
     * @return FileInfo
     * @throws IOException for a variety of file reading problems
     */
    public FileInfo getInfo() throws IOException {
        if (fi != null) {
            return fi;
        } else {
            // A trivial implementation to get us started.
            fi = super.getInfo();
            return fi;
        }
    }

}
